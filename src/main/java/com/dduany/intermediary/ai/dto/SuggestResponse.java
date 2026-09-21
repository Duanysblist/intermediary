package com.dduany.intermediary.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * What POST /ai/suggest returns: the change set plus the id of the proposal it was saved as.
 * Saving it means a browser that loses the connection while Claude is thinking (a phone
 * switching apps, for instance) still finds the result in its inbox.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuggestResponse(Long proposalId, String summary, List<ChangeSet.Change> changes) {}
