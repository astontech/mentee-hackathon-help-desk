package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.AgentRequest;
import com.example.helpdesk.dto.AgentResponse;
import com.example.helpdesk.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("{id}")
    public ResponseEntity<AgentResponse> get(@PathVariable Long id) {
        return new ResponseEntity<>(AgentResponse.from(agentService.get(id)), HttpStatus.OK);
    }

    @GetMapping("{id}/tickets")
    public ResponseEntity<List<Ticket>> getAgentTicketsUnresolved(@PathVariable Long id) {
        return new ResponseEntity<>(agentService.findUnresolvedTicketsByAgent(id), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<AgentResponse> postAgent(@Valid @RequestBody AgentRequest agentRequest) {
        Agent agent = agentService.create(agentRequest);

        //returns URI of where it can be found now that it has been created, along with the contents of the post
        return ResponseEntity.created(URI.create("/agents/" + agent.getId()))
                .body(AgentResponse.from(agent));
    }
}
