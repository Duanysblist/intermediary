package com.dduany.intermediary.ai;

import com.dduany.intermediary.ai.dto.SuggestRequest;
import com.dduany.intermediary.ai.dto.SuggestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI", description = "Claude-powered schedule suggestions")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/status")
    @Operation(summary = "Whether suggestions are available on this server")
    public Map<String, Object> status() {
        return Map.of("enabled", aiService.isEnabled(), "model", aiService.model());
    }

    @PostMapping("/suggest")
    @Operation(summary = "Ask Claude for plan changes given a context block; the answer is saved as a pending proposal, nothing is applied server-side")
    public SuggestResponse suggest(@Valid @RequestBody SuggestRequest request) {
        return aiService.suggest(request.getContext(), request.getRequest());
    }
}
