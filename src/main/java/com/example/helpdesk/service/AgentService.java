package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.dto.AgentAssignmentDto;
import com.example.helpdesk.dto.AgentRequest;
import com.example.helpdesk.repo.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    @Value("${agent.ticket-limit:3}")
    private int TICKET_LIMIT;

    @Transactional(readOnly = true)
    public List<Agent> getAll() {
        return agentRepository.findAll(Sort.by("id"));
    }

    @Transactional
    public Agent create(AgentRequest agentRequest) {
        return agentRepository.save(new Agent(agentRequest.name(), agentRequest.email()));
    }

    public Iterable<AgentAssignmentDto> getAvailableAgents(){
        return agentRepository.findAvailableAgentsOrderedByLastAssignment(TICKET_LIMIT);
    }
}
