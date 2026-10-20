package com.example.helpdesk.repo;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    //all tickets associated with agent that is assigned or in progress
    @Query("""
            SELECT ticket FROM Agent agent
            JOIN agent.tickets ticket
            WHERE agent.id = :agentId AND ticket.status IN ('ASSIGNED', 'IN_PROGRESS')
            ORDER BY CASE ticket.status
                WHEN 'ASSIGNED' THEN 0
                ELSE 1
                END
            """)
    List<Ticket> findUnresolvedTicketsByAgentId(Long agentId);
}
