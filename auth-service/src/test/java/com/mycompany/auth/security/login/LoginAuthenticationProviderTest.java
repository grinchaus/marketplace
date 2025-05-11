package com.mycompany.auth.security.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.auth.model.Role;
import com.mycompany.auth.model.User;
import com.mycompany.auth.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class LoginAuthenticationProviderTest {

  private LoginAuthenticationProvider provider;

  @Mock private UserService userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    provider = new LoginAuthenticationProvider(userService);
  }

  @Test
  void authenticate_success() throws Exception {
    String raw = "rawPass";
    String hashed = new BCryptPasswordEncoder().encode(raw);
    User dbUser =
        User.builder()
            .username("john")
            .password(hashed)
            .roles(Set.of(Role.builder().name("USER").build()))
            .build();

    when(userService.findByUsername("john")).thenReturn(dbUser);
    var token = new UsernamePasswordAuthenticationToken("john", raw);
    var auth = provider.authenticate(token);
    assertTrue(auth.isAuthenticated(), "должен аутентифицироваться");
    assertEquals("john", auth.getName(), "имя в токене == username");
  }

  @Test
  void badPassword_throws() throws Exception {
    String hashed = new BCryptPasswordEncoder().encode("correct");
    User dbUser =
        User.builder()
            .username("john")
            .password(hashed)
            .roles(Set.of(Role.builder().name("USER").build()))
            .build();

    when(userService.findByUsername("john")).thenReturn(dbUser);
    var badToken = new UsernamePasswordAuthenticationToken("john", "wrong");
    assertThrows(
        BadCredentialsException.class,
        () -> provider.authenticate(badToken),
        "неправильный пароль -> BadCredentials");
  }
}
