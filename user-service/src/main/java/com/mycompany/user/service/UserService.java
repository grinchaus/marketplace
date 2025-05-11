package com.mycompany.user.service;

import com.mycompany.user.dto.UserDto;

public interface UserService {
  UserDto registerUser(UserDto userDto);

  UserDto updateUser(Long id, UserDto userDto);
}
