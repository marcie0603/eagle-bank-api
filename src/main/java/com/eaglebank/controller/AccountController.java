package com.eaglebank.controller;

import com.eaglebank.model.Account;
import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    //Create account
    @PostMapping
    public ResponseEntity<?> createAccount(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Account account = Account.builder()
                .accountNumber(UUID.randomUUID().toString())
                .balance(0.0)
                .user(user)
                .build();

        Account saved = accountRepository.save(account);
        return ResponseEntity.ok(saved);
    }

    //List all accounts for thr logged-in user
    @GetMapping
    public ResponseEntity<?> listAccounts(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountRepository.findByUser(user);
        return ResponseEntity.ok(accounts);
    }
}
