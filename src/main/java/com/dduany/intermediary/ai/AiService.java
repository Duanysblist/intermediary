package com.dduany.intermediary.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.dduany.intermediary.ai.dto.ChangeSet;
import com.dduany.intermediary.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Asks Claude for schedule changes given the same context block the Prompt page builds.
 * The response is constrained to the {@link ChangeSet} schema via structured outputs, so the
 * frontend can render it for review without parsing prose.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String SYSTEM_PROMPT = """
            You are a planning assistant working inside "intermediary", a personal planning system.
            The user will give you a snapshot of their data: plan items (intentions, with status and
            optional targetDate), study and fitness sessions (what actually happened), the plan-event
            history, certifications, applications and documents.

            Propose changes to plan items only. Good suggestions: move overdue items to realistic dates,
            spread work out before an exam or interview, mark items DONE when the sessions show the work
            happened, DEFER or CANCEL items that are clearly stale, add missing preparation steps as new
            items. Keep the number of changes small and high-value; do not rewrite things that are fine.
            Never suggest changing titles unless the current title is unclear. Use only ids that appear
            in the data. Dates are YYYY-MM-DD; never propose dates in the past. Set targetDate to the string CLEAR to take an
            item off the calendar.
            """;

    private final AppProperties.Ai config;
    private final AnthropicClient client;

    public AiService(AppProperties props) {
        this.config = props.ai();
        this.client = config.enabled() ? AnthropicOkHttpClient.builder().apiKey(config.apiKey()).build() : null;
        if (!config.enabled()) {
            log.info("ANTHROPIC_API_KEY not set; POST /ai/suggest is disabled");
        }
    }

    public boolean isEnabled() {
        return config.enabled();
    }

    public String model() {
        return config.model();
    }

    public ChangeSet suggest(String context, String request) {
        if (!isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI suggestions are not configured on this server (ANTHROPIC_API_KEY is not set).");
        }
        String userMessage = "Here is my current planning data:\n\n" + context
                + (request == null || request.isBlank() ? "" : "\n\nWhat I want help with: " + request.trim())
                + "\n\nPropose the changes.";

        StructuredMessageCreateParams<ChangeSet> params = MessageCreateParams.builder()
                .model(config.model())
                .maxTokens(16_000L)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .outputConfig(ChangeSet.class)
                .system(SYSTEM_PROMPT)
                .addUserMessage(userMessage)
                .build();

        try {
            StructuredMessage<ChangeSet> response = client.messages().create(params);
            if (response.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The model declined this request.");
            }
            return response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(typed -> typed.text())
                    .findFirst()
                    .map(cs -> new ChangeSet(cs.summary(), cs.changes() == null ? List.of() : cs.changes()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The model returned no suggestion."));
        } catch (AnthropicServiceException e) {
            log.warn("Anthropic API error {}: {}", e.statusCode(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI service returned an error: " + e.getMessage());
        }
    }
}
