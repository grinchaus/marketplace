package com.mycompany.auth.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceException extends Exception {
  private ErrorCode errorCode;

  public ServiceException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public ServiceException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
