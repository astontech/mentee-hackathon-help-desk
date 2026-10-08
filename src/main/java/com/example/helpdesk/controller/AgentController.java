package com.example.helpdesk.controller;

import com.example.helpdesk.dto.AgentResponse;
import com.example.helpdesk.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> getAll() {
        return agentService.getAll().stream().map(AgentResponse::from).toList();
    }
}
