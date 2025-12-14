package org.example.eventpal.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.Role;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterUserRequest {
    @NotBlank
    private String name;
    @NotBlank private String username;
    @Email
    private String email;
    @Pattern(regexp = "^[+0-9]{7,20}$") private String phoneNumber;
    @Size(min = 8, max = 100) private String password;
    private String profilePicture;
    private Role role;
}
