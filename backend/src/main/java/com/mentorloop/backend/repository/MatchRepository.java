package com.mentorloop.backend.repository;

import com.mentorloop.backend.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, String> {
    List<MatchEntity> findByMentee_Id(String menteeId);
}
