package org.example.eventpal.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Surname is required")
    private String surname;

    @NotEmpty(message = "Username is required")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @Email(message = "Invalid email address")
    @NotEmpty(message = "Email is required")
    private String email;

    @NotEmpty(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "User must live on a planet")
    private Long planetId;
}
