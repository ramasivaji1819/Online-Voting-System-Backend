package com.voting.dto;

import java.time.LocalDateTime;

public record ElectionResponse(Long id, String title, String description,
                               LocalDateTime startTime, LocalDateTime endTime, String status) { }
