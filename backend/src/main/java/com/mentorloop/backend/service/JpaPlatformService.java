package com.mentorloop.backend.service;

import com.mentorloop.backend.entity.AccountEntity;
import com.mentorloop.backend.entity.MatchEntity;
import com.mentorloop.backend.entity.ProgressEntity;
import com.mentorloop.backend.entity.SessionEntity;
import com.mentorloop.backend.entity.UserProfileEntity;
import com.mentorloop.backend.repository.AccountRepository;
import com.mentorloop.backend.repository.MatchRepository;
import com.mentorloop.backend.repository.ProgressRepository;
import com.mentorloop.backend.repository.SessionRepository;
import com.mentorloop.backend.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JpaPlatformService {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final MatchRepository matchRepository;
    private final SessionRepository sessionRepository;
    private final ProgressRepository progressRepository;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public JpaPlatformService(
            AccountRepository accountRepository,
            UserProfileRepository userProfileRepository,
            MatchRepository matchRepository,
            SessionRepository sessionRepository,
            ProgressRepository progressRepository
    ) {
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.matchRepository = matchRepository;
        this.sessionRepository = sessionRepository;
        this.progressRepository = progressRepository;
    }

    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("timestamp", Instant.now().toString());
        response.put("database", "mysql");
        return response;
    }

    public Map<String, Object> login(Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        AccountEntity account = accountRepository.findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String token = "token-" + UUID.randomUUID();
        activeSessions.put(token, account.getId());
        return buildAuthPayload(account, token);
    }

    public Map<String, Object> me(String authorizationHeader) {
        AccountEntity account = requireAccount(authorizationHeader);
        return buildAuthPayload(account, extractToken(authorizationHeader));
    }

    public Map<String, Object> logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token != null) {
            activeSessions.remove(token);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    public Map<String, Object> dashboard(String authorizationHeader) {
        requireAccount(authorizationHeader);

        List<UserProfileEntity> mentors = sortUsersByName(userProfileRepository.findByRole("mentor"));
        List<UserProfileEntity> mentees = sortUsersByName(userProfileRepository.findByRole("mentee"));
        List<SessionEntity> allSessions = new ArrayList<>(sessionRepository.findAll());
        List<SessionEntity> upcomingSessions = allSessions.stream()
                .filter(session -> "scheduled".equalsIgnoreCase(session.getStatus()))
                .filter(session -> parseDateTime(session.getDateTime()).isAfter(Instant.now()))
                .sorted(Comparator.comparing(session -> parseDateTime(session.getDateTime())))
                .toList();
        List<SessionEntity> completedSessions = allSessions.stream()
                .filter(session -> "completed".equalsIgnoreCase(session.getStatus()))
                .toList();

        double mentorshipHours = completedSessions.stream()
                .mapToInt(session -> defaultInt(session.getDurationMinutes()))
                .sum() / 60.0;

        int averageProgress = mentees.isEmpty()
                ? 0
                : (int) Math.round(
                mentees.stream()
                        .mapToInt(mentee -> progressSummary(mentee.getId()).completionAverage())
                        .average()
                        .orElse(0)
        );

        Map<String, Integer> focusCounts = new LinkedHashMap<>();
        for (UserProfileEntity mentee : mentees) {
            for (String focus : safeList(mentee.getFocusAreas())) {
                focusCounts.put(focus, focusCounts.getOrDefault(focus, 0) + 1);
            }
        }

        List<Map<String, Object>> focusBreakdown = focusCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("label", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .toList();

        List<Map<String, Object>> recentSessions = upcomingSessions.stream()
                .limit(4)
                .map(this::sessionResponse)
                .toList();

        List<Map<String, Object>> featuredMatches = matchRepository.findAll().stream()
                .sorted(Comparator.comparing((MatchEntity match) -> parseDateTime(match.getCreatedAt())).reversed())
                .limit(4)
                .map(this::matchResponse)
                .toList();

        Map<String, Object> heroStats = new LinkedHashMap<>();
        heroStats.put("totalMentors", mentors.size());
        heroStats.put("totalMentees", mentees.size());
        heroStats.put("upcomingSessions", upcomingSessions.size());
        heroStats.put("averageProgress", averageProgress);
        heroStats.put("mentorshipHours", Math.round(mentorshipHours * 10.0) / 10.0);
        heroStats.put("satisfactionScore", 96);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("heroStats", heroStats);
        response.put("focusBreakdown", focusBreakdown);
        response.put("recentSessions", recentSessions);
        response.put("featuredMatches", featuredMatches);
        return response;
    }

    public List<Map<String, Object>> mentors(String authorizationHeader, String search, String expertise) {
        requireAccount(authorizationHeader);
        String safeSearch = search == null ? "" : search.toLowerCase();

        return sortUsersByName(userProfileRepository.findByRole("mentor")).stream()
                .filter(mentor -> {
                    String combined = String.join(" ",
                            safeString(mentor.getName()),
                            safeString(mentor.getTitle()),
                            safeString(mentor.getCompany()),
                            safeString(mentor.getBio()),
                            String.join(" ", safeList(mentor.getExpertise()))
                    ).toLowerCase();
                    boolean matchesSearch = safeSearch.isBlank() || combined.contains(safeSearch);
                    boolean matchesExpertise = expertise == null || expertise.isBlank() || "all".equalsIgnoreCase(expertise)
                            || safeList(mentor.getExpertise()).contains(expertise);
                    return matchesSearch && matchesExpertise;
                })
                .map(this::userResponse)
                .toList();
    }

    public List<Map<String, Object>> mentees(String authorizationHeader) {
        AccountEntity account = requireAccount(authorizationHeader);

        return sortUsersByName(userProfileRepository.findByRole("mentee")).stream()
                .filter(mentee -> canAccessMentee(account, mentee.getId()))
                .map(mentee -> {
                    Map<String, Object> response = new LinkedHashMap<>(userResponse(mentee));
                    response.put("mentor", userResponse(findUser(mentee.getCurrentMentorId())));
                    response.put("progress", progressSummaryMap(mentee.getId()));
                    return response;
                })
                .toList();
    }

    public List<Map<String, Object>> recommendations(String authorizationHeader, String menteeId) {
        AccountEntity account = requireAccount(authorizationHeader);
        String targetMenteeId = hasText(menteeId) ? menteeId : linkedUserId(account);
        validateMenteeAccess(account, targetMenteeId);
        UserProfileEntity mentee = requireMentee(targetMenteeId);

        List<Map<String, Object>> responses = new ArrayList<>();
        for (UserProfileEntity mentor : userProfileRepository.findByRole("mentor")) {
            List<String> overlap = safeList(mentor.getExpertise()).stream()
                    .filter(item -> safeList(mentee.getFocusAreas()).contains(item))
                    .toList();
            double loadFactor = 1 - (defaultInt(mentor.getCurrentMentees()) / (double) Math.max(defaultInt(mentor.getCapacity()), 1));
            int score = (int) Math.round(overlap.size() * 30 + loadFactor * 20 + defaultDouble(mentor.getRating()) * 10);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mentor", userResponse(mentor));
            item.put("overlap", overlap);
            item.put("score", score);
            responses.add(item);
        }

        responses.sort((left, right) -> Integer.compare((Integer) right.get("score"), (Integer) left.get("score")));
        return responses.stream().limit(3).toList();
    }
}
