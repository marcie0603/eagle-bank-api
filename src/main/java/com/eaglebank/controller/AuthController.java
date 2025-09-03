package com.eaglebank.controller;

import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        return userRepository.findByUsername(loginRequest.getUsername())
                .map(user -> {
                    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        return ResponseEntity.status(401).body("Invalis username or password");
                    }
                    String token = jwtUtil.generateToken(user.getUsername());
                    return ResponseEntity.ok("Bearer " + token);
                }).orElse(ResponseEntity.status(401).body("Invalid username or password"));
    }
}
