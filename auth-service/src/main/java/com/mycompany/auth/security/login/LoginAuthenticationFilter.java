package com.mycompany.auth.security.login;

import com.mycompany.auth.dto.SigninDto;
import com.mycompany.auth.security.exception.AuthMethodNotSupportedException;
import com.mycompany.auth.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;

  public LoginAuthenticationFilter(
      final String defaultFilterProcessesUrl,
      final AuthenticationSuccessHandler successHandler,
      final AuthenticationFailureHandler failureHandler) {
    super(defaultFilterProcessesUrl);
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
  }

  @Override
  public Authentication attemptAuthentication(
      final HttpServletRequest request, final HttpServletResponse response)
      throws AuthenticationException {
    if (!HttpMethod.POST.name().equals(request.getMethod())) {
      if (log.isDebugEnabled()) {
        log.debug("Authentication method not supported. Request method: {}", request.getMethod());
      }
      throw new AuthMethodNotSupportedException("Authentication method not supported");
    }

    SigninDto signinDto;
    try {
      signinDto = JsonUtils.fromReader(request.getReader(), SigninDto.class);
    } catch (Exception e) {
      throw new AuthenticationServiceException("Invalid login request payload");
    }

    assert signinDto != null;
    if (StringUtils.isBlank(signinDto.getUsername())
        || StringUtils.isEmpty(signinDto.getPassword())) {
      throw new AuthenticationServiceException("Username or Password not provided");
    }

    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(signinDto.getUsername(), signinDto.getPassword());
    token.setDetails(authenticationDetailsSource.buildDetails(request));
    return this.getAuthenticationManager().authenticate(token);
  }

  @Override
  protected void successfulAuthentication(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    this.successHandler.onAuthenticationSuccess(request, response, authResult);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    this.failureHandler.onAuthenticationFailure(request, response, failed);
  }
}
