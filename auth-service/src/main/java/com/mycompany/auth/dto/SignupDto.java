package com.mycompany.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupDto {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20)
  private String username;

  @NotBlank(message = "Email is required")
  @Size(max = 50)
  @Email(message = "Invalid email address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 40)
  private String password;

  @NotEmpty(message = "Role is required")
  private Set<String> roles;
}
