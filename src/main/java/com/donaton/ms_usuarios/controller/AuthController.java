package com.donaton.ms_usuarios.controller;

import com.donaton.ms_usuarios.dto.AuthResponse;
import com.donaton.ms_usuarios.dto.LoginRequest;
import com.donaton.ms_usuarios.dto.RegisterRequest;
import com.donaton.ms_usuarios.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            AuthResponse response = userService.register(req);
            return ResponseEntity.ok(response);
        } catch (UserService.EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            AuthResponse response = userService.login(req);
            return ResponseEntity.ok(response);
        } catch (UserService.InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
