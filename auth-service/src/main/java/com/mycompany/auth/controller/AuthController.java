package com.mycompany.auth.controller;

import com.mycompany.auth.dto.SignupDto;
import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;

  @Operation(summary = "User signup")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User is successfully signed up"),
        @ApiResponse(
            responseCode = "400",
            description = "One of Username already exists or Email already exists")
      })
  @PostMapping("/signup")
  public ResponseEntity<SignupDto> registerUser(@RequestBody final SignupDto signUpRequest)
      throws ServiceException {
    return ResponseEntity.ok(userService.addUser(signUpRequest));
  }

  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Number of users in the system"),
        @ApiResponse(responseCode = "400", description = "Error while getting number of users")
      })
  @GetMapping("/users/count")
  public ResponseEntity<Long> getUsersCount() {
    long count = userService.getUsersCount();
    return ResponseEntity.ok(count);
  }
}
