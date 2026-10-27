package com.example.helpdesk;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.repo.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TicketApiTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void createdTicketCanBeReadBack() {
        MvcTestResult created = mvc.post().uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "VPN drops every hour", "description": "Since Monday", "priority": "HIGH"}
                        """)
                .exchange();

        assertThat(created).hasStatus(201);
        assertThat(created).bodyJson().extractingPath("$.priority").isEqualTo("HIGH");
        String location = created.getResponse().getHeader("Location");

        assertThat(mvc.get().uri(location))
                .hasStatusOk()
                .bodyJson().extractingPath("$.title").isEqualTo("VPN drops every hour");
    }

    @Test
    void missingTitleIsRejected() {
        assertThat(mvc.post().uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"priority": "LOW"}
                        """))
                .hasStatus(400)
                .bodyJson().extractingPath("$.errors.title").isNotNull();
    }

    @Test
    void unknownPriorityIsRejected() {
        assertThat(mvc.post().uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Printer jam", "priority": "CRITICAL"}
                        """))
                .hasStatus(400);
    }

    @Test
    void unknownTicketIsNotFound() {
        assertThat(mvc.get().uri("/tickets/999999")).hasStatus(404);
    }

    @Test
    void unknownStatusQueryParamIsRejected() {
        assertThat(mvc.get().uri("/tickets?status=COMPLETE")).hasStatus(400);
    }

    @Test
    void unknownPriorityQueryParamIsRejected() {
        assertThat(mvc.get().uri("/tickets?priority=EXTREME")).hasStatus(400);
    }

    @Test
    void unknownSortIsRejected() {
        assertThat(mvc.get().uri("/tickets?sort=name,desc")).hasStatus(400);
    }

    @Test
    void returnsFirstTenTicketsByDefault() {
        //add at least 11 tickets in case seed data is modified or removed
        for(int i = 0; i < 11; i++) {
            ticketRepository.save(new Ticket("Ticket " + i, "Pagination test", Priority.HIGH, Instant.now()));
        }

        assertThat(mvc.get().uri("/tickets")).hasStatusOk()
                .bodyJson()
                .extractingPath("$.content").asArray().hasSize(10);
    }
}
