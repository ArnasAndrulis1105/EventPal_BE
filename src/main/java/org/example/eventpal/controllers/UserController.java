package org.example.eventpal.controllers;

import lombok.RequiredArgsConstructor;
import org.example.eventpal.dto.user.UpdateUserProfileRequest;
import org.example.eventpal.dto.user.UserProfileResponse;
import org.example.eventpal.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.loadUser(id));
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Long id, @RequestBody UpdateUserProfileRequest user){
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserProfileResponse> deleteUserProfile(@PathVariable Long id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal
    ) {
        return ResponseEntity.ok(userService.loadUserByEmail(principal.getUsername()));
    }

    @PutMapping(value = "/me", produces = "application/json")
    public ResponseEntity<UserProfileResponse> updateMe(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            @RequestBody UpdateUserProfileRequest req
    ) {
        return ResponseEntity.ok(userService.updateUserByEmail(principal.getUsername(), req));
    }
}
