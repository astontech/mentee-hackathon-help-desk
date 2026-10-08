# Design

How the team split the work: the pieces, who owns each, where the pieces meet, and what the owners agreed about each place they meet.

# Routing (Ticket Assignment / Creation)
    - Tickets are auto-assigned to agents if that agent is not at ticket limit
    - Assignment prioritizes agents with least amount of tickets, if tied the agent who has gone the longest without
      a ticket assignment
    - Assign highest priority, oldest ticket
    - When ticket is assigned update ticket status to "Assigned"
    - Schedule a job to check for agent availability and assign tickets (every 5 min?)
    - Auto-assign process is triggered when a ticket's status is set to Resolved
    - Do we care about the priority of tickets a
    - Ticket limit should be configurable
    - Tickets require a Priority
    - Tickets need to show how long they have existed

# Escalation
    - Tickets escalate automatically after a time period based on current priority
        -Low(3days), normal(1day), high(4hours), urgent(1hour)
        -Time periods should be configurable so we can adjust in the future and for demo
    - Check for escalation should be scheduled job. (start with 5 min?) 
    - Ticket is marked escalated after first auto escalation
    - Ticket priority is only changed through this auto-escalation process
    - don't need to track previous/original priority
    - Tickets require a Priority

# Agent Workflow (status updates / viewability)
    - Tickets have a status field
        - Unassigned, Assigned, In Progress, Resolved
    - Status field needs to be updated when an agent starts work and finishes work
    - Tickets need to show whether they have been escalated
    - Should be able to view all tickets, thier status, and their priority 
    - Agents have a limit of tickets they can work on
    - Agents need to be able to update ticket status (implies endpoint for status update)
    - Agents can view the tickets assigned to them and the status of those tickets (implies endpoint)
