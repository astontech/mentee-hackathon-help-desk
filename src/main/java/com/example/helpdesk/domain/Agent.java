package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private Instant lastAssignedTs;

    @OneToMany(mappedBy = "agent")
    private List<Ticket> tickets;

    public Agent(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Agent(String name, String email, Instant lastAssignedTs){
        this.name = name;
        this.email = email;
        this.lastAssignedTs = lastAssignedTs;
    }
}
