package com.mycompany.auth.mapper;

import com.mycompany.auth.dto.SignupDto;
import com.mycompany.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public User fromDto(SignupDto dto) {
    return new User(dto.getUsername(), dto.getEmail(), dto.getPassword());
  }
}
