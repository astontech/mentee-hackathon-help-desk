package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.UNASSIGNED;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant lastEscalationDate;

    @Column
    private Instant dateAssigned;

    @ManyToOne(fetch = FetchType.LAZY)
    private Agent agent;

    public Ticket(String title, String description, Priority priority, Instant createdAt) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    public Ticket(String title, String description, Priority priority, TicketStatus status, Instant createdAt, Agent agent){
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.agent = agent;
    }
}
