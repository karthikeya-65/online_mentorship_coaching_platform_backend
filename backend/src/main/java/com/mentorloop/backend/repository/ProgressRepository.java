package com.mentorloop.backend.repository;

import com.mentorloop.backend.entity.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepository extends JpaRepository<ProgressEntity, String> {
    List<ProgressEntity> findByMentee_Id(String menteeId);
}
