package com.mentorloop.backend.controller;

import com.mentorloop.backend.service.PlatformService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PlatformService platformService;

    public ApiController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return platformService.health();
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        return platformService.login(payload);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.me(authorizationHeader);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.logout(authorizationHeader);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.dashboard(authorizationHeader);
    }

    @GetMapping("/mentors")
    public List<Map<String, Object>> mentors(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String expertise
    ) {
        return platformService.mentors(authorizationHeader, search, expertise);
    }

    @GetMapping("/mentees")
    public List<Map<String, Object>> mentees(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.mentees(authorizationHeader);
    }

    @GetMapping("/recommendations")
    public List<Map<String, Object>> recommendations(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String menteeId
    ) {
        return platformService.recommendations(authorizationHeader, menteeId);
    }

    @GetMapping("/matches")
    public List<Map<String, Object>> matches(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.matches(authorizationHeader);
    }

    @PostMapping("/matches")
    public Map<String, Object> createMatch(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, Object> payload
    ) {
        return platformService.createMatch(authorizationHeader, payload);
    }

    @GetMapping("/sessions")
    public List<Map<String, Object>> sessions(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String menteeId,
            @RequestParam(required = false) String mentorId
    ) {
        return platformService.sessions(authorizationHeader, menteeId, mentorId);
    }

    @PostMapping("/sessions")
    public Map<String, Object> createSession(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, Object> payload
    ) {
        return platformService.createSession(authorizationHeader, payload);
    }

    @PatchMapping("/sessions/{sessionId}")
    public Map<String, Object> updateSession(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> payload
    ) {
        return platformService.updateSession(authorizationHeader, sessionId, payload);
    }

    @GetMapping("/progress")
    public List<Map<String, Object>> progress(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String menteeId
    ) {
        return platformService.progress(authorizationHeader, menteeId);
    }

    @PostMapping("/progress")
    public Map<String, Object> createProgress(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, Object> payload
    ) {
        return platformService.createProgress(authorizationHeader, payload);
    }

    @PatchMapping("/progress/{progressId}")
    public Map<String, Object> updateProgress(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String progressId,
            @RequestBody Map<String, Object> payload
    ) {
        return platformService.updateProgress(authorizationHeader, progressId, payload);
    }

    @GetMapping("/admin/summary")
    public Map<String, Object> adminSummary(@RequestHeader("Authorization") String authorizationHeader) {
        return platformService.adminSummary(authorizationHeader);
    }
}
