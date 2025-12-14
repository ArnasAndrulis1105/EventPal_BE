package org.example.eventpal.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.Role;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private long id;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private Boolean active;
    private Date creationDate;
    private Date lastLoginDate;
    private Role role;
}
