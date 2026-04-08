package com.mentorloop.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentor_id")
    private UserProfileEntity mentor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentee_id")
    private UserProfileEntity mentee;

    private String status;
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserProfileEntity getMentor() {
        return mentor;
    }

    public void setMentor(UserProfileEntity mentor) {
        this.mentor = mentor;
    }

    public UserProfileEntity getMentee() {
        return mentee;
    }

    public void setMentee(UserProfileEntity mentee) {
        this.mentee = mentee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
