package com.dduany.intermediary;

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
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(put("/plan-items/" + id).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Dated\",\"intent\":\"READ\",\"status\":\"PLANNED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDate").value((Object) null))
                .andExpect(jsonPath("$.notes").value((Object) null));
    }

    @Test
    void recurringPlanGeneratesOneItemPerMatchingDayAndIsIdempotent() throws Exception {
        DayOfWeek tomorrow = LocalDate.now().plusDays(1).getDayOfWeek();
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
                assertThat(n.get("targetDate").asText()).isEqualTo(LocalDate.now().plusDays(1).toString());
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
