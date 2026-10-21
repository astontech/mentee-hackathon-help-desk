package com.example.helpdesk;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.repo.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class TicketRepositoryTests {

    @Autowired
    TicketRepository ticketRepository;

    //declare constant times for testing
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Duration LOW_WINDOW = Duration.ofDays(3);
    private static final Duration NORMAL_WINDOW = Duration.ofDays(1);
    private static final Duration HIGH_WINDOW = Duration.ofHours(4);
    private static final Duration URGENT_WINDOW = Duration.ofHours(1);

    //remove all records in test container before executing each test method
    @BeforeEach
    void removeTicketsFromTable() {
        ticketRepository.deleteAll();
    }

    //Begin Testing
    @Test
    void returnsNothingWhenThereAreNoTickets() {
        //arrange
        //act
        List<Ticket> tickets = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(tickets).isEmpty();
    }

    @Test
    void returnsHighTicketPastItsWindow() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.HIGH, NOW.minus(HIGH_WINDOW).minus(Duration.ofHours(1)));
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).extracting(Ticket::getId).containsExactly(savedTicket.getId());
    }

    @Test
    void skipsNormalTicketInsideItsWindow() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.NORMAL, NOW);
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).isEmpty();
    }

    @Test
    void skipsTicketExactlyAtItsCutoff() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.NORMAL, NOW.minus(NORMAL_WINDOW));
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).isEmpty();
    }

    @Test
    void returnsNothingWhenTicketIsResolved() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.NORMAL, NOW.minus(NORMAL_WINDOW).minus(Duration.ofHours(1)));
        savedTicket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(savedTicket);
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).isEmpty();
    }

    @Test
    void returnsNothingWhenTicketIsUrgentAndMarkedAsEscalated() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.URGENT, NOW.minus(URGENT_WINDOW.multipliedBy(3)));
        savedTicket.setLastEscalationDate(NOW.minus(URGENT_WINDOW.multipliedBy(2)));
        ticketRepository.save(savedTicket);
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).isEmpty();
    }

    @Test
    void usesLastEscalationDateInsteadOfCreatedAt() {
        //arrange
        Ticket savedTicket = saveTicket(Priority.HIGH, NOW.minus(NORMAL_WINDOW).minus(HIGH_WINDOW.dividedBy(2)));
        savedTicket.setLastEscalationDate(NOW.minus(HIGH_WINDOW.dividedBy(2)));
        ticketRepository.save(savedTicket);
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).isEmpty();
    }

    @Test
    void appliesEachPriorityToItsOwnWindow() {
        //arrange
        Ticket savedLowTicket = saveTicket(Priority.LOW, NOW.minus(URGENT_WINDOW.multipliedBy(2)));
        Ticket savedUrgentTicket = saveTicket(Priority.URGENT, NOW.minus(URGENT_WINDOW.multipliedBy(2)));
        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).extracting(Ticket::getId).containsExactly(savedUrgentTicket.getId());
    }

    @Test
    void returnsOnlyOverdueTicketsFromAMixedSet() {
        //arrange
        Ticket lowOverdue = saveTicket(Priority.LOW, NOW.minus(LOW_WINDOW.multipliedBy(2)));
        Ticket lowInWindow = saveTicket(Priority.LOW, NOW.minus(LOW_WINDOW.dividedBy(2)));
        Ticket normalOverdue = saveTicket(Priority.NORMAL, NOW.minus(NORMAL_WINDOW.multipliedBy(2)));
        Ticket normalInWindow = saveTicket(Priority.NORMAL, NOW.minus(NORMAL_WINDOW.dividedBy(2)));
        Ticket highOverdue = saveTicket(Priority.HIGH, NOW.minus(HIGH_WINDOW.multipliedBy(2)));
        Ticket highInWindow = saveTicket(Priority.HIGH, NOW.minus(HIGH_WINDOW.dividedBy(2)));
        Ticket urgentOverdue = saveTicket(Priority.URGENT, NOW.minus(URGENT_WINDOW.multipliedBy(2)));
        Ticket urgentInWindow = saveTicket(Priority.URGENT, NOW.minus(URGENT_WINDOW.dividedBy(2)));

        Ticket overdueButResolved = saveTicket(Priority.NORMAL, NOW.minus(NORMAL_WINDOW.multipliedBy(2))); //resolved ticket should NOT return
        overdueButResolved.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(overdueButResolved);

        Ticket urgentButAlreadyEscalated = saveTicket(Priority.URGENT, NOW.minus(URGENT_WINDOW.multipliedBy(3))); //urgent tickets marked resolved already should NOT return
        urgentButAlreadyEscalated.setLastEscalationDate(NOW.minus(URGENT_WINDOW.multipliedBy(2)));
        ticketRepository.save(urgentButAlreadyEscalated);

        //act
        List<Ticket> results = findAllTicketsOverdueAt(NOW);
        //assert
        assertThat(results).extracting(Ticket::getId).containsExactlyInAnyOrder(
                lowOverdue.getId(),
                normalOverdue.getId(),
                highOverdue.getId(),
                urgentOverdue.getId()
        );
    }


    //region helper methods
    Ticket saveTicket(Priority priority, Instant createdAt) {
        return ticketRepository.save(new Ticket(
                "title",
                "desc",
                priority,
                createdAt
        ));
    }

    List<Ticket> findAllTicketsOverdueAt(Instant now) {
        Instant lowPriorityCutoffDate = now.minus(LOW_WINDOW);
        Instant normalPriorityCutoffDate = now.minus(NORMAL_WINDOW);
        Instant highPriorityCutoffDate = now.minus(HIGH_WINDOW);
        Instant urgentPriorityCutoffDate = now.minus(URGENT_WINDOW);

        return ticketRepository.findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
                lowPriorityCutoffDate,
                normalPriorityCutoffDate,
                highPriorityCutoffDate,
                urgentPriorityCutoffDate
        );
    }
    //endregion
}
