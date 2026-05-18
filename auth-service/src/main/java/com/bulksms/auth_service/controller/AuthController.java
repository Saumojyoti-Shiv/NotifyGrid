package com.bulksms.auth_service.controller;

import com.bulksms.auth_service.model.User;
import com.bulksms.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (user.getRole() == null) user.setRole("USER");
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User credentials) {
        return userRepository.findByUsername(credentials.getUsername())
                .filter(u -> u.getPassword().equals(credentials.getPassword()))
                .map(u -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("token", "mock-jwt-token-for-" + u.getUsername());
                    response.put("username", u.getUsername());
                    response.put("role", u.getRole());
                    return ResponseEntity.ok((Object)response);
                })
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
}
