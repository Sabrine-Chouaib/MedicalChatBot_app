package com.example.medical_chatbot.controller;


import com.example.medical_chatbot.config.JwtUtil;
import com.example.medical_chatbot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        authService.register(request.get("username"), request.get("password"));
        return ResponseEntity.ok("Utilisateur enregistré avec succès");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        if (authService.authenticate(request.get("username"), request.get("password"))) {
            String token = jwtUtil.generateToken(request.get("username"));
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
        }
    }
}

