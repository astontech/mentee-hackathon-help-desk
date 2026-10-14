-- The support team. Runs on every startup; existing agents are left alone.
INSERT INTO agent (name, email) VALUES
    ('Ana Ruiz',   'ana.ruiz@example.com'),
    ('Ben Okafor', 'ben.okafor@example.com'),
    ('Cara Singh', 'cara.singh@example.com')
ON CONFLICT (email) DO NOTHING;
