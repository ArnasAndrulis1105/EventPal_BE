package org.example.eventpal.dto.auth;

import lombok.*;
import org.example.eventpal.enumerators.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private String profilePicture;
    private boolean active;
}
