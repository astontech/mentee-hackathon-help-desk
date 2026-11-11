package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.repo.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTests {

    @Mock
    private AgentRepository agentRepository;

    private AgentService agentService;

    @BeforeEach
    void setup(){
        agentService = new AgentService(agentRepository);
    }

    @Test
    void shouldUpdateOnlyOneField(){
        //arrange
        Agent agent = new Agent("Andrew", "Andrew@example.com");
        //act
        agentService.updateLastAssignedTs(agent);
        //assert
        verify(agentRepository, times(1)).updateAgentLastAssignedTs(agent.getId());
    }

}
