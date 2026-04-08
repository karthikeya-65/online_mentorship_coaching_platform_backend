package com.mentorloop.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorloop.backend.entity.AccountEntity;
import com.mentorloop.backend.entity.MatchEntity;
import com.mentorloop.backend.entity.ProgressEntity;
import com.mentorloop.backend.entity.SessionEntity;
import com.mentorloop.backend.entity.UserProfileEntity;
import com.mentorloop.backend.model.PlatformData;
import com.mentorloop.backend.repository.AccountRepository;
import com.mentorloop.backend.repository.MatchRepository;
import com.mentorloop.backend.repository.ProgressRepository;
import com.mentorloop.backend.repository.SessionRepository;
import com.mentorloop.backend.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            ObjectMapper objectMapper,
            UserProfileRepository userProfileRepository,
            AccountRepository accountRepository,
            MatchRepository matchRepository,
            SessionRepository sessionRepository,
            ProgressRepository progressRepository
    ) {
        return args -> {
            if (userProfileRepository.count() > 0) {
                return;
            }

            try (InputStream inputStream = new ClassPathResource("seed-data.json").getInputStream()) {
                PlatformData seed = objectMapper.readValue(inputStream, PlatformData.class);

                for (PlatformData.UserProfile source : seed.getUsers()) {
                    UserProfileEntity target = new UserProfileEntity();
                    target.setId(source.getId());
                    target.setRole(source.getRole());
                    target.setName(source.getName());
                    target.setTitle(source.getTitle());
                    target.setCompany(source.getCompany());
                    target.setBio(source.getBio());
                    target.setExpertise(List.copyOf(source.getExpertise()));
                    target.setAvailability(List.copyOf(source.getAvailability()));
                    target.setExperienceYears(source.getExperienceYears());
                    target.setCapacity(source.getCapacity());
                    target.setCurrentMentees(source.getCurrentMentees());
                    target.setRating(source.getRating());
                    target.setLocation(source.getLocation());
                    target.setGoals(List.copyOf(source.getGoals()));
                    target.setFocusAreas(List.copyOf(source.getFocusAreas()));
                    target.setCurrentMentorId(source.getCurrentMentorId());
                    userProfileRepository.save(target);
                }

                for (PlatformData.Account source : seed.getAccounts()) {
                    AccountEntity target = new AccountEntity();
                    target.setId(source.getId());
                    target.setRole(source.getRole());
                    target.setUsername(source.getUsername());
                    target.setPassword(source.getPassword());
                    target.setDisplayName(source.getDisplayName());
                    if (source.getLinkedUserId() != null) {
                        target.setLinkedUser(userProfileRepository.findById(source.getLinkedUserId()).orElse(null));
                    }
                    accountRepository.save(target);
                }

                for (PlatformData.MentorshipMatch source : seed.getMatches()) {
                    MatchEntity target = new MatchEntity();
                    target.setId(source.getId());
                    target.setMentor(userProfileRepository.findById(source.getMentorId()).orElse(null));
                    target.setMentee(userProfileRepository.findById(source.getMenteeId()).orElse(null));
                    target.setStatus(source.getStatus());
                    target.setCreatedAt(source.getCreatedAt());
                    matchRepository.save(target);
                }

                for (PlatformData.MentorshipSession source : seed.getSessions()) {
                    SessionEntity target = new SessionEntity();
                    target.setId(source.getId());
                    target.setMentor(userProfileRepository.findById(source.getMentorId()).orElse(null));
                    target.setMentee(userProfileRepository.findById(source.getMenteeId()).orElse(null));
                    target.setTopic(source.getTopic());
                    target.setDateTime(source.getDateTime());
                    target.setMode(source.getMode());
                    target.setDurationMinutes(source.getDurationMinutes());
                    target.setStatus(source.getStatus());
                    target.setNotes(source.getNotes());
                    sessionRepository.save(target);
                }

                for (PlatformData.ProgressMilestone source : seed.getProgress()) {
                    ProgressEntity target = new ProgressEntity();
                    target.setId(source.getId());
                    target.setMentee(userProfileRepository.findById(source.getMenteeId()).orElse(null));
                    target.setTitle(source.getTitle());
                    target.setDescription(source.getDescription());
                    target.setCompletion(source.getCompletion());
                    target.setStatus(source.getStatus());
                    target.setDueDate(source.getDueDate());
                    progressRepository.save(target);
                }
            }
        };
    }
}
