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

    public RegisterResponse register(RegisterRequest registerRequest) {

        if (isUniqueUser(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + registerRequest.getEmail() + " already exists");
        }



        var user = User.builder()
                .name(registerRequest.getName())
                .username(registerRequest.getUsername())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(registerRequest.getRole())
                .build();
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getRole()
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
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
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
