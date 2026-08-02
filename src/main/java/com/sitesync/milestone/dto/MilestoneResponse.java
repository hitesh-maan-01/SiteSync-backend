package com.sitesync.milestone.dto;

import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {
    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private MilestoneStatus status;
    private Integer sequenceOrder;
    private LocalDate targetDate;
    private LocalDate completedDate;
    private LocalDateTime updatedAt;

    public static MilestoneResponse fromEntity(Milestone m) {
        return MilestoneResponse.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .status(m.getStatus())
                .sequenceOrder(m.getSequenceOrder())
                .targetDate(m.getTargetDate())
                .completedDate(m.getCompletedDate())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}