package com.mentorloop.backend.model;

import java.util.ArrayList;
import java.util.List;

public class PlatformData {

    private List<Account> accounts = new ArrayList<>();
    private List<UserProfile> users = new ArrayList<>();
    private List<MentorshipMatch> matches = new ArrayList<>();
    private List<MentorshipSession> sessions = new ArrayList<>();
    private List<ProgressMilestone> progress = new ArrayList<>();

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<UserProfile> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }

    public List<MentorshipMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<MentorshipMatch> matches) {
        this.matches = matches;
    }

    public List<MentorshipSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<MentorshipSession> sessions) {
        this.sessions = sessions;
    }

    public List<ProgressMilestone> getProgress() {
        return progress;
    }

    public void setProgress(List<ProgressMilestone> progress) {
        this.progress = progress;
    }

    public static class Account {
        private String id;
        private String role;
        private String username;
        private String password;
        private String displayName;
        private String linkedUserId;

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

        public String getLinkedUserId() {
            return linkedUserId;
        }

        public void setLinkedUserId(String linkedUserId) {
            this.linkedUserId = linkedUserId;
        }
    }

    public static class UserProfile {
        private String id;
        private String role;
        private String name;
        private String title;
        private String company;
        private String bio;
        private List<String> expertise = new ArrayList<>();
        private List<String> availability = new ArrayList<>();
        private Integer experienceYears;
        private Integer capacity;
        private Integer currentMentees;
        private Double rating;
        private String location;
        private List<String> goals = new ArrayList<>();
        private List<String> focusAreas = new ArrayList<>();
        private String currentMentorId;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public List<String> getExpertise() {
            return expertise;
        }

        public void setExpertise(List<String> expertise) {
            this.expertise = expertise;
        }

        public List<String> getAvailability() {
            return availability;
        }

        public void setAvailability(List<String> availability) {
            this.availability = availability;
        }

        public Integer getExperienceYears() {
            return experienceYears;
        }

        public void setExperienceYears(Integer experienceYears) {
            this.experienceYears = experienceYears;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public Integer getCurrentMentees() {
            return currentMentees;
        }

        public void setCurrentMentees(Integer currentMentees) {
            this.currentMentees = currentMentees;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public List<String> getGoals() {
            return goals;
        }

        public void setGoals(List<String> goals) {
            this.goals = goals;
        }

        public List<String> getFocusAreas() {
            return focusAreas;
        }

        public void setFocusAreas(List<String> focusAreas) {
            this.focusAreas = focusAreas;
        }

        public String getCurrentMentorId() {
            return currentMentorId;
        }

        public void setCurrentMentorId(String currentMentorId) {
            this.currentMentorId = currentMentorId;
        }
    }

    public static class MentorshipMatch {
        private String id;
        private String mentorId;
        private String menteeId;
        private String status;
        private String createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMentorId() {
            return mentorId;
        }

        public void setMentorId(String mentorId) {
            this.mentorId = mentorId;
        }

        public String getMenteeId() {
            return menteeId;
        }

        public void setMenteeId(String menteeId) {
            this.menteeId = menteeId;
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

    public static class MentorshipSession {
        private String id;
        private String mentorId;
        private String menteeId;
        private String topic;
        private String dateTime;
        private String mode;
        private Integer durationMinutes;
        private String status;
        private String notes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMentorId() {
            return mentorId;
        }

        public void setMentorId(String mentorId) {
            this.mentorId = mentorId;
        }

        public String getMenteeId() {
            return menteeId;
        }

        public void setMenteeId(String menteeId) {
            this.menteeId = menteeId;
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

    public static class ProgressMilestone {
        private String id;
        private String menteeId;
        private String title;
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

        public String getMenteeId() {
            return menteeId;
        }

        public void setMenteeId(String menteeId) {
            this.menteeId = menteeId;
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
}
