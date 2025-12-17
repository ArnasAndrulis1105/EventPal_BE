package org.example.eventpal.services;

import io.jsonwebtoken.Claims;
import org.example.eventpal.dto.auth.LoginRequest;
import org.example.eventpal.dto.auth.LoginResponse;
import org.example.eventpal.dto.auth.RegisterRequest;
import org.example.eventpal.dto.auth.RegisterResponse;
import org.example.eventpal.entities.Token;
import org.example.eventpal.entities.User;
import org.example.eventpal.enumerators.TokenType;
import org.example.eventpal.exceptions.authentication.EmailAlreadyExistsException;
import org.example.eventpal.repositories.TokenRepository;
import org.example.eventpal.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + request.getEmail() + " already exists");
        }

        // Default profile picture if not provided
        String profilePicture = request.getProfilePicture();
        if (profilePicture == null || profilePicture.isBlank()) {
            profilePicture = "https://api.dicebear.com/7.x/identicon/png?seed=" +
                    URLEncoder.encode(request.getEmail(), StandardCharsets.UTF_8);
        }

        Date now = new Date();

        // IMPORTANT:
        // Your User entity currently does NOT have "surname".
        // If you want to persist it, add a `surname` field to User + DB column.
        var user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .profilePicture(profilePicture)
                .active(true)           // fixes NOT NULL
                .creationDate(now)      // fixes NOT NULL
                .lastLoginDate(now)     // fixes NOT NULL (you can update on login later)
                .role(request.getRole())
                .build();



        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                request.getName(),
                request.getSurname(), // returned even if not stored, unless you add it to User
                saved.getUsername(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getProfilePicture(),
                Boolean.TRUE.equals(saved.getActive())
        );
    }

    public LoginResponse login(LoginRequest loginRequest) {

        User user = selectUser(loginRequest);

        userCredentialsMatch(loginRequest, user);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        var jwtToken = jwtService.generateToken(user);

        revokeAllUserTokens(user);
        saveToken(jwtToken, user, TokenType.BEARER);

        return new LoginResponse(jwtToken);
    }

    private void userCredentialsMatch(LoginRequest loginRequest, User user) {
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect password");
        }
    }

    private User selectUser(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email not found"));
    }

    private Boolean isUniqueUser(String email) {
        return userRepository.existsByEmail(email);
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokensOfUser(user.getId());

        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private void saveToken(String jwtToken, User user, TokenType tokenType) {
        Token token = Token.builder()
                .token(jwtToken)
                .tokenType(tokenType)
                .expiryDate(jwtService.extractClaim(jwtToken, Claims::getExpiration))
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(token);
    }
}
