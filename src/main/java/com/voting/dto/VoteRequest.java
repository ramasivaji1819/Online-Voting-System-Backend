package com.voting.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull Long candidateId) { }
