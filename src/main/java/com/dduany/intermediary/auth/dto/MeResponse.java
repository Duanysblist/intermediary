package com.dduany.intermediary.auth.dto;

import java.time.Instant;

public record MeResponse(String username, Instant expiresAt) {}
