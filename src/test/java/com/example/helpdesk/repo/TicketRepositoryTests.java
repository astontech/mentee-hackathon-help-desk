package com.example.helpdesk.repo;

import com.example.helpdesk.TestcontainersConfiguration;
import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Iterator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class TicketRepositoryTests {
    @Autowired
    TicketRepository ticketRepository;

    @Test
    void returnsOnlyUnassignedTicketsInPriorityOrder() {
        Iterable<Ticket> tickets = ticketRepository.findUnassignedTicketsOrderedByPriority();

        tickets.forEach(ticket -> {
            assert ticket.getStatus().equals(TicketStatus.UNASSIGNED);
        });

        Iterator<Ticket> it = tickets.iterator();

        assertThat(it.next().getPriority() == Priority.URGENT);
        assertThat(it.next().getPriority() == Priority.URGENT);
        assertThat(it.next().getPriority() == Priority.HIGH);
        assertThat(it.next().getPriority() == Priority.NORMAL);
        assertThat(it.next().getPriority() == Priority.LOW);
    }
}
