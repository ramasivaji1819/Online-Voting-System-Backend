package com.voting.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ElectionRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) { }
