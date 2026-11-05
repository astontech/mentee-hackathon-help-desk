package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.dto.AgentAssignmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentAssignmentServiceTests {

    @Mock
    private AgentService agentService;
    @Mock
    private TicketService ticketService;

    private AgentAssignmentService agentAssignmentService;

    @BeforeEach
    void setup(){
        agentAssignmentService = new AgentAssignmentService(agentService, ticketService);
        ReflectionTestUtils.setField(agentAssignmentService, "TICKET_LIMIT", 3);
    }

    @Test
    void noAgentsAvailable(){
        //arrange
        agentAssignmentQueryReturns();
        ticketAssignmentQueryReturns(new Ticket("title", "desc", Priority.NORMAL, Instant.now()));
        //act
        agentAssignmentService.assignTicketsToAgents();
        //assert
        verify(ticketService, never()).assignTicket(any());
        verify(agentService, never()).updateLastAssignedTs(any());
    }

    @Test
    void noTicketsReturnedByQuery(){
        //arrange
        agentAssignmentQueryReturns(new AgentAssignmentDto(new Agent("Andrew", "andrew@example.com"), 2L));
        ticketAssignmentQueryReturns();
        //act
        agentAssignmentService.assignTicketsToAgents();
        //assert
        verify(ticketService, never()).assignTicket(any());
        verify(agentService, never()).updateLastAssignedTs(any());
    }

    @Test
    void moreTicketsThanAgentsAvailable(){
        //arrange
        ArgumentCaptor<Ticket> assignedTickets = ArgumentCaptor.forClass(Ticket.class);
        ArgumentCaptor<Agent> assignedAgents = ArgumentCaptor.forClass(Agent.class);
        AgentAssignmentDto andrew = new AgentAssignmentDto(new Agent("Andrew", "andrew@example.com"), 2L);
        AgentAssignmentDto dan = new AgentAssignmentDto(new Agent("Dan", "dan@example.com"), 2L);
        AgentAssignmentDto matt = new AgentAssignmentDto(new Agent("Matt", "matt@example.com"), 2L);

        Ticket urgent = new Ticket("title", "desc", Priority.URGENT, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket high = new Ticket("title", "desc", Priority.HIGH, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket normal = new Ticket("title", "desc", Priority.NORMAL, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket low = new Ticket("title", "desc", Priority.LOW, TicketStatus.UNASSIGNED, Instant.now(), null);
        agentAssignmentQueryReturns(andrew, dan, matt);
        ticketAssignmentQueryReturns(urgent, high, normal, low);
        //act
        agentAssignmentService.assignTicketsToAgents();

        //assert
        verify(ticketService, times(3)).assignTicket(assignedTickets.capture());
        assertThat(assignedTickets.getAllValues())
                .extracting(Ticket::getAgent).doesNotHaveDuplicates().containsExactly(andrew.agent(), dan.agent(), matt.agent());

        verify(agentService, times(3)).updateLastAssignedTs(assignedAgents.capture());
        assertThat(assignedAgents.getAllValues())
                .doesNotHaveDuplicates().containsExactly(andrew.agent(), dan.agent(), matt.agent());

        assertThat(urgent.getAgent()).isSameAs(andrew.agent());
        assertThat(high.getAgent()).isSameAs(dan.agent());
        assertThat(normal.getAgent()).isSameAs(matt.agent());
    }

    @Test
    void moreAgentsThanTickets(){
        //arrange
        ArgumentCaptor<Ticket> assignedTickets = ArgumentCaptor.forClass(Ticket.class);
        ArgumentCaptor<Agent> assignedAgents = ArgumentCaptor.forClass(Agent.class);
        AgentAssignmentDto andrew = new AgentAssignmentDto(new Agent("Andrew", "andrew@example.com"), 2L);
        AgentAssignmentDto dan = new AgentAssignmentDto(new Agent("Dan", "dan@example.com"), 2L);
        AgentAssignmentDto sam = new AgentAssignmentDto(new Agent("Sam", "sam@example.com"), 2L);
        AgentAssignmentDto matt = new AgentAssignmentDto(new Agent("Matt", "matt@example.com"), 2L);

        Ticket urgent = new Ticket("title", "desc", Priority.URGENT, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket high = new Ticket("title", "desc", Priority.HIGH, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket normal = new Ticket("title", "desc", Priority.NORMAL, TicketStatus.UNASSIGNED, Instant.now(), null);

        agentAssignmentQueryReturns(andrew, dan, sam, matt);
        ticketAssignmentQueryReturns(urgent, high, normal);
        //act
        agentAssignmentService.assignTicketsToAgents();

        //assert
        verify(ticketService, times(3)).assignTicket(assignedTickets.capture());
        assertThat(assignedTickets.getAllValues())
                .extracting(Ticket::getAgent).doesNotHaveDuplicates().containsExactly(andrew.agent(), dan.agent(), sam.agent());

        verify(agentService, times(3)).updateLastAssignedTs(assignedAgents.capture());
        assertThat(assignedAgents.getAllValues())
                .doesNotHaveDuplicates().containsExactly(andrew.agent(), dan.agent(), sam.agent());

        assertThat(urgent.getAgent()).isSameAs(andrew.agent());
        assertThat(high.getAgent()).isSameAs(dan.agent());
        assertThat(normal.getAgent()).isSameAs(sam.agent());
    }

    @Test
    void agentIsAssignedMultipleTickets(){
        //arrange
        ArgumentCaptor<Ticket> assignedTickets = ArgumentCaptor.forClass(Ticket.class);
        ArgumentCaptor<Agent> assignedAgents = ArgumentCaptor.forClass(Agent.class);
        AgentAssignmentDto andrew = new AgentAssignmentDto(new Agent("Andrew", "andrew@example.com"), 1L);
        AgentAssignmentDto dan = new AgentAssignmentDto(new Agent("Dan", "dan@example.com"), 2L);

        Ticket urgent = new Ticket("title", "desc", Priority.URGENT, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket high = new Ticket("title", "desc", Priority.HIGH, TicketStatus.UNASSIGNED, Instant.now(), null);
        Ticket normal = new Ticket("title", "desc", Priority.NORMAL, TicketStatus.UNASSIGNED, Instant.now(), null);

        agentAssignmentQueryReturns(andrew, dan);
        ticketAssignmentQueryReturns(urgent, high, normal);
        //act
        agentAssignmentService.assignTicketsToAgents();

        //assert
        verify(ticketService, times(3)).assignTicket(assignedTickets.capture());
        assertThat(assignedTickets.getAllValues())
                .extracting(Ticket::getAgent).containsExactly(andrew.agent(), dan.agent(), andrew.agent());

        verify(agentService, times(3)).updateLastAssignedTs(assignedAgents.capture());
        assertThat(assignedAgents.getAllValues())
                .containsExactly(andrew.agent(), dan.agent(), andrew.agent());

        assertThat(urgent.getAgent()).isSameAs(andrew.agent());
        assertThat(high.getAgent()).isSameAs(dan.agent());
        assertThat(normal.getAgent()).isSameAs(andrew.agent());
    }


    private void agentAssignmentQueryReturns(AgentAssignmentDto... agents) {
        when(agentService.getAvailableAgents())
                .thenReturn(List.of(agents));
    }

    private void ticketAssignmentQueryReturns(Ticket... tickets) {
        when(ticketService.getUnassignedTicketsOrderedByPriority())
                .thenReturn(List.of(tickets));
    }

}
