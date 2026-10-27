package com.example.helpdesk.service;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.dto.CreateTicketRequest;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.helper.PriorityWindowLookup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
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

    @Transactional(readOnly = true)
    public Iterable<Ticket> getUnassignedTicketsOrderedByPriority(){
        return ticketRepository.findUnassignedTicketsOrderedByPriority();
    }

    @Scheduled(fixedRateString = "${escalation-job-schedule-window}")
    public void escalateOverdueTickets() {
        //run escalation query
        Instant now = Instant.now();
        Instant lowPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValueForPriority(Priority.LOW));
        Instant normalPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValueForPriority(Priority.NORMAL));
        Instant highPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValueForPriority(Priority.HIGH));
        Instant urgentPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValueForPriority(Priority.URGENT));

        List<Ticket> ticketsNeedingEscalation = ticketRepository.findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
                lowPriorityCutoffDate,
                normalPriorityCutoffDate,
                highPriorityCutoffDate,
                urgentPriorityCutoffDate
        );

        int ticketsEscalated = 0;
        for(Ticket ticket : ticketsNeedingEscalation) {
            Instant referenceDate = ticket.getLastEscalationDate() == null ? ticket.getCreatedAt() : ticket.getLastEscalationDate();
            Priority newPriority = ticket.getPriority();
            Instant newLastEscalationDate = referenceDate.plus(priorityWindowLookup.getWindowValueForPriority(ticket.getPriority()));
            if(ticket.getPriority() != Priority.URGENT) {
                newPriority = newPriority.next();
            }
            //call ticketRepo to update fields
            ticketsEscalated += ticketRepository.updatePriorityAndLastEscalationDate(ticket.getId(), newPriority, newLastEscalationDate);
        }
        if(ticketsEscalated > 0 ){
            log.info("Number of tickets escalated: {}", ticketsEscalated);
        }
        else{
            log.debug("Number of tickets escalated: {}", ticketsEscalated);
        }
    }

    @Transactional
    public Ticket updateStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No ticket with id " + id));

        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
}
