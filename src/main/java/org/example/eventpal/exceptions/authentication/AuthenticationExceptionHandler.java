package org.example.eventpal.exceptions.authentication;

import org.example.eventpal.controllers.AuthenticationController;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(assignableTypes = {AuthenticationController.class})
@Order(1)
public class AuthenticationExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Bad credentials");
        problemDetail.setType(URI.create("http://datatracker.ietf.org/doc/html/rfc7235#section-3.1"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Email already exists");
        problemDetail.setType(URI.create("https://datatracker.ietf.org/doc/html/rfc7231#section-6.5.8"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }
}
