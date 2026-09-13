package com.dduany.intermediary.proposal.dto;

import com.dduany.intermediary.ai.dto.ChangeSet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalRequest {

    /** Who proposed it, e.g. "mcp:claude-desktop". Shown in the review dialog. */
    @NotBlank(message = "Source is required")
    @Size(max = 200, message = "Source must be 200 characters or fewer")
    private String source;

    @Size(max = 2000, message = "Summary must be 2000 characters or fewer")
    private String summary;

    @NotNull(message = "Changes are required")
    private List<ChangeSet.Change> changes;
}
