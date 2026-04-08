package com.mentorloop.backend.repository;

import com.mentorloop.backend.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {
    List<UserProfileEntity> findByRole(String role);
}
