package com.mentorloop.backend.repository;

import com.mentorloop.backend.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    List<SessionEntity> findByMentee_Id(String menteeId);
}
