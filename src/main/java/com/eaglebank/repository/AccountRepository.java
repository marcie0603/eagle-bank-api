package com.eaglebank.repository;

import com.eaglebank.model.Account;
import com.eaglebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(User user);
    Optional<Account> findByIdAndUser(Long id, User user);
}
