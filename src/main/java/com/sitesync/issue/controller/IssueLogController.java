package com.sitesync.issue.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.issue.dto.IssueLogResponse;
import com.sitesync.issue.service.IssueLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueLogController {

    private final IssueLogService issueLogService;

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    // GET /api/v1/issues/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<IssueLogResponse>>> getIssues(
            @PathVariable String projectId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                issueLogService.getIssuesForProject(
                        UUID.fromString(projectId))));
    }

    // POST /api/v1/issues (multipart — photo is optional)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IssueLogResponse>> createIssue(
            @RequestParam("projectId") String projectId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "urgency", defaultValue = "medium") String urgency,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {

        IssueLogResponse issue = issueLogService.createIssue(
                UUID.fromString(projectId),
                currentUserId(authentication),
                title, description, urgency, photo);

        return ResponseEntity.ok(ApiResponse.ok(issue, "Issue reported."));
    }

    // PATCH /api/v1/issues/{id}/resolve
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<IssueLogResponse>> resolveIssue(
            @PathVariable String id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                issueLogService.resolveIssue(
                        UUID.fromString(id),
                        currentUserId(authentication)),
                "Issue resolved."));
    }

    // PATCH /api/v1/issues/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IssueLogResponse>> updateStatus(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                issueLogService.updateStatus(
                        UUID.fromString(id),
                        currentUserId(authentication),
                        body.get("status")),
                "Status updated."));
    }

    // GET /api/v1/issues/project/{projectId}/open-count
    @GetMapping("/project/{projectId}/open-count")
    public ResponseEntity<ApiResponse<Long>> getOpenCount(
            @PathVariable String projectId) {
        return ResponseEntity.ok(ApiResponse.ok(
                issueLogService.getOpenIssueCount(
                        UUID.fromString(projectId))));
    }
}