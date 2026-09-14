package com.dduany.intermediary.proposal.dto;

import com.dduany.intermediary.ai.dto.ChangeSet;
import com.dduany.intermediary.proposal.ProposalStatus;

import java.time.Instant;
import java.util.List;

public record ProposalResponse(
        Long id,
        String source,
        String summary,
        List<ChangeSet.Change> changes,
        ProposalStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
