package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        Instant createdAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getCreatedAt());
    }
}
