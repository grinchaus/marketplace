package com.mycompany.auth.security.jwt;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

class TokenAuthenticationFilterTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtTokenProvider tokenProvider;
  @Mock private AuthenticationFailureHandler failureHandler;
  @Mock private FilterChain chain;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private TokenAuthenticationFilter filter;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    filter =
        new TokenAuthenticationFilter(
            tokenProvider, new AntPathRequestMatcher("/**"), failureHandler);
    filter.setAuthenticationManager(authenticationManager);
  }

  @Test
  void skipWhenNoAuthHeader() throws Exception {
    when(tokenProvider.getTokenFromRequest(request))
        .thenThrow(new AuthenticationServiceException("Missing or invalid Authorization header"));
    filter.doFilter(request, response, chain);

    verify(failureHandler)
        .onAuthenticationFailure(
            eq(request), eq(response), any(AuthenticationServiceException.class));
    verifyNoInteractions(chain);
  }

  @Test
  void failureHandledWhenAuthThrows() throws Exception {
    when(tokenProvider.getTokenFromRequest(request)).thenReturn("some.token");
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

    filter.doFilter(request, response, chain);
    verify(failureHandler)
        .onAuthenticationFailure(eq(request), eq(response), any(BadCredentialsException.class));
    verifyNoInteractions(chain);
  }
}
