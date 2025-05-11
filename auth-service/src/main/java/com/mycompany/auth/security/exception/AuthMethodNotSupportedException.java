package com.mycompany.auth.security.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class AuthMethodNotSupportedException extends AuthenticationServiceException {
  public AuthMethodNotSupportedException(final String msg) {
    super(msg);
  }
}
