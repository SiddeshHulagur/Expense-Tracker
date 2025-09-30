package com.siddesh.expensetracker.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}