package com.mycompany.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {

  @GetMapping("/protected")
  public String protectedEndpoint() {
    return "protected data";
  }
}
