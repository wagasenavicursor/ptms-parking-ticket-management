ALTER TABLE parking_ticket ADD COLUMN ticket_number INT NULL AFTER duration_hours;
UPDATE parking_ticket p JOIN (SELECT id, ROW_NUMBER() OVER (PARTITION BY duration_hours ORDER BY id) AS duration_ticket_number FROM parking_ticket) numbered ON numbered.id=p.id SET p.ticket_number=numbered.duration_ticket_number;
ALTER TABLE parking_ticket MODIFY ticket_number INT NOT NULL;
CREATE UNIQUE INDEX uk_ticket_duration_number ON parking_ticket(duration_hours,ticket_number);
