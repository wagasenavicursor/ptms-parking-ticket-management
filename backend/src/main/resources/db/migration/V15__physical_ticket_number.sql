ALTER TABLE parking_ticket ADD COLUMN physical_ticket_number VARCHAR(80) NULL AFTER barcode;
CREATE INDEX idx_ticket_physical_number ON parking_ticket(physical_ticket_number);
