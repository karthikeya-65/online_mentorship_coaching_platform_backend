package com.mentorloop.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "progress")
public class ProgressEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentee_id")
    private UserProfileEntity mentee;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer completion;
    private String status;
    private String dueDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserProfileEntity getMentee() {
        return mentee;
    }

    public void setMentee(UserProfileEntity mentee) {
        this.mentee = mentee;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCompletion() {
        return completion;
    }

    public void setCompletion(Integer completion) {
        this.completion = completion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
