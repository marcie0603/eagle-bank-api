package com.eaglebank.repository;

import com.eaglebank.model.Account;
import com.eaglebank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    Optional<Transaction> findByIdAndAccount(Long id, Account account);
}
