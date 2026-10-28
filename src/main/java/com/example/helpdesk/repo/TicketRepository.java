package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
                        ((ticket.lastEscalationDate is null) AND (ticket.createdAt < :urgentPriorityCutoffDate)))
                   ) 
            """)
    List<Ticket> findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
            @Param("lowPriorityCutoffDate") Instant lowPriorityCutoffDate,
            @Param("normalPriorityCutoffDate") Instant normalPriorityCutoffDate,
            @Param("highPriorityCutoffDate") Instant highPriorityCutoffDate,
            @Param("urgentPriorityCutoffDate") Instant urgentPriorityCutoffDate
    );

    @Query("""
                    SELECT t FROM Ticket t WHERE t.status = 'UNASSIGNED'
                    ORDER BY CASE t.priority
                        WHEN 'URGENT' THEN 0
                        WHEN 'HIGH' THEN 1
                        WHEN 'NORMAL' THEN 2
                        ELSE 3
                        END
            """)
    Iterable<Ticket> findUnassignedTicketsOrderedByPriority();

    @Transactional
    @Modifying
    @Query("""
    UPDATE Ticket t
    SET t.priority = :priority, t.lastEscalationDate = :lastEscalationDate
    WHERE t.id = :id
    """)
    int updatePriorityAndLastEscalationDate( //returns the number of rows updated
            @Param("id") Long id,
            @Param("priority") Priority priority,
            @Param("lastEscalationDate") Instant lastEscalationDate);

    @Query("""
                SELECT t FROM Ticket t
                WHERE (:status IS NULL OR :status = t.status)
                AND (:priority IS NULL OR :priority = t.priority)
            """)
    Page<Ticket> getTickets(@Param("status") TicketStatus status,
                            @Param("priority") Priority priority,
                            Pageable pageable);
}
