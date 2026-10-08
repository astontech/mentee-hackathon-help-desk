package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Agent;

public record AgentResponse(
        Long id,
        String name,
        String email,
        int ticketLimit
) {
    public static AgentResponse from(Agent agent) {
        return new AgentResponse(agent.getId(), agent.getName(), agent.getEmail(), agent.getTicketLimit());
    }
}
