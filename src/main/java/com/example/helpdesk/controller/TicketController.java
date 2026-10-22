package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.dto.CreateTicketRequest;
import com.example.helpdesk.dto.TicketResponse;
import com.example.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.create(request);
        return ResponseEntity.created(URI.create("/tickets/" + ticket.getId()))
                .body(TicketResponse.from(ticket));
    }

    @GetMapping("{id}")
    public TicketResponse get(@PathVariable Long id) {
        return TicketResponse.from(ticketService.get(id));
    }

    //pageable allows for sorting
    @GetMapping()
    public Page<TicketResponse> getTickets( @RequestParam(required = false) TicketStatus status,
                                                @RequestParam(required = false) Priority priority,
                                                @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ticketService.getTickets(status, priority, pageable);
    }
}
