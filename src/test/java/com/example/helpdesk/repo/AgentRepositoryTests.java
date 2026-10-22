package com.example.helpdesk.repo;

import com.example.helpdesk.TestcontainersConfiguration;
import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
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
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets.add(saveTicket(Priority.NORMAL, TicketStatus.IN_PROGRESS, Instant.now()));
        tickets.add(saveTicket(Priority.NORMAL, TicketStatus.RESOLVED, Instant.now()));
        //create Agent1 with 2 active tickets
        Agent agent1 = saveAgent(now, tickets);
        //create Agent2 with 0 tickets
        Agent agent2 = saveAgent(now, new ArrayList<>());

        //Act
        Iterable<Agent> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //agent2 should be first since it has no tickets associated
        assertThat(agents).containsExactly(agent2, agent1);
    }

    @Test
    void agentReturnedFirstIfAllTicketsAreResolved(){
        //Arrange
        List<Ticket> tickets1 = new ArrayList<>();
        tickets1.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets1.add(saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        Agent agent1 = saveAgent(Instant.now(), tickets1);

        List<Ticket> tickets2 = new ArrayList<>();
        tickets2.add(saveTicket(Priority.NORMAL, TicketStatus.RESOLVED, Instant.now()));
        tickets2.add(saveTicket(Priority.URGENT, TicketStatus.RESOLVED, Instant.now()));
        tickets2.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        Agent agent2 = saveAgent(Instant.now(), tickets2);

        //Act
        Iterable<Agent> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //agent2 should be first as all of it's tickets are RESOLVED and not active
        assertThat(agents).containsExactly(agent2, agent1);

    }

    @Test
    void returnsNothingWhenAllAgentsAreAtLimit(){
        //Arrange
        List<Ticket> tickets1 = new ArrayList<>();
        tickets1.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets1.add(saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.ASSIGNED, Instant.now()));
        saveAgent(Instant.now(), tickets1);

        List<Ticket> tickets2 = new ArrayList<>();
        tickets2.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets2.add(saveTicket(Priority.URGENT, TicketStatus.ASSIGNED, Instant.now()));
        tickets2.add(saveTicket(Priority.HIGH, TicketStatus.ASSIGNED, Instant.now()));
        saveAgent(Instant.now(), tickets2);

        //Act
        //Each agent has 3 tickets and the ticket limit is 3
        Iterable<Agent> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        assertThat(agents).isEmpty();
    }

    @Test
    void resolvedTicketsDoNotCountTowardTicketLimit(){
        //Arrange
        List<Ticket> tickets1 = new ArrayList<>();
        tickets1.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets1.add(saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        tickets1.add(saveTicket(Priority.HIGH, TicketStatus.RESOLVED, Instant.now()));
        saveAgent(Instant.now(), tickets1);

        //Act
        // Only agent has 6 tickets, but 4 of them are RESOLVED. Limit is for 3 non-RESOLVED
        Iterable<Agent> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        assertThat(agents).isNotEmpty();
    }

    @Test
    void returnsAgentsWithSameAvailabilityOrderedByDateAssigned(){
        //Arrange
        Instant NOW = Instant.now();
        List<Ticket> tickets1 = new ArrayList<>();
        tickets1.add(saveTicket(Priority.NORMAL, TicketStatus.ASSIGNED, Instant.now()));
        tickets1.add(saveTicket(Priority.URGENT, TicketStatus.IN_PROGRESS, Instant.now()));
        Agent agent1 = saveAgent(NOW, tickets1);

        List<Ticket> tickets2 = new ArrayList<>();
        tickets2.add(saveTicket(Priority.NORMAL, TicketStatus.IN_PROGRESS, Instant.now()));
        tickets2.add(saveTicket(Priority.URGENT, TicketStatus.ASSIGNED, Instant.now()));
        Agent agent2 = saveAgent(NOW.minus(2, ChronoUnit.HOURS), tickets2);

        //Act
        Iterable<Agent> agents = agentRepository.findAvailableAgentsOrderedByLastAssignment(3);

        //Assert
        //Agent2 Should come first as they have gone the longest without being assigned
        assertThat(agents).containsExactly(agent2,agent1);
    }

    //Ticket creation helper method
    Ticket saveTicket(Priority priority, TicketStatus status,  Instant createdAt) {
        return ticketRepository.save(new Ticket(
                "title",
                "desc",
                priority,
                status,
                createdAt
        ));
    }

    //Agent creation helper method
    Agent saveAgent(Instant lastAssignedTs, List<Ticket> tickets){
        return agentRepository.save(new Agent(
                "Agent" + counter.get(),
                "example" + counter.getAndIncrement() + "@example.com",
                lastAssignedTs,
                tickets
        ));
    }


}
