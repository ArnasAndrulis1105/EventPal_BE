package org.example.eventpal.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginUserRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
