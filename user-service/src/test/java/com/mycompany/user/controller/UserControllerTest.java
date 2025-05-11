package com.mycompany.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.user.dto.UserDto;
import com.mycompany.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  private MockMvc mockMvc;

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  @DisplayName("Успешная регистрация нового пользователя")
  public void testRegisterUser() throws Exception {
    UserDto inputDto =
        UserDto.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();

    UserDto outputDto =
        UserDto.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

    when(userService.registerUser(any(UserDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.firstName").value("Test"));
  }

  @Test
  @DisplayName("Успешное обновление данных пользователя")
  public void testUpdateUser() throws Exception {
    Long userId = 1L;
    UserDto inputDto =
        UserDto.builder()
            .email("test@example.com")
            .firstName("Updated")
            .lastName("User")
            .password("newpassword")
            .build();

    UserDto outputDto =
        UserDto.builder()
            .id(userId)
            .email("test@example.com")
            .firstName("Updated")
            .lastName("User")
            .build();

    when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.firstName").value("Updated"));
  }
}
