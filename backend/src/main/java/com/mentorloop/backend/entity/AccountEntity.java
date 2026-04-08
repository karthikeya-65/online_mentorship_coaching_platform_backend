package com.mentorloop.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private String id;

    private String role;
    private String username;
    private String password;
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linked_user_id")
    private UserProfileEntity linkedUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UserProfileEntity getLinkedUser() {
        return linkedUser;
    }

    public void setLinkedUser(UserProfileEntity linkedUser) {
        this.linkedUser = linkedUser;
    }
}
