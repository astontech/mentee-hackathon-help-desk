package com.example.helpdesk;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.repo.TicketRepository;
import com.example.helpdesk.service.TicketService;
import com.example.helpdesk.service.helper.PriorityWindowLookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTests {

    //mock the repo
    @Mock
    private TicketRepository ticketRepository;

    //declare the service
    private TicketService ticketService;

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Duration LOW_WINDOW = Duration.ofDays(3);
    private static final Duration NORMAL_WINDOW = Duration.ofDays(1);
    private static final Duration HIGH_WINDOW = Duration.ofHours(4);
    private static final Duration URGENT_WINDOW = Duration.ofHours(1);

    //before each test, 'build' the service by adding its dependencies
    @BeforeEach
    void setup(){
        PriorityWindowLookup priorityWindowLookup = new PriorityWindowLookup(
                LOW_WINDOW,
                NORMAL_WINDOW,
                HIGH_WINDOW,
                URGENT_WINDOW
        );

        ticketService = new TicketService(ticketRepository, priorityWindowLookup);
    }

    @Test
    void updatesNothingWhenQueryReturnsNoTickets(){
        //arrange
        queryReturns();

        //act
        ticketService.escalateOverdueTickets();

        //assert
        verify(ticketRepository, never()).updatePriorityAndLastEscalationDate(any(),any(),any());
    }

    @Test
    void promotesOneLevelAndStampsFromCreatedAt(){
        //arrange
        Ticket ticket = new Ticket(
            "title",
            "desc",
            Priority.HIGH,
            NOW.minus(HIGH_WINDOW).minus(HIGH_WINDOW.dividedBy(2))
        );
        ticket.setId(1L);
        queryReturns(ticket);

        //act
        ticketService.escalateOverdueTickets();

        //assert
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(1L,Priority.URGENT, ticket.getCreatedAt().plus(HIGH_WINDOW));
    }

    @Test
    void promotesOneLevelAndStampsFromLastEscalationDate(){
        //arrange
        Ticket ticket = new Ticket(
                "title",
                "desc",
                Priority.NORMAL,
                NOW.minus(LOW_WINDOW).minus(NORMAL_WINDOW).plus(NORMAL_WINDOW.dividedBy(2))
        );
        ticket.setId(1L);
        ticket.setLastEscalationDate(ticket.getCreatedAt().plus(NORMAL_WINDOW));
        queryReturns(ticket);

        //act
        ticketService.escalateOverdueTickets();

        //assert
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(1L,Priority.HIGH, ticket.getLastEscalationDate().plus(NORMAL_WINDOW));

    }

    @Test
    void keepsUrgentPriorityAndOnlyStampsLastEscalationDate(){
        //arrange
        Ticket ticket = new Ticket(
                "title",
                "desc",
                Priority.URGENT,
                NOW.minus(URGENT_WINDOW.multipliedBy(2))
        );
        ticket.setId(1L);
        queryReturns(ticket);

        //act
        ticketService.escalateOverdueTickets();

        //assert
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(1L, Priority.URGENT, ticket.getCreatedAt().plus(URGENT_WINDOW));
    }

    @Test
    void updatesEveryReturnedTicket(){
        //arrange
        //region ticket declaration
        Ticket lowToNormal = new Ticket(
                "title",
                "desc",
                Priority.LOW,
                NOW.minus(LOW_WINDOW.multipliedBy(2))
        );
        Ticket normalToHigh = new Ticket(
                "title",
                "desc",
                Priority.NORMAL,
                NOW.minus(NORMAL_WINDOW.multipliedBy(2))
        );
        Ticket highToUrgent = new Ticket(
                "title",
                "desc",
                Priority.HIGH,
                NOW.minus(HIGH_WINDOW.multipliedBy(2))
        );
        Ticket urgentToUrgent = new Ticket(
                "title",
                "desc",
                Priority.URGENT,
                NOW.minus(URGENT_WINDOW.multipliedBy(2))
        );
        Ticket stampedEscalation = new Ticket(
                "title",
                "desc",
                Priority.NORMAL,
                NOW.minus(LOW_WINDOW).minus(NORMAL_WINDOW).plus(NORMAL_WINDOW.dividedBy(2))
        );

        lowToNormal.setId(1L);
        normalToHigh.setId(2L);
        highToUrgent.setId(3L);
        urgentToUrgent.setId(4L);
        stampedEscalation.setId(5L);
        stampedEscalation.setLastEscalationDate(stampedEscalation.getCreatedAt().plus(NORMAL_WINDOW));

        //endregion
        queryReturns(
                lowToNormal,
                normalToHigh,
                highToUrgent,
                urgentToUrgent,
                stampedEscalation
        );

        //act
        ticketService.escalateOverdueTickets();

        //assert
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(1L, Priority.NORMAL, lowToNormal.getCreatedAt().plus(LOW_WINDOW));
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(2L, Priority.HIGH, normalToHigh.getCreatedAt().plus(NORMAL_WINDOW));
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(3L, Priority.URGENT, highToUrgent.getCreatedAt().plus(HIGH_WINDOW));
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(4L, Priority.URGENT, urgentToUrgent.getCreatedAt().plus(URGENT_WINDOW));
        verify(ticketRepository, times(1)).updatePriorityAndLastEscalationDate(5L, Priority.HIGH, stampedEscalation.getLastEscalationDate().plus(NORMAL_WINDOW));
        verifyNoMoreInteractions(ticketRepository); //verify that no more calls to repo were made after calls above
    }

    //region helpers
    private void queryReturns(Ticket... tickets) {
        when(ticketRepository.findAllOpenTicketsWithStatusUnassignedAssignedOrInProgressAndIsOverCutoffDate(
                any(),
                any(),
                any(),
                any()))
            .thenReturn(List.of(tickets));
    }
    //endregion
}
