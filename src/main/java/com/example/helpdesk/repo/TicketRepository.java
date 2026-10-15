package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;


public interface TicketRepository extends JpaRepository<Ticket, Long> {


    @Query("""
                SELECT ticket
                FROM  Ticket ticket
                WHERE ( ticket.status = com.example.helpdesk.domain.TicketStatus.UNASSIGNED OR 
                        ticket.status = com.example.helpdesk.domain.TicketStatus.ASSIGNED OR
                        ticket.status = com.example.helpdesk.domain.TicketStatus.IN_PROGRESS)
                AND ( 
                        (ticket.priority = com.example.helpdesk.domain.Priority.LOW 
                         AND
                        COALESCE(ticket.lastEscalationDate, ticket.createdAt) < :lowPriorityCutoffDate)
                    OR
                        (ticket.priority = com.example.helpdesk.domain.Priority.NORMAL 
                         AND
                        COALESCE(ticket.lastEscalationDate, ticket.createdAt) < :normalPriorityCutoffDate)
                    OR
                        (ticket.priority = com.example.helpdesk.domain.Priority.HIGH 
                         AND
                        COALESCE(ticket.lastEscalationDate, ticket.createdAt) < :highPriorityCutoffDate)
                    OR
                        (ticket.priority = com.example.helpdesk.domain.Priority.URGENT 
                         AND
                        COALESCE(ticket.lastEscalationDate, ticket.createdAt) < :urgentPriorityCutoffDate)
                   ) 
            """)
    List<Ticket> findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
            @Param("lowPriorityCutoffDate") Instant lowPriorityCutoffDate,
            @Param("normalPriorityCutoffDate") Instant normalPriorityCutoffDate,
            @Param("highPriorityCutoffDate") Instant highPriorityCutoffDate,
            @Param("urgentPriorityCutoffDate") Instant urgentPriorityCutoffDate
    );
}


