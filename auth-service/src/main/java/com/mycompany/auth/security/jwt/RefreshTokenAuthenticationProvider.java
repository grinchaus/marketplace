package com.mycompany.auth.security.jwt;

import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.service.UserDetailsImpl;
import com.mycompany.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenAuthenticationProvider implements AuthenticationProvider {

  private final UserService userService;

  private final JwtTokenProvider tokenProvider;

  @Autowired
  public RefreshTokenAuthenticationProvider(
      final UserService userService, final JwtTokenProvider tokenProvider) {
    this.userService = userService;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {
    String token = (String) authentication.getCredentials();
    String username = tokenProvider.getUserNameFromJwtToken(token);
    UserDetailsImpl userDetails = getUserDetails(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private UserDetailsImpl getUserDetails(String username) {
    try {
      return UserDetailsImpl.build(userService.findByUsername(username));
    } catch (ServiceException e) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return (RefreshJwtAuthenticationToken.class.isAssignableFrom(authentication));
  }
}
