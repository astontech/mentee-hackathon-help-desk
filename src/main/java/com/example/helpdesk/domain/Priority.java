package com.example.helpdesk.domain;

public enum Priority {
    LOW, // 3 Days before Overdue and escalation
    NORMAL, // 1 Day before Overdue and escalation
    HIGH, // 4 hours before Overdue and escalation
    URGENT // 1 hour before Overdue
}
