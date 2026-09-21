ALTER TABLE parking_ticket DROP INDEX uk_ticket_duration_number;
ALTER TABLE parking_ticket ADD COLUMN stock_issue_date DATE NULL AFTER ticket_number;
ALTER TABLE parking_ticket ADD COLUMN ticket_number_cycle VARCHAR(80) NOT NULL DEFAULT 'LEGACY' AFTER stock_issue_date;
UPDATE parking_ticket SET stock_issue_date=DATE(created_at), ticket_number_cycle=CONCAT('LEGACY:', duration_hours);
CREATE UNIQUE INDEX uk_ticket_duration_cycle_number ON parking_ticket(duration_hours,ticket_number_cycle,ticket_number);

UPDATE parking_ticket SET barcode=SUBSTRING(barcode,5) WHERE barcode LIKE 'OGF-%';

INSERT INTO app_setting(setting_key,setting_value) VALUES
('ticketNumberReset1h','DAY'),('ticketNumberReset2h','DAY'),('ticketNumberReset4h','DAY'),
('ticketNumberReset6h','DAY'),('ticketNumberReset8h','DAY'),('ticketNumberReset12h','DAY'),
('ticketNumberManualCycle1h','1'),('ticketNumberManualCycle2h','1'),('ticketNumberManualCycle4h','1'),
('ticketNumberManualCycle6h','1'),('ticketNumberManualCycle8h','1'),('ticketNumberManualCycle12h','1'),
('inventoryDefaultDuration','1'),('inventoryDefaultExpiryMode','NONE'),('inventoryDefaultEntryMode','PHOTO')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
