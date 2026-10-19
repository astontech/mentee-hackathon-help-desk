package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/utility")
@RequiredArgsConstructor
public class UtilityController {

    private final TicketService ticketService;

    // Retrieves all unassigned tickets and orders them by priority level as follows: URGENT, HIGH, NORMAL, LOW
    @GetMapping("/tickets/unassigned")
    public ResponseEntity<Iterable<Ticket>> getUnassignedTickets(){
        return new ResponseEntity(ticketService.getUnassignedTicketsOrderedByPriority(), HttpStatus.OK);
    }
}
