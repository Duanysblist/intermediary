package com.dduany.intermediary.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * The contract for schedule suggestions coming back from Claude (or pasted in by hand).
 * The frontend renders each change for review; nothing is applied without the user accepting it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChangeSet(
        @JsonPropertyDescription("One or two sentences explaining the overall recommendation.")
        String summary,
        @JsonPropertyDescription("Proposed changes to plan items, most important first. Empty if nothing should change.")
        List<Change> changes
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Change(
            @JsonPropertyDescription("\"update\" for an existing plan item (id required) or \"create\" for a new one (id null).")
            String op,
            @JsonPropertyDescription("Id of the plan item to update; null when op is create.")
            Long id,
            @JsonPropertyDescription("Fields to set. For update, only the fields that change; for create, at least title and intent.")
            PlanItemFields fields,
            @JsonPropertyDescription("Why this change helps, in one sentence, referencing the data.")
            String reason
    ) {}

    /** Null means "leave unchanged". To clear a target date, send the string CLEAR. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PlanItemFields(
            @JsonPropertyDescription("Plan item title. Null to leave unchanged.")
            String title,
            @JsonPropertyDescription("One of STUDY, EXERCISE, APPLY, READ, WRITE, OTHER. Null to leave unchanged.")
            String intent,
            @JsonPropertyDescription("One of PLANNED, IN_PROGRESS, DONE, DEFERRED, CANCELED. Null to leave unchanged.")
            String status,
            @JsonPropertyDescription("Target date as YYYY-MM-DD to move the item, the literal string CLEAR to take it off the calendar, or null to leave unchanged.")
            String targetDate,
            @JsonPropertyDescription("Notes text. Null to leave unchanged.")
            String notes
    ) {}
}
