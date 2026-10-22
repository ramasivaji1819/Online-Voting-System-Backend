package com.voting.dto;

import jakarta.validation.constraints.NotBlank;

public record CandidateRequest(@NotBlank String name, String party) { }
