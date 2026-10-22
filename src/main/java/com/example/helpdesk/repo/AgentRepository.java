package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.dto.AgentAssignmentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query("""
            SELECT NEW com.example.helpdesk.dto.AgentAssignmentDto(a.id, a.name, a.email, a.lastAssignedTs, COUNT(t.id) AS ticketCount)
            FROM Agent a
            LEFT JOIN a.tickets t ON t.status != 'RESOLVED'
            GROUP BY a.id
            HAVING COUNT(t.id) < :ticketLimit
            ORDER BY ticketCount, a.lastAssignedTs NULLS FIRST
            """)
    Iterable<AgentAssignmentDto> findAvailableAgentsOrderedByLastAssignment(@Param("ticketLimit") int ticketLimit);
}
