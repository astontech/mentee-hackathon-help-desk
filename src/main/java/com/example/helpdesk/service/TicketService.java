package com.example.helpdesk.service;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.CreateTicketRequest;
import com.example.helpdesk.repo.TicketRepository;
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

    //grab priority window values from profile
    @Value("${escalation-window.LOW}")
    private String lowPriorityWindow;
    @Value("${escalation-window.NORMAL}")
    private String normalPriorityWindow;
    @Value("${escalation-window.HIGH}")
    private String highPriorityWindow;
    @Value("${escalation-window.URGENT}")
    private String urgentPriorityWindow;

    private final TicketRepository ticketRepository;

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
        System.out.println("LOW " + lowPriorityWindow);
        System.out.println("NORMAL " + normalPriorityWindow);
        System.out.println("HIGH " + highPriorityWindow);
        System.out.println("URGENT " + urgentPriorityWindow);

        //update all tickets within list to appropriate priorities.
    }
}
