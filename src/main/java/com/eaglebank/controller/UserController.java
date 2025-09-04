package com.eaglebank.controller;

import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public UserController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
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

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, Authentication authentication) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String usernameFromToken = authentication.getName();
        if (!user.get().getUsername().equals(usernameFromToken)) {
            return ResponseEntity.status(403).body("Forbidden: cannot access another user's details");
        }
        return ResponseEntity.ok(user.get());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated, Authentication authentication) {
        var userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();

        String usernameFromToken = authentication.getName();
        if (!user.getUsername().equals(usernameFromToken)) {
            return ResponseEntity.status(403).body("Forbidden: cannot access another user's details");
        }

        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getPassword() != null) {
            user.setPassword(new BCryptPasswordEncoder().encode(updated.getPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        var userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();

        String usernameFromToken = authentication.getName();
        if (!user.getUsername().equals(usernameFromToken)) {
            return ResponseEntity.status(403).body("Forbidden: cannot delete another user");
        }

        if (!accountRepository.findByUser(user).isEmpty()) {
            return ResponseEntity.status(409).body("Conflict: user has active bank accounts");
        }

        userRepository.delete(userOpt.get());
        return ResponseEntity.ok("User deleted");
    }
}
