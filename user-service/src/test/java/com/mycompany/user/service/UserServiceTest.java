package com.mycompany.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.user.dto.UserDto;
import com.mycompany.user.model.User;
import com.mycompany.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testRegisterUserSuccess() {
    UserDto userDto =
        UserDto.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();

    String hashedPassword = "hashed_password123";
    when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    User savedUser =
        User.builder()
            .id(1L)
            .email("test@example.com")
            .hashedPassword(hashedPassword)
            .firstName("Test")
            .lastName("User")
            .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserDto result = userService.registerUser(userDto);
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  public void testRegisterUserDuplicateEmail() {
    UserDto userDto =
        UserDto.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(User.builder().build()));

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              userService.registerUser(userDto);
            });
    assertEquals("User with this email already exists", exception.getMessage());
  }

  @Test
  public void testUpdateUserSuccess() {
    Long userId = 1L;
    UserDto updateDto =
        UserDto.builder().firstName("Updated").lastName("User").password("newpass").build();

    User existingUser =
        User.builder()
            .id(userId)
            .email("test@example.com")
            .hashedPassword("oldHashed")
            .firstName("Test")
            .lastName("User")
            .build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    String newHashed = "newHashed";
    when(passwordEncoder.encode("newpass")).thenReturn(newHashed);
    existingUser.setHashedPassword(newHashed);
    existingUser.setFirstName("Updated");
    existingUser.setLastName("User");
    when(userRepository.save(existingUser)).thenReturn(existingUser);

    UserDto result = userService.updateUser(userId, updateDto);
    assertNotNull(result);
    assertEquals("Updated", result.getFirstName());
    assertEquals("User", result.getLastName());
  }
}
