package com.example.helpdesk.service;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.CreateTicketRequest;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.helper.PriorityWindowLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PriorityWindowLookup priorityWindowLookup;

    @Transactional
    public Ticket create(CreateTicketRequest request) {
        Ticket ticket = new Ticket(request.title(), request.description(), request.priority(), Instant.now());
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket get(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No ticket with id " + id));
    }

    @Scheduled(fixedRateString = "${escalation-job-schedule-window}")
    public void getAllTicketsThatNeedEscalation(){
        //run escalation query
        System.out.println("LOW " + priorityWindowLookup.getWindowValurForStatus(Priority.LOW));
        System.out.println("NORMAL " + priorityWindowLookup.getWindowValurForStatus(Priority.NORMAL));
        System.out.println("HIGH " + priorityWindowLookup.getWindowValurForStatus(Priority.HIGH));
        System.out.println("URGENT " + priorityWindowLookup.getWindowValurForStatus(Priority.URGENT));

        //update all tickets within list to appropriate priorities.
    }
}
