package com.mentorloop.backend.repository;

import com.mentorloop.backend.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByUsernameAndPassword(String username, String password);
}
