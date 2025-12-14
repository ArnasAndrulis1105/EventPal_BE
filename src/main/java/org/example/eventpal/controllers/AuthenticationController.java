package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import org.example.eventpal.dto.auth.LoginRequest;
import org.example.eventpal.dto.auth.LoginResponse;
import org.example.eventpal.dto.auth.RegisterRequest;
import org.example.eventpal.dto.auth.RegisterResponse;
import org.example.eventpal.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(authenticationService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(authenticationService.login(loginRequest), HttpStatus.OK);
    }
}
