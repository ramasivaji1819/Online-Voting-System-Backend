package com.voting.dto;

public record ResultResponse(Long candidateId, String name, String party, long votes) { }
