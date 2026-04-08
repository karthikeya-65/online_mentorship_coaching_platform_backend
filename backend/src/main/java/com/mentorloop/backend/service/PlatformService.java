package com.mentorloop.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorloop.backend.model.PlatformData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PlatformService {

    private final ObjectMapper objectMapper;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private PlatformData store;

    public PlatformService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.store = loadSeedData();
    }

    public synchronized Map<String, Object> login(Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        PlatformData.Account account = store.getAccounts().stream()
                .filter(item -> Objects.equals(item.getUsername(), username) && Objects.equals(item.getPassword(), password))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String token = "token-" + UUID.randomUUID();
        activeSessions.put(token, account.getId());
        return buildAuthPayload(account, token);
    }

    public synchronized Map<String, Object> me(String authorizationHeader) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        return buildAuthPayload(account, extractToken(authorizationHeader));
    }

    public synchronized Map<String, Object> logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token != null) {
            activeSessions.remove(token);
        }
        return Map.of("success", true);
    }

    public synchronized Map<String, Object> health() {
        return Map.of("status", "ok", "timestamp", Instant.now().toString());
    }

    public synchronized Map<String, Object> dashboard(String authorizationHeader) {
        requireAccount(authorizationHeader);

        List<PlatformData.UserProfile> mentors = getUsersByRole("mentor");
        List<PlatformData.UserProfile> mentees = getUsersByRole("mentee");
        List<PlatformData.MentorshipSession> upcomingSessions = store.getSessions().stream()
                .filter(session -> "scheduled".equalsIgnoreCase(session.getStatus()))
                .filter(session -> Instant.parse(session.getDateTime()).isAfter(Instant.now()))
                .sorted(Comparator.comparing(PlatformData.MentorshipSession::getDateTime))
                .toList();

        List<PlatformData.MentorshipSession> completedSessions = store.getSessions().stream()
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
        for (PlatformData.UserProfile mentee : mentees) {
            for (String focus : safeList(mentee.getFocusAreas())) {
                focusCounts.put(focus, focusCounts.getOrDefault(focus, 0) + 1);
            }
        }

        List<Map<String, Object>> focusBreakdown = focusCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> Map.<String, Object>of("label", entry.getKey(), "count", entry.getValue()))
                .toList();

        List<Map<String, Object>> recentSessions = upcomingSessions.stream()
                .limit(4)
                .map(this::sessionResponse)
                .toList();

        List<Map<String, Object>> featuredMatches = store.getMatches().stream()
                .sorted(Comparator.comparing(PlatformData.MentorshipMatch::getCreatedAt).reversed())
                .limit(4)
                .map(this::matchResponse)
                .toList();

        return Map.of(
                "heroStats", Map.of(
                        "totalMentors", mentors.size(),
                        "totalMentees", mentees.size(),
                        "upcomingSessions", upcomingSessions.size(),
                        "averageProgress", averageProgress,
                        "mentorshipHours", Math.round(mentorshipHours * 10.0) / 10.0,
                        "satisfactionScore", 96
                ),
                "focusBreakdown", focusBreakdown,
                "recentSessions", recentSessions,
                "featuredMatches", featuredMatches
        );
    }

    public synchronized List<Map<String, Object>> mentors(String authorizationHeader, String search, String expertise) {
        requireAccount(authorizationHeader);
        String safeSearch = search == null ? "" : search.toLowerCase();

        return getUsersByRole("mentor").stream()
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

    public synchronized List<Map<String, Object>> mentees(String authorizationHeader) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        return getUsersByRole("mentee").stream()
                .filter(mentee -> canAccessMentee(account, mentee.getId()))
                .map(mentee -> {
                    Map<String, Object> response = new LinkedHashMap<>(userResponse(mentee));
                    response.put("mentor", userResponse(findUser(mentee.getCurrentMentorId())));
                    response.put("progress", progressSummaryMap(mentee.getId()));
                    return response;
                })
                .toList();
    }

    public synchronized List<Map<String, Object>> recommendations(String authorizationHeader, String menteeId) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String targetMenteeId = menteeId != null && !menteeId.isBlank() ? menteeId : account.getLinkedUserId();
        validateMenteeAccess(account, targetMenteeId);
        PlatformData.UserProfile mentee = findUser(targetMenteeId);

        return getUsersByRole("mentor").stream()
                .map(mentor -> {
                    List<String> overlap = safeList(mentor.getExpertise()).stream()
                            .filter(item -> safeList(mentee.getFocusAreas()).contains(item))
                            .toList();
                    double loadFactor = 1 - (defaultInt(mentor.getCurrentMentees()) / (double) Math.max(defaultInt(mentor.getCapacity()), 1));
                    int score = (int) Math.round(overlap.size() * 30 + loadFactor * 20 + defaultDouble(mentor.getRating()) * 10);

                    return Map.<String, Object>of(
                            "mentor", userResponse(mentor),
                            "overlap", overlap,
                            "score", score
                    );
                })
                .sorted((left, right) -> Integer.compare((Integer) right.get("score"), (Integer) left.get("score")))
                .limit(3)
                .toList();
    }

    public synchronized List<Map<String, Object>> matches(String authorizationHeader) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        return store.getMatches().stream()
                .filter(match -> "admin".equals(account.getRole()) || Objects.equals(match.getMenteeId(), account.getLinkedUserId()))
                .map(this::matchResponse)
                .toList();
    }

    public synchronized List<Map<String, Object>> sessions(String authorizationHeader, String menteeId, String mentorId) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String effectiveMenteeId = "admin".equals(account.getRole()) ? menteeId : account.getLinkedUserId();
        if (effectiveMenteeId != null) {
            validateMenteeAccess(account, effectiveMenteeId);
        }

        return store.getSessions().stream()
                .filter(session -> effectiveMenteeId == null || Objects.equals(session.getMenteeId(), effectiveMenteeId))
                .filter(session -> mentorId == null || mentorId.isBlank() || Objects.equals(session.getMentorId(), mentorId))
                .sorted(Comparator.comparing(PlatformData.MentorshipSession::getDateTime))
                .map(this::sessionResponse)
                .toList();
    }

    public synchronized List<Map<String, Object>> progress(String authorizationHeader, String menteeId) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String targetMenteeId = menteeId != null && !menteeId.isBlank() ? menteeId : account.getLinkedUserId();
        validateMenteeAccess(account, targetMenteeId);

        return store.getProgress().stream()
                .filter(item -> Objects.equals(item.getMenteeId(), targetMenteeId))
                .sorted(Comparator.comparing(PlatformData.ProgressMilestone::getCompletion).reversed())
                .map(this::progressResponse)
                .toList();
    }

    public synchronized Map<String, Object> createMatch(String authorizationHeader, Map<String, Object> payload) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String mentorId = asString(payload.get("mentorId"));
        String menteeId = asString(payload.get("menteeId"));
        if (menteeId == null || menteeId.isBlank()) {
            menteeId = account.getLinkedUserId();
        }

        validateMenteeAccess(account, menteeId);
        PlatformData.UserProfile mentor = requireMentor(mentorId);
        PlatformData.UserProfile mentee = requireMentee(menteeId);

        PlatformData.UserProfile previousMentor = findUser(mentee.getCurrentMentorId());
        if (previousMentor != null && !Objects.equals(previousMentor.getId(), mentor.getId())) {
            previousMentor.setCurrentMentees(Math.max(defaultInt(previousMentor.getCurrentMentees()) - 1, 0));
            for (PlatformData.MentorshipMatch match : store.getMatches()) {
                if (Objects.equals(match.getMenteeId(), mentee.getId()) && "active".equalsIgnoreCase(match.getStatus())) {
                    match.setStatus("reassigned");
                }
            }
        }

        boolean alreadyActive = store.getMatches().stream()
                .anyMatch(match -> Objects.equals(match.getMentorId(), mentor.getId())
                        && Objects.equals(match.getMenteeId(), mentee.getId())
                        && "active".equalsIgnoreCase(match.getStatus()));

        if (!alreadyActive) {
            PlatformData.MentorshipMatch match = new PlatformData.MentorshipMatch();
            match.setId(newId("match"));
            match.setMentorId(mentor.getId());
            match.setMenteeId(mentee.getId());
            match.setStatus("active");
            match.setCreatedAt(Instant.now().toString());
            store.getMatches().add(match);
            mentor.setCurrentMentees(Math.min(defaultInt(mentor.getCurrentMentees()) + 1, defaultInt(mentor.getCapacity())));
        }

        mentee.setCurrentMentorId(mentor.getId());
        return Map.of("success", true);
    }

    public synchronized Map<String, Object> createSession(String authorizationHeader, Map<String, Object> payload) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String mentorId = asString(payload.get("mentorId"));
        String menteeId = asString(payload.get("menteeId"));
        String topic = asString(payload.get("topic"));
        String dateTime = asString(payload.get("dateTime"));
        String mode = asString(payload.get("mode"));

        if (menteeId == null || menteeId.isBlank()) {
            menteeId = account.getLinkedUserId();
        }
        validateMenteeAccess(account, menteeId);

        if (mentorId == null || topic == null || dateTime == null || mode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required session fields");
        }

        requireMentor(mentorId);
        requireMentee(menteeId);

        PlatformData.MentorshipSession session = new PlatformData.MentorshipSession();
        session.setId(newId("session"));
        session.setMentorId(mentorId);
        session.setMenteeId(menteeId);
        session.setTopic(topic);
        session.setDateTime(dateTime);
        session.setMode(mode);
        session.setDurationMinutes(60);
        session.setStatus("scheduled");
        session.setNotes("New session scheduled from the React dashboard.");
        store.getSessions().add(session);
        return Map.of("success", true);
    }

    public synchronized Map<String, Object> updateSession(String authorizationHeader, String sessionId, Map<String, Object> payload) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        PlatformData.MentorshipSession session = store.getSessions().stream()
                .filter(item -> Objects.equals(item.getId(), sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        validateMenteeAccess(account, session.getMenteeId());
        String status = asString(payload.get("status"));
        if (status != null && !"admin".equals(account.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update session status");
        }

        if (status != null) {
            session.setStatus(status);
        }
        if (asString(payload.get("dateTime")) != null) {
            session.setDateTime(asString(payload.get("dateTime")));
        }
        if (asString(payload.get("notes")) != null) {
            session.setNotes(asString(payload.get("notes")));
        }

        return Map.of("success", true);
    }

    public synchronized Map<String, Object> createProgress(String authorizationHeader, Map<String, Object> payload) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        String menteeId = asString(payload.get("menteeId"));
        String title = asString(payload.get("title"));
        String description = asString(payload.get("description"));
        String dueDate = asString(payload.get("dueDate"));

        if (menteeId == null || menteeId.isBlank()) {
            menteeId = account.getLinkedUserId();
        }
        validateMenteeAccess(account, menteeId);

        if (title == null || description == null || dueDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required milestone fields");
        }

        requireMentee(menteeId);

        PlatformData.ProgressMilestone milestone = new PlatformData.ProgressMilestone();
        milestone.setId(newId("progress"));
        milestone.setMenteeId(menteeId);
        milestone.setTitle(title);
        milestone.setDescription(description);
        milestone.setCompletion(0);
        milestone.setStatus("Planned");
        milestone.setDueDate(dueDate);
        store.getProgress().add(milestone);
        return Map.of("success", true);
    }

    public synchronized Map<String, Object> updateProgress(String authorizationHeader, String progressId, Map<String, Object> payload) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        PlatformData.ProgressMilestone milestone = store.getProgress().stream()
                .filter(item -> Objects.equals(item.getId(), progressId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found"));

        validateMenteeAccess(account, milestone.getMenteeId());

        Integer completion = asInteger(payload.get("completion"));
        if (completion != null) {
            milestone.setCompletion(Math.max(0, Math.min(100, completion)));
        }

        String status = asString(payload.get("status"));
        if (status != null) {
            milestone.setStatus(status);
        } else if (defaultInt(milestone.getCompletion()) >= 100) {
            milestone.setStatus("Completed");
        } else if (defaultInt(milestone.getCompletion()) >= 60) {
            milestone.setStatus("On Track");
        } else {
            milestone.setStatus("Needs Attention");
        }

        return Map.of("success", true);
    }

    public synchronized Map<String, Object> adminSummary(String authorizationHeader) {
        PlatformData.Account account = requireAccount(authorizationHeader);
        requireAdmin(account);

        List<PlatformData.UserProfile> mentors = getUsersByRole("mentor");
        List<PlatformData.UserProfile> mentees = getUsersByRole("mentee");
        int totalCapacity = mentors.stream().mapToInt(mentor -> defaultInt(mentor.getCapacity())).sum();
        int usedCapacity = mentors.stream().mapToInt(mentor -> defaultInt(mentor.getCurrentMentees())).sum();

        List<Map<String, Object>> atRisk = mentees.stream()
                .map(mentee -> {
                    ProgressSummary summary = progressSummary(mentee.getId());
                    Map<String, Object> response = new LinkedHashMap<>(userResponse(mentee));
                    response.put("mentor", userResponse(findUser(mentee.getCurrentMentorId())));
                    response.put("progress", progressSummaryMap(mentee.getId()));
                    response.put("completionAverage", summary.completionAverage());
                    return response;
                })
                .filter(item -> (Integer) item.get("completionAverage") < 60)
                .toList();

        return Map.of(
                "metrics", List.of(
                        Map.of("label", "Mentor utilization", "value", totalCapacity == 0 ? "0%" : Math.round((usedCapacity * 100.0) / totalCapacity) + "%"),
                        Map.of("label", "Active matches", "value", String.valueOf(store.getMatches().stream().filter(item -> "active".equalsIgnoreCase(item.getStatus())).count())),
                        Map.of("label", "Upcoming sessions", "value", String.valueOf(store.getSessions().stream().filter(item -> "scheduled".equalsIgnoreCase(item.getStatus())).count())),
                        Map.of("label", "Needs attention", "value", String.valueOf(atRisk.size()))
                ),
                "atRiskMentees", atRisk,
                "sessions", store.getSessions().stream()
                        .sorted(Comparator.comparing(PlatformData.MentorshipSession::getDateTime))
                        .map(this::sessionResponse)
                        .toList()
        );
    }

    private PlatformData loadSeedData() {
        try (InputStream inputStream = new ClassPathResource("seed-data.json").getInputStream()) {
            return objectMapper.readValue(inputStream, PlatformData.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load seed data", exception);
        }
    }

    private PlatformData.Account requireAccount(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null || !activeSessions.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String accountId = activeSessions.get(token);
        return store.getAccounts().stream()
                .filter(account -> Objects.equals(account.getId(), accountId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private void requireAdmin(PlatformData.Account account) {
        if (!"admin".equals(account.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private void validateMenteeAccess(PlatformData.Account account, String menteeId) {
        if (menteeId == null || menteeId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentee id is required");
        }

        if (!canAccessMentee(account, menteeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profile access denied");
        }
    }

    private boolean canAccessMentee(PlatformData.Account account, String menteeId) {
        return "admin".equals(account.getRole()) || Objects.equals(account.getLinkedUserId(), menteeId);
    }

    private PlatformData.UserProfile requireMentor(String mentorId) {
        PlatformData.UserProfile user = findUser(mentorId);
        if (user == null || !"mentor".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mentor");
        }
        return user;
    }

    private PlatformData.UserProfile requireMentee(String menteeId) {
        PlatformData.UserProfile user = findUser(menteeId);
        if (user == null || !"mentee".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mentee");
        }
        return user;
    }

    private PlatformData.UserProfile findUser(String userId) {
        if (userId == null) {
            return null;
        }

        return store.getUsers().stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findFirst()
                .orElse(null);
    }

    private List<PlatformData.UserProfile> getUsersByRole(String role) {
        return store.getUsers().stream()
                .filter(user -> Objects.equals(user.getRole(), role))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<String, Object> buildAuthPayload(PlatformData.Account account, String token) {
        Map<String, Object> accountView = new LinkedHashMap<>();
        accountView.put("id", account.getId());
        accountView.put("role", account.getRole());
        accountView.put("username", account.getUsername());
        accountView.put("displayName", account.getDisplayName());
        accountView.put("linkedUserId", account.getLinkedUserId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("token", token);
        payload.put("account", accountView);
        payload.put("profile", userResponse(findUser(account.getLinkedUserId())));
        return payload;
    }

    private Map<String, Object> userResponse(PlatformData.UserProfile user) {
        if (user == null) {
            return null;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("role", user.getRole());
        response.put("name", user.getName());
        response.put("title", user.getTitle());
        response.put("company", user.getCompany());
        response.put("bio", user.getBio());
        response.put("expertise", safeList(user.getExpertise()));
        response.put("availability", safeList(user.getAvailability()));
        response.put("experienceYears", user.getExperienceYears());
        response.put("capacity", user.getCapacity());
        response.put("currentMentees", user.getCurrentMentees());
        response.put("rating", user.getRating());
        response.put("location", user.getLocation());
        response.put("goals", safeList(user.getGoals()));
        response.put("focusAreas", safeList(user.getFocusAreas()));
        response.put("currentMentorId", user.getCurrentMentorId());
        return response;
    }

    private Map<String, Object> matchResponse(PlatformData.MentorshipMatch match) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", match.getId());
        response.put("mentorId", match.getMentorId());
        response.put("menteeId", match.getMenteeId());
        response.put("status", match.getStatus());
        response.put("createdAt", match.getCreatedAt());
        response.put("mentor", userResponse(findUser(match.getMentorId())));
        response.put("mentee", userResponse(findUser(match.getMenteeId())));
        return response;
    }

    private Map<String, Object> sessionResponse(PlatformData.MentorshipSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", session.getId());
        response.put("mentorId", session.getMentorId());
        response.put("menteeId", session.getMenteeId());
        response.put("topic", session.getTopic());
        response.put("dateTime", session.getDateTime());
        response.put("mode", session.getMode());
        response.put("durationMinutes", defaultInt(session.getDurationMinutes()));
        response.put("status", session.getStatus());
        response.put("notes", session.getNotes());
        response.put("mentor", userResponse(findUser(session.getMentorId())));
        response.put("mentee", userResponse(findUser(session.getMenteeId())));
        return response;
    }

    private Map<String, Object> progressResponse(PlatformData.ProgressMilestone item) {
        return Map.of(
                "id", item.getId(),
                "menteeId", item.getMenteeId(),
                "title", item.getTitle(),
                "description", item.getDescription(),
                "completion", defaultInt(item.getCompletion()),
                "status", item.getStatus(),
                "dueDate", item.getDueDate()
        );
    }

    private ProgressSummary progressSummary(String menteeId) {
        List<PlatformData.ProgressMilestone> milestones = store.getProgress().stream()
                .filter(item -> Objects.equals(item.getMenteeId(), menteeId))
                .toList();

        if (milestones.isEmpty()) {
            return new ProgressSummary(0, 0, 0);
        }

        int total = milestones.stream().mapToInt(item -> defaultInt(item.getCompletion())).sum();
        int completed = (int) milestones.stream().filter(item -> defaultInt(item.getCompletion()) >= 100).count();
        return new ProgressSummary((int) Math.round(total / (double) milestones.size()), completed, milestones.size());
    }

    private Map<String, Object> progressSummaryMap(String menteeId) {
        ProgressSummary summary = progressSummary(menteeId);
        return Map.of(
                "completionAverage", summary.completionAverage(),
                "completedMilestones", summary.completedMilestones(),
                "milestoneCount", summary.milestoneCount()
        );
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private record ProgressSummary(int completionAverage, int completedMilestones, int milestoneCount) {
    }
}
