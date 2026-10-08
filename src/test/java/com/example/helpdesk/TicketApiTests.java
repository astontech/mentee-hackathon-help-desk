package com.example.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TicketApiTests {

    @Autowired
    MockMvcTester mvc;

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
    void seedAgentsAreLoaded() {
        assertThat(mvc.get().uri("/agents"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(3);
    }
}
