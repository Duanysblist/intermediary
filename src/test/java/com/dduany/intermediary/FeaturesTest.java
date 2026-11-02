package com.dduany.intermediary;

import com.dduany.intermediary.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full-replacement PUT, recurring plans, proposals, and the calendar feed, through HTTP. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = { "app.auth.username=dev", "app.auth.password=dev" })
@AutoConfigureMockMvc
class FeaturesTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AppProperties props;

    String bearer;

    @BeforeEach
    void login() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dev\",\"password\":\"dev\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        bearer = "Bearer " + json.readTree(body).get("token").asText();
    }

    @Test
    void putReplacesTheRecordSoADateCanBeCleared() throws Exception {
        String created = mvc.perform(post("/plan-items").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Dated\",\"intent\":\"READ\",\"targetDate\":\"2030-01-05\",\"notes\":\"keep\"}"))
                .andExpect(status().isCreated())
                // Server-set timestamps are UTC instants, so every client can convert them to its own zone.
                .andExpect(jsonPath("$.createdAt").value(endsWith("Z")))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(put("/plan-items/" + id).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Dated\",\"intent\":\"READ\",\"status\":\"PLANNED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDate").value((Object) null))
                .andExpect(jsonPath("$.notes").value((Object) null));
    }

    @Test
    void recurringPlanGeneratesOneItemPerMatchingDayAndIsIdempotent() throws Exception {
        // "Today" on the server is in the configured app zone, not the JVM default.
        LocalDate tomorrowDate = LocalDate.now(props.zone()).plusDays(1);
        DayOfWeek tomorrow = tomorrowDate.getDayOfWeek();
        String plan = mvc.perform(post("/recurring-plans").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Routine\",\"intent\":\"EXERCISE\",\"days\":[\"" + tomorrow + "\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        long planId = json.readTree(plan).get("id").asLong();

        String first = mvc.perform(post("/recurring-plans/generate?days=7").header("Authorization", bearer))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode items = json.readTree(first);
        long mine = 0;
        for (JsonNode n : items) if (n.get("recurringPlanId").asLong() == planId) mine++;
        assertThat(mine).isEqualTo(1);
        for (JsonNode n : items) {
            if (n.get("recurringPlanId").asLong() == planId) {
                assertThat(n.get("targetDate").asText()).isEqualTo(tomorrowDate.toString());
            }
        }

        String second = mvc.perform(post("/recurring-plans/generate?days=7").header("Authorization", bearer))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long again = 0;
        for (JsonNode n : json.readTree(second)) if (n.get("recurringPlanId").asLong() == planId) again++;
        assertThat(again).isZero();
    }

    @Test
    void proposalsAreStoredPendingAndCanBeResolved() throws Exception {
        String created = mvc.perform(post("/proposals").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"test\",\"summary\":\"Move it\",\"changes\":[{\"op\":\"update\",\"id\":1,\"fields\":{\"targetDate\":\"CLEAR\"},\"reason\":\"r\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.changes[0].fields.targetDate").value("CLEAR"))
                .andExpect(jsonPath("$.changes[0].fields.title").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(get("/proposals?status=PENDING").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + id)));

        mvc.perform(put("/proposals/" + id + "/status").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void upsertByKeyUpdatesTheSameItemAndKeepsTheUsersStatus() throws Exception {
        String first = mvc.perform(put("/plan-items/by-key").param("key", "tii:2030-03-04:block").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A1\",\"intent\":\"STUDY\",\"targetDate\":\"2030-03-04\",\"notes\":\"v1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(first).get("id").asLong();

        mvc.perform(put("/plan-items/" + id).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A1\",\"intent\":\"STUDY\",\"targetDate\":\"2030-03-04\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk());

        mvc.perform(put("/plan-items/by-key").param("key", "tii:2030-03-04:block").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A1, Red\",\"intent\":\"STUDY\",\"targetDate\":\"2030-03-04\",\"notes\":\"v2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("A1, Red"))
                .andExpect(jsonPath("$.notes").value("v2"))
                .andExpect(jsonPath("$.status").value("DONE"));

        // Once the day is graded, the sender's verdict wins.
        mvc.perform(put("/plan-items/by-key").param("key", "tii:2030-03-04:block").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A1, Red\",\"intent\":\"STUDY\",\"targetDate\":\"2030-03-04\",\"status\":\"CANCELED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mvc.perform(put("/plan-items/by-key").param("key", " ").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\",\"intent\":\"STUDY\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void documentUpsertByPathKeepsOneRowPerLink() throws Exception {
        String body = "{\"title\":\"%s\",\"path\":\"https://example.com/doc-upsert\",\"type\":\"GUIDE\"}";
        String first = mvc.perform(put("/documents/by-path").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("Old name")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(first).get("id").asLong();

        mvc.perform(put("/documents/by-path").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("New name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("New name"));
    }

    long createItem(String title, String status) throws Exception {
        String body = mvc.perform(post("/plan-items").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"intent\":\"OTHER\",\"status\":\"" + status + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void orderSetsPositionsMovesColumnsAndAnEditToAnotherColumnClearsIt() throws Exception {
        long a = createItem("a", "PLANNED"), b = createItem("b", "PLANNED"), c = createItem("c", "DONE");

        // c is dragged from Done to the top of Planned.
        mvc.perform(put("/plan-items/order").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANNED\",\"ids\":[" + c + "," + b + "," + a + "]}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/plan-items/" + c).header("Authorization", bearer))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.position").value(0));
        mvc.perform(get("/plan-events").param("planItemId", String.valueOf(c)).header("Authorization", bearer))
                .andExpect(jsonPath("$[0].toStatus").value("PLANNED"));

        // An edit that keeps the status keeps the position; one that changes it sends the item to the bottom.
        mvc.perform(put("/plan-items/" + a).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"a2\",\"intent\":\"OTHER\",\"status\":\"PLANNED\"}"))
                .andExpect(jsonPath("$.position").value(2));
        mvc.perform(put("/plan-items/" + a).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"a2\",\"intent\":\"OTHER\",\"status\":\"DONE\"}"))
                .andExpect(jsonPath("$.position").value((Object) null));

        // An unknown id rejects the whole column.
        mvc.perform(put("/plan-items/order").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\",\"ids\":[" + b + ",999999]}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/plan-items/" + b).header("Authorization", bearer))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void calendarFeedNeedsItsTokenAndListsDatedItems() throws Exception {
        mvc.perform(post("/plan-items").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"On the calendar; yes, really\",\"intent\":\"READ\",\"targetDate\":\"2030-02-03\"}"))
                .andExpect(status().isCreated());

        String link = mvc.perform(get("/calendar/link").header("Authorization", bearer))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String url = json.readTree(link).get("url").asText();
        String token = url.substring(url.indexOf("token=") + 6);

        mvc.perform(get("/calendar.ics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/calendar.ics").param("token", "nope")).andExpect(status().isUnauthorized());
        mvc.perform(get("/calendar.ics").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/calendar"))
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("DTSTART;VALUE=DATE:20300203")))
                .andExpect(content().string(containsString("SUMMARY:On the calendar\\; yes\\, really")));
    }
}
