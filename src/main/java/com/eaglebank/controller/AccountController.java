package com.eaglebank.controller;

import com.eaglebank.model.Account;
import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

   //Fetch single account
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByIdAndUser(id,user)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(403).body("Forbidden: cannot access another user's account"));
    }

    //Update account (only owner)
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody Account updated, Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByIdAndUser(id,user).<ResponseEntity<?>>map(account -> {
            if (updated.getBalance() != null){
                account.setBalance(updated.getBalance());
            }
            accountRepository.save(account);
            return ResponseEntity.ok(account);
        }).orElseGet(() -> ResponseEntity.status(403).body("Forbidden: cannot update another user's account"));
    }

    //Delete account
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByIdAndUser(id,user).map(account -> {
            accountRepository.delete(account);
            return ResponseEntity.ok("Account deleted");
        }).orElse(ResponseEntity.status(403).body("Forbidden: cannot update another user's account"));
    }
}
