package com.example.helpdesk;


import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.repo.AgentRepository;
import com.example.helpdesk.repo.TicketRepository;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AgentApiTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    TicketRepository ticketRepository;

    //based on createdTicketsCanBeReadBack() method in TicketApiTests.java
    @Test
    void createdAgentCanBeReadBack() {
        MvcTestResult created = mvc.post().uri("/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "John", "email": "John@astontech.com"}
                        """)
                .exchange();

        assertThat(created).hasStatus(201);
        assertThat(created).bodyJson().extractingPath("$.name").isEqualTo("John");
        String location = created.getResponse().getHeader("Location");

        assert location != null;
        assertThat(mvc.get().uri(location))
                .hasStatusOk()
                .bodyJson().extractingPath("$.email").isEqualTo("John@astontech.com");
    }

    @Test
    void missingNameIsRejected() {
        assertThat(mvc.post().uri("/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "John@astontech.com"}
                        """))
                .hasStatus(400)
                .bodyJson().extractingPath("$.errors.name").isNotNull();
    }

    @Test
    void missingEmailIsRejected() {
        assertThat(mvc.post().uri("/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "John"}
                        """))
                .hasStatus(400)
                .bodyJson().extractingPath("$.errors.email").isNotNull();
    }

    @Test
    void unknownAgentIsNotFound() {
        assertThat(mvc.get().uri("/agents/999999"))
                .hasStatus(404);
    }

    @Test
    void seedAgentsAreLoaded() {
        assertThat(mvc.get().uri("/agents"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(3);
    }

    @Test
    void unresolvedTicketsAssociatedWithAgentCanBeFound() {
        Ticket unassigned = new Ticket("johns unassigned", "johns unassigned", Priority.HIGH, Instant.now());
        unassigned.setStatus(TicketStatus.UNASSIGNED);
        ticketRepository.save(unassigned);

        Ticket assigned = new Ticket("johns assigned", "johns assigned", Priority.HIGH, Instant.now());
        assigned.setStatus(TicketStatus.ASSIGNED);
        ticketRepository.save(assigned);

        Ticket inProgress = new Ticket("johns inProgress","johns inProgress", Priority.HIGH, Instant.now());
        inProgress.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(inProgress);

        Ticket resolved = new Ticket("johns resolved","johns resolved", Priority.HIGH, Instant.now());
        resolved.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(resolved);

        Agent john = new Agent("John", "John@email.com");
        john.setTickets(List.of(unassigned, assigned, inProgress, resolved));
        agentRepository.save(john);

        //make sure query filters by agent
        Ticket assigned2 = new Ticket("janes assigned", "janes assigned", Priority.HIGH, Instant.now());
        assigned2.setStatus(TicketStatus.ASSIGNED);
        ticketRepository.save(assigned2);

        Ticket inProgress2 = new Ticket("janes inProgress","janes inProgress", Priority.HIGH, Instant.now());
        inProgress2.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(inProgress2);

        Agent jane = new Agent("Jane", "Jane@email.com");
        jane.setTickets(List.of(assigned2, inProgress2));
        agentRepository.save(jane);

        //now see if query excludes all except in_progress & assigned
        MvcTestResult johnsUnresolvedTickets = mvc.get().uri("/agents/"+ john.getId() +"/tickets")
                .exchange();

        assertThat(johnsUnresolvedTickets).hasStatusOk();

        //every element is either assigned or in progress
        //$ - root of JSON / object, which in this case is the array,
        // [*] - every element in the array,
        // .title is the tickets title field
        assertThat(johnsUnresolvedTickets).bodyJson().extractingPath("$[*].title")
                .asArray().containsExactlyInAnyOrder("johns assigned", "johns inProgress");

    }
}
