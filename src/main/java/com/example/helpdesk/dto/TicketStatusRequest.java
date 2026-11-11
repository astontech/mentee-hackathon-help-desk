package com.example.helpdesk.dto;

import com.example.helpdesk.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusRequest(
        @NotNull TicketStatus status
) {
}
