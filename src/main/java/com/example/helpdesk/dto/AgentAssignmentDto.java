package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Agent;

import java.time.Instant;


public record AgentAssignmentDto(
        Agent agent,
        Long ticketCount) {}
