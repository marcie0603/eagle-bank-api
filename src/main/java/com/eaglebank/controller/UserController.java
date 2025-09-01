package com.eaglebank.controller;

import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Username already in use");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already in use");
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }
}
