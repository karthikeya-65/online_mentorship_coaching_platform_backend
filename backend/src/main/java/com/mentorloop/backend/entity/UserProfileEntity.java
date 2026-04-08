package com.mentorloop.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String name;

    private String title;
    private String company;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mentor_expertise", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "expertise")
    private List<String> expertise = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_availability", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "availability_slot")
    private List<String> availability = new ArrayList<>();

    private Integer experienceYears;
    private Integer capacity;
    private Integer currentMentees;
    private Double rating;
    private String location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mentee_goals", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "goal")
    private List<String> goals = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mentee_focus_areas", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "focus_area")
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
