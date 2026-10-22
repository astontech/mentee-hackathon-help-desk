package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query("""
            SELECT a
            FROM Agent a
            LEFT JOIN a.tickets t ON t.status != 'RESOLVED'
            GROUP BY a.id
            HAVING COUNT(t.id) < :ticketLimit
            ORDER BY COUNT(t.id), a.lastAssignedTs NULLS FIRST
            """)
    Iterable<Agent> findAvailableAgentsOrderedByLastAssignment(@Param("ticketLimit") int ticketLimit);
}
