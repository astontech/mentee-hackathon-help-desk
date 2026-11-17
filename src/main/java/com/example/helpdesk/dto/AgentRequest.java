package com.example.helpdesk.dto;


import jakarta.validation.constraints.NotBlank;

public record AgentRequest(
        @NotBlank String name,
        @NotBlank String email
) {
}
