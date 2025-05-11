package com.mycompany.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto {
  private Long id;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email address")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Company name is required")
  private String companyName;
}
