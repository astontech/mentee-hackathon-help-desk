-- The support team. Runs on every startup; existing agents are left alone.
INSERT INTO agent (name, email) VALUES
    ('Ana Ruiz',   'ana.ruiz@example.com'),
    ('Ben Okafor', 'ben.okafor@example.com'),
    ('Cara Singh', 'cara.singh@example.com')
ON CONFLICT (email) DO NOTHING;

INSERT INTO ticket (title, description, priority, status, created_at) VALUES
    ('Low Ticket', 'This is a new ticket that is low priority', 'LOW', 'UNASSIGNED', clock_timestamp()),
    ('Normal Ticket', 'This is a new ticket that is normal priority', 'NORMAL', 'UNASSIGNED', clock_timestamp()),
    ('High Ticket', 'This is a new ticket that is high priority', 'HIGH', 'UNASSIGNED', clock_timestamp()),
    ('Urgent Ticket', 'This is a new ticket that is urgent priority', 'URGENT', 'UNASSIGNED', clock_timestamp());