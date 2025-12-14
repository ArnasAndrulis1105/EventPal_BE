package org.example.eventpal.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;

    private String name;

    private String username;

    private String email;

    private String phoneNumber;

    private Role role;
}
