package com.mycompany.auth.exception;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  GENERAL(1, HttpStatus.INTERNAL_SERVER_ERROR),
  AUTHENTICATION(2, HttpStatus.UNAUTHORIZED),
  JWT_TOKEN_EXPIRED(3, HttpStatus.UNAUTHORIZED),
  BAD_REQUEST_PARAMS(10, HttpStatus.BAD_REQUEST),
  ACCESS_DENIED(20, HttpStatus.FORBIDDEN);

  private final int code;
  private final HttpStatus status;

  @JsonValue
  public int getCode() {
    return code;
  }
}
