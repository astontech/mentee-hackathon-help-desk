# Acceptance criteria

The team's agreed acceptance criteria, precise enough that anyone could tell whether the system meets them. Product signs them off by approving the pull request that adds them. After that they change only through a new pull request that product approves.

# Ticket Assignment
    - Tickets are auto-assigned to agents if that agent is not at ticket limit
    - Assign highest priority, oldest ticket
    - Auto-assign process is triggered when a ticket is created
    - Auto-assign process is triggered when a ticket's status is set to Resolved
    - Do we care about the priority of tickets already assigned to an agent? (no but posible follow-up) 

# Escalation
    -Tickets escalate automatically after a time period based on current priority
        -Low(3days), normal(1day), high(4hours), urgent(1hour)
    -Ticket is marked overdue after first auto escalation
    -Ticket priority is only changed through this auto-escalation process
    -don't need to track previous/original priority

# Ticket Limit
    - Ticket limit should be configurable
    
# Tickets
    - Tickets require a Priority
    - Tickets have a status field
        - Unassigned, Assigned, In Progress, Resolved
    - Status field needs to be updatable
    - Tickets have a unique ID
    - Tickets need to show whether they have been escalated
    - Tickets need to show how long they have existed
    - A Resolved ticket is removed from an agent's ticket limit

# Agents
    - Agents have a limit of tickets they can work on
    - Agents have a Unique ID
    - Agents HAVE tickets
    - Agents need to be able to update ticket status (implies endpoint for status update)

# Ticket Status
    - Tickets have a status field
        - Unassigned, Assigned, In Progress, Resolved
    - Tickets updated to Done trigger the auto-assign process for the agent 