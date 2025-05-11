package com.mycompany.user.controller;

import com.mycompany.user.dto.UserDto;
import com.mycompany.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserDto userDto) {
    UserDto registeredUser = userService.registerUser(userDto);
    return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable Long id, @RequestBody @Valid UserDto userDto) {
    UserDto updatedUser = userService.updateUser(id, userDto);
    return ResponseEntity.ok(updatedUser);
  }
}
