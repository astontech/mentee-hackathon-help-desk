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
import java.util.List;
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
    public void getAllTicketsThatNeedEscalation() {
        //run escalation query
        Instant now = Instant.now();
        Instant lowPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValurForStatus(Priority.LOW));
        Instant normalPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValurForStatus(Priority.NORMAL));
        Instant highPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValurForStatus(Priority.HIGH));
        Instant urgentPriorityCutoffDate = now.minus(priorityWindowLookup.getWindowValurForStatus(Priority.URGENT));

        List<Ticket> ticketsNeedingEscalation = ticketRepository.findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
                lowPriorityCutoffDate,
                normalPriorityCutoffDate,
                highPriorityCutoffDate,
                urgentPriorityCutoffDate
        );

        for (Ticket ticket : ticketsNeedingEscalation) {
            System.out.println();
            System.out.println("ticket ID: " + ticket.getId());
            System.out.println("ticket priority: " + ticket.getPriority());
            System.out.println("ticket status: " + ticket.getStatus());
            System.out.println("ticket date created: " + ticket.getCreatedAt());
        }


        //update all tickets within list to appropriate priorities.
            //if lastEscalationDate is null, update
            //else if lastEscalationDate is not null and priority is not urgent, update
            //else ticket is already urgent and has an escalation date, ignore. (this doesn't seem right)
    }
}
