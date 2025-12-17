package org.example.eventpal.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.Role;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email address")
    public String email;

    @Column(nullable = false,  unique = true)
    public String phoneNumber;

    @Column(nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String profilePicture;

    @Column(nullable = false)
    public Boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date creationDate;

    @Column(nullable = false)
    @UpdateTimestamp
    private Date lastLoginDate;

    @Enumerated(EnumType.STRING)
    public Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
