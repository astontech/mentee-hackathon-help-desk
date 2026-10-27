package com.example.helpdesk.service;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.dto.CreateTicketRequest;
import com.example.helpdesk.dto.TicketResponse;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.helper.PriorityWindowLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Page<TicketResponse> getTickets(TicketStatus status, Priority priority, Pageable pageable) {
        return ticketRepository.getTickets(status, priority, pageable).map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public Iterable<Ticket> getUnassignedTicketsOrderedByPriority(){
        return ticketRepository.findUnassignedTicketsOrderedByPriority();
    }

    @Scheduled(fixedRateString = "${escalation-job-schedule-window}")
    public void getAllTicketsThatNeedEscalation() {
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

        //TODO processing: See next ticket.
    }
}
