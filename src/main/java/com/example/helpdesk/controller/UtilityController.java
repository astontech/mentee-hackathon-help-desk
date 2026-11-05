package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.AgentAssignmentDto;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.AgentService;
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
    private final AgentService agentService;

    // Retrieves all unassigned tickets and orders them by priority level as follows: URGENT, HIGH, NORMAL, LOW
    @GetMapping("/tickets/unassigned")
    public ResponseEntity<Iterable<Ticket>> getUnassignedTickets(){
        return new ResponseEntity(ticketService.getUnassignedTicketsOrderedByPriority(), HttpStatus.OK);
    }

    @GetMapping("/agents/available")
    public ResponseEntity<Iterable<AgentAssignmentDto>> getAvailableAgents(){
        return new ResponseEntity<>(agentService.getAvailableAgents(), HttpStatus.OK);
    }
}
