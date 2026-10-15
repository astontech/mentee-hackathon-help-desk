package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.dto.AgentRequest;
import com.example.helpdesk.repo.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    @Transactional(readOnly = true)
    public List<Agent> getAll() {
        return agentRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Agent get(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No agent with id " + id));
    }

    @Transactional
    public Agent create(AgentRequest agentRequest) {
        return agentRepository.save(new Agent(agentRequest.name(), agentRequest.email()));
    }
}
