package com.mycompany.auth.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.auth.security.exception.ExpiredTokenException;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtTokenProviderTest {

  private JwtTokenProvider provider;
  private UserDetailsService userDetailsService;

  @BeforeEach
  void setUp() throws Exception {
    KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();
    RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();

    userDetailsService = mock(UserDetailsService.class);
    provider = new JwtTokenProvider(userDetailsService, privateKey, publicKey);
    setValidity("accessTokenValiditySeconds", 60L);
    setValidity("refreshTokenValiditySeconds", 120L);
  }

  private void setValidity(String fieldName, long value) throws Exception {
    Field field = JwtTokenProvider.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.setLong(provider, value);
  }

  @Test
  void generateAndValidatePair() {
    UserDetails john =
        User.withUsername("john")
            .password("irrelevant")
            .authorities(Collections.emptyList())
            .build();
    when(userDetailsService.loadUserByUsername("john")).thenReturn(john);

    JwtPair pair = provider.generateTokenPair(john);
    assertNotNull(pair.getToken(), "token mustn't be null");
    assertNotNull(pair.getRefreshToken(), "refresh token mustn't be null");
    assertTrue(provider.validateToken(pair.getToken()), "token is valid");
    assertTrue(provider.validateToken(pair.getRefreshToken()), "refresh token is valid");

    String sub = provider.getUserNameFromJwtToken(pair.getToken());
    assertEquals("john", sub);
    UserDetails parsed = provider.parseJwtToken(pair.getToken());
    assertEquals("john", parsed.getUsername());
    verify(userDetailsService).loadUserByUsername("john");
  }

  @Test
  void expiredAccessTokenThrows() throws Exception {
    setValidity("accessTokenValiditySeconds", 1L);
    setValidity("refreshTokenValiditySeconds", 2L);
    UserDetails john =
        User.withUsername("john")
            .password("irrelevant")
            .authorities(Collections.emptyList())
            .build();
    when(userDetailsService.loadUserByUsername("john")).thenReturn(john);

    JwtPair pair = provider.generateTokenPair(john);
    Thread.sleep(1100);
    assertThrows(
        ExpiredTokenException.class,
        () -> provider.validateToken(pair.getToken()),
        "Expired token should cause ExpiredTokenException");
  }
}
