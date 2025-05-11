package com.mycompany.auth.it;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAccessIntegrationTest {

  @Autowired private MockMvc mvc;

  @Test
  @WithMockUser(username = "bob", roles = "USER")
  void adminEndpoint_forbidden_for_userRole() throws Exception {
    mvc.perform(get("/admin/health")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminEndpoint_allowed_for_adminRole() throws Exception {
    mvc.perform(get("/admin/health"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("UP")));
  }
}
