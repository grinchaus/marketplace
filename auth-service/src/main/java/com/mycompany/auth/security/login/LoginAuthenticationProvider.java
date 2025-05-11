package com.mycompany.auth.security.login;

import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.model.User;
import com.mycompany.auth.service.UserDetailsImpl;
import com.mycompany.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LoginAuthenticationProvider implements AuthenticationProvider {

  private final UserService userService;
  private final PasswordEncoder encoder;

  @Autowired
  public LoginAuthenticationProvider(final UserService userService) {
    this.userService = userService;
    this.encoder = new BCryptPasswordEncoder();
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {
    Assert.notNull(authentication, "No authentication data provided");
    String username = (String) authentication.getPrincipal();
    String password = authentication.getCredentials().toString();
    UserDetails securityUser = authenticateByUsernameAndPassword(username, password);
    return new UsernamePasswordAuthenticationToken(
        securityUser, null, securityUser.getAuthorities());
  }

  private UserDetails authenticateByUsernameAndPassword(
      final String username, final String password) {
    User user = getUser(username);
    if (!encoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("exception.badCredentials");
    }
    return UserDetailsImpl.build(user);
  }

  private User getUser(final String username) {
    try {
      return userService.findByUsername(username);
    } catch (ServiceException e) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
  }
}
