package com.eaglebank.controller;

import com.eaglebank.model.Account;
import com.eaglebank.model.Transaction;
import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.TransactionRepository;
import com.eaglebank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/v1/accounts/{accountId}/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionRepository transactionRepository, AccountRepository accountRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    //create transaction( deposit or withdrawal)
    @PostMapping
    public ResponseEntity<?> createTransaction(@PathVariable Long accountId, @RequestBody Transaction txRequest, Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(403).body("Forbidden: cannot access another user's account");
        }

        if (txRequest.getAmount() == null || txRequest.getAmount() <= 0 || txRequest.getType() == null) {
            return ResponseEntity.badRequest().body("Missing or invalid transaction data");
        }

        if (txRequest.getType().equalsIgnoreCase("withdrawal")) {
            if (account.getBalance() < txRequest.getAmount()) {
                return ResponseEntity.unprocessableEntity().body("Insufficient funds");
            }
            account.setBalance(account.getBalance() - txRequest.getAmount());
        } else if (txRequest.getType().equalsIgnoreCase("deposit")) {
            account.setBalance(account.getBalance() + txRequest.getAmount());
        } else {
            return ResponseEntity.badRequest().body("Invalid transaction type");
        }

        Account updatedAccount = accountRepository.save(account);

        Transaction savedTx = Transaction.builder()
                .type(txRequest.getType().toLowerCase())
                .amount(txRequest.getAmount())
                .timestamp(LocalDateTime.now())
                .account(account)
                .build();

        accountRepository.save(account);
        transactionRepository.save(savedTx);

        return ResponseEntity.ok(
                new Object() {
                    public final Transaction transaction = savedTx;
                    public final Account account = updatedAccount;
                }
        );
    }

    //List all transaction for this account
    @GetMapping
    public ResponseEntity<?> listTransactions(@PathVariable Long accountId, Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(403).body("Forbidden: cannot access another user's account");
        }

        List<Transaction> transactions = transactionRepository.findByAccount(account);
        return ResponseEntity.ok(transactions);
    }

    //Fetch a single transaction
    @GetMapping("/{transactionID}")
    public ResponseEntity<?> getTransaction(@PathVariable Long accountId, @PathVariable Long transactionId, Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(403).body("Forbidden: cannot access another user's account");
        }

        return transactionRepository.findByIdAndAccount(transactionId, account)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
