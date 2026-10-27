package com.example.helpdesk.dto;

import java.time.Instant;


public record AgentAssignmentDto(
        Long id,
        String name,
        String email,
        Instant lastAssignedTs,
        Long ticketCount) {}
