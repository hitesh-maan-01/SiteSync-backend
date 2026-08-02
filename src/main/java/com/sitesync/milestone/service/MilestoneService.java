package com.sitesync.milestone.service;

import com.sitesync.milestone.dto.MilestoneResponse;
import com.sitesync.milestone.dto.MilestoneUpdateRequest;
import com.sitesync.milestone.entity.Milestone;
import com.sitesync.milestone.entity.MilestoneStatus;
import com.sitesync.milestone.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;

    public List<MilestoneResponse> getMilestonesByProject(UUID projectId) {
        return milestoneRepository.findByProjectIdOrderBySequenceOrderAsc(projectId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MilestoneResponse updateMilestoneStatus(UUID milestoneId, MilestoneUpdateRequest request, UUID userId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));

        milestone.setStatus(request.getStatus());
        milestone.setUpdatedByUserId(userId);

        if (request.getStatus() == MilestoneStatus.COMPLETED) {
            milestone.setCompletedDate(LocalDate.now());
        }

        Milestone saved = milestoneRepository.save(milestone);
        return MilestoneResponse.fromEntity(saved);
    }
}