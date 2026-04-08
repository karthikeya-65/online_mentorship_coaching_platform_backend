package com.mentorloop.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentor_id")
    private UserProfileEntity mentor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentee_id")
    private UserProfileEntity mentee;

    private String topic;
    private String dateTime;
    private String mode;
    private Integer durationMinutes;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
