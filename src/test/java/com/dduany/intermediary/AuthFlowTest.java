package com.dduany.intermediary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login issues a JWT; data endpoints require it; docs stay public. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = { "app.auth.username=dev", "app.auth.password=dev" })
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void dataEndpointsRequireAToken() throws Exception {
        mvc.perform(get("/plan-items")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ai/status")).andExpect(status().isUnauthorized());
    }

    @Test
    void docsAndLoginArePublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dev\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loginTokenUnlocksDataEndpoints() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dev\",\"password\":\"dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dev"))
                .andReturn().getResponse().getContentAsString();
        JsonNode login = json.readTree(body);
        String token = login.get("token").asText();
        assertThat(token).isNotBlank();

        mvc.perform(get("/plan-items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dev"));
        // AI is off in tests because ANTHROPIC_API_KEY is not set.
        mvc.perform(get("/ai/status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
        mvc.perform(post("/ai/suggest").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"context\":\"# nothing\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
