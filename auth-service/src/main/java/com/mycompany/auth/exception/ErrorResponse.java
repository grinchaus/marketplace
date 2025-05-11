package com.mycompany.auth.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  @NotBlank
  @Schema(description = "Error message", example = "Error message")
  private final String message;

  @NotBlank
  @Schema(
      description =
          "Error code: "
              + "1 - General error (HTTP: 500 - Internal Server Error), "
              + "2 - Authentication failed (HTTP: 401 - Unauthorized), "
              + "3 - JWT token expired (HTTP: 401 - Unauthorized), "
              + "10 - Bad request parameters (HTTP: 400 - Bad Request), "
              + "20 - Access denied (HTTP: 403 - Forbidden)",
      example = "2")
  private final ErrorCode code;

  @NotBlank
  @Schema(description = "Error code", example = "401")
  private final HttpStatus status;

  @NotBlank
  @Schema(description = "Error timestamp", example = "2021-08-25T15:00:00")
  private final Date timestamp;

  public static ErrorResponse of(final String message, final ErrorCode code) {
    return new ErrorResponse(message, code, code.getStatus(), new Date());
  }
}
