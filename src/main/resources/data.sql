-- The support team. Runs on every startup; existing agents are left alone.
INSERT INTO agent (name, email, ticket_limit) VALUES
    ('Ana Ruiz',   'ana.ruiz@example.com',   2),
    ('Ben Okafor', 'ben.okafor@example.com', 2),
    ('Cara Singh', 'cara.singh@example.com', 1)
ON CONFLICT (email) DO NOTHING;
