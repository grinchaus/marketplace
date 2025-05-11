package com.mycompany.auth.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    private static String token;
    private static String refreshToken;

    @Test
    @Order(1)
    void signup_ok() throws Exception {
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "username":"alice","email":"alice@ex.com","password":"StrongP@ss1" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Order(2)
    void signup_duplicate_email() throws Exception {
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "username":"another","email":"alice@ex.com","password":"Pass2" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10));
    }

    @Test
    @Order(3)
    void signin_ok() throws Exception {
        MvcResult res = mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "username":"alice","password":"StrongP@ss1" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode body = mapper.readTree(res.getResponse().getContentAsString());
        token = body.get("token").asText();
        refreshToken = body.get("refreshToken").asText();
    }

    @Test
    @Order(4)
    void signin_bad_password() throws Exception {
        mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "username":"alice","password":"wrong" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.message").value("exception.badCredentials"));
    }

    @Test
    @Order(5)
    void protected_without_token() throws Exception {
        mvc.perform(get("/api/protected")).andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void protected_with_token() throws Exception {
        mvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("protected data")));
    }

    @Test
    @Order(7)
    void refreshToken_flow() throws Exception {
        MvcResult res = mvc.perform(post("/auth/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        token = mapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }
}
