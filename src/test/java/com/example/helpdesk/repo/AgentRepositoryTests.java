package com.example.helpdesk.repo;

import com.example.helpdesk.TestcontainersConfiguration;
import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.dto.AgentAssignmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class AgentRepositoryTests {

    @Autowired AgentRepository agentRepository;
    @Autowired TicketRepository ticketRepository;

    AtomicInteger counter = new AtomicInteger(1);

    @BeforeEach
    void setup(){
        agentRepository.deleteAll();
        ticketRepository.deleteAll();
        counter.set(1);
    }

    @Test
    void returnsNewAgentWithZeroTicketsFirst(){
        //Arrange
        Instant now = Instant.now();
        //create Agent1 with 2 active tickets
        Agent agent1 = saveAgent(now);
        //create Agent2 with 0 tickets
        Agent agent2 = saveAgent(now);

        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent1);
        saveTicket(Priority.NORMAL, TicketStatus.IN_PROGRESS, Instant.now(), agent1);
        saveTicket(Priority.NORMAL, TicketStatus.RESOLVED, Instant.now(),agent1);

        //Act
        Iterable<AgentAssignmentDto> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //agent2 should be first since it has no tickets associated
        assertThat(agents).extracting(AgentAssignmentDto::agent).containsExactly(agent2, agent1);
    }

    @Test
    void agentReturnedFirstIfAllTicketsAreResolved(){
        //Arrange
        Agent agent1 = saveAgent(Instant.now());
        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent1);
        saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now(), agent1);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent1);

        Agent agent2 = saveAgent(Instant.now());
        saveTicket(Priority.NORMAL, TicketStatus.RESOLVED, Instant.now(), agent2);
        saveTicket(Priority.URGENT, TicketStatus.RESOLVED, Instant.now(), agent2);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent2);

        //Act
        Iterable<AgentAssignmentDto> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //agent2 should be first as all of its tickets are RESOLVED and not active
        assertThat(agents).extracting(AgentAssignmentDto::agent).containsExactly(agent2, agent1);

    }

    @Test
    void returnsNothingWhenAllAgentsAreAtLimit(){
        //Arrange
        Agent agent1 = saveAgent(Instant.now());
        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent1);
        saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now(), agent1);
        saveTicket(Priority.HIGH, TicketStatus.ASSIGNED, Instant.now(), agent1);

        Agent agent2 = saveAgent(Instant.now());
        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent2);
        saveTicket(Priority.URGENT, TicketStatus.ASSIGNED, Instant.now(), agent2);
        saveTicket(Priority.HIGH, TicketStatus.ASSIGNED, Instant.now(), agent2);

        //Act
        //Each agent has 3 tickets and the ticket limit is 3
        Iterable<AgentAssignmentDto> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        assertThat(agents).isEmpty();
    }

    @Test
    void resolvedTicketsDoNotCountTowardTicketLimit(){
        //Arrange
        List<Ticket> tickets1 = new ArrayList<>();
        Agent agent = saveAgent(Instant.now());
        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent);
        saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now(), agent);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent);
        saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now(), agent);

        //Act
        // Only agent has 6 tickets, but 4 of them are RESOLVED. Limit is for 3 non-RESOLVED
        Iterable<AgentAssignmentDto> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        assertThat(agents).isNotEmpty();
    }

    @Test
    void returnsAgentsWithSameAvailabilityOrderedByDateAssigned(){
        //Arrange
        Instant NOW = Instant.now();
        Agent agent1 = saveAgent(NOW);
        saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now(), agent1);
        saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now(), agent1);

        Agent agent2 = saveAgent(NOW.minus(2, ChronoUnit.HOURS));
        saveTicket(Priority.NORMAL, TicketStatus.IN_PROGRESS, Instant.now(), agent2);
        saveTicket(Priority.URGENT, TicketStatus.ASSIGNED, Instant.now(), agent2);

        //Act
        Iterable<AgentAssignmentDto> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //Agent2 Should come first as they have gone the longest without being assigned
        assertThat(agents).extracting(AgentAssignmentDto::agent).containsExactly(agent2, agent1);
    }

    //Ticket creation helper method
    Ticket saveTicket(Priority priority, TicketStatus status,  Instant createdAt, Agent agent) {
        return ticketRepository.save(new Ticket(
                "title",
                "desc",
                priority,
                status,
                createdAt,
                agent
        ));
    }

    //Agent creation helper method
    Agent saveAgent(Instant lastAssignedTs){
        return agentRepository.save(new Agent(
                "Agent" + counter.get(),
                "example" + counter.getAndIncrement() + "@example.com",
                lastAssignedTs
        ));
    }


}
