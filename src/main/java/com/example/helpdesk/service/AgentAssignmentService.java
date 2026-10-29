package com.example.helpdesk.service;

import com.example.helpdesk.domain.Agent;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.AgentAssignmentDto;
import com.example.helpdesk.repo.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class AgentAssignmentService {
    private final AgentService agentService;
    private final TicketService ticketService;

    @Value("${agent.ticket-limit:3}")
    private int TICKET_LIMIT;

    @Scheduled(cron = "${ticket-assignment-job-schedule-window}")
    public void assignTicketsToAgents(){
        Queue<AgentAssignmentDto> availableAgents =
                StreamSupport.stream(agentService.getAvailableAgents().spliterator(),false).collect(Collectors.toCollection(ArrayDeque::new));
        Iterator<Ticket> ticketsToAssign = ticketService.getUnassignedTicketsOrderedByPriority().iterator();

        ticketsToAssign.forEachRemaining(ticket -> {
            while(Objects.nonNull(availableAgents.peek())){
                AgentAssignmentDto agent = availableAgents.poll();
                ticket.setAgent(agent.agent());
                ticketService.assignTicket(ticket);
                agentService.updateLastAssignedTs(agent.agent());
                updateTicketQueue(availableAgents, agent);
            }
        });
    }

    public void updateTicketQueue(Queue<AgentAssignmentDto> queue, AgentAssignmentDto agent){
        Integer ticketCount = agent.ticketCount().intValue();
        if(TICKET_LIMIT - ticketCount > 0){
            queue.add(new AgentAssignmentDto(agent.agent(),ticketCount.longValue() + 1));
        }
    }
}
