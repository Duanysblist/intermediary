package com.dduany.intermediary.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuggestRequest {

    /** The Markdown + JSON context the frontend's Prompt page builds. */
    @NotBlank(message = "Context is required")
    @Size(max = 400_000, message = "Context is too large")
    private String context;

    /** Optional free-text ask, e.g. "I only have evenings free this week". */
    @Size(max = 4_000, message = "Request must be 4000 characters or fewer")
    private String request;
}
