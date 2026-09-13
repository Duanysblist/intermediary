package com.dduany.intermediary.proposal.dto;

import com.dduany.intermediary.proposal.ProposalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProposalStatusRequest {

    @NotNull(message = "Status is required")
    private ProposalStatus status;
}
