package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// Agents come from data.sql. There's no endpoint to add or change them.
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

    @OneToMany
    private List<Ticket> tickets;
}
