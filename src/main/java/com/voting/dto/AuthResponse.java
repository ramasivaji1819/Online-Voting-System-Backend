package com.voting.dto;

public record AuthResponse(String token, String type, String name, String email, String role) { }
