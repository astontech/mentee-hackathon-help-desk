package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
                    SELECT t FROM Ticket t WHERE t.status = 'UNASSIGNED'
                    ORDER BY CASE t.priority
                        WHEN 'URGENT' THEN 0
                        WHEN 'HIGH' THEN 1
                        WHEN 'NORMAL' THEN 2
                        ELSE 3
                        END
            """)
    Iterable<Ticket> findUnassignedTicketsOrderedbyPriority();
}
