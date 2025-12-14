package org.example.eventpal.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    public Long id;
    private String name;
    private String username;
    @Email
    private String email;
    @Pattern(regexp = "^[+0-9]{7,20}$") private String phoneNumber;
    private String profilePicture;
}
