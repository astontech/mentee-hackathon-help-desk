package com.example.helpdesk;


import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AgentApiTests {

    @Autowired
    MockMvcTester mvc;

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
        assertThat(created).bodyJson().extractingPath("$.email").isEqualTo("John@astontech.com");

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
    void seedAgentsAreLoaded() {
        assertThat(mvc.get().uri("/agents"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(3);
    }
}
