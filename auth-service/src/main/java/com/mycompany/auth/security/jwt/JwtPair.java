package com.mycompany.auth.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPair {
  private String token;
  private String refreshToken;
}
