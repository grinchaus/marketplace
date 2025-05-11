package com.mycompany.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ERole {
  ADMIN("ADMIN"),
  USER("USER");
  final String name;
}
