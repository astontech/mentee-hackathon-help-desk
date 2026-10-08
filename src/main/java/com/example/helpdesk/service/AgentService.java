package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.repo.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    @Transactional(readOnly = true)
    public List<Agent> getAll() {
        return agentRepository.findAll(Sort.by("id"));
    }
}
