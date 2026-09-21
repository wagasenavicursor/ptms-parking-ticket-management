ALTER TABLE ticket_issue ADD COLUMN issue_mode VARCHAR(16) NOT NULL DEFAULT 'NORMAL' AFTER request_number;
ALTER TABLE ticket_issue ADD COLUMN rush_batch_reference VARCHAR(40) NULL AFTER issue_mode;
CREATE INDEX idx_ticket_issue_rush_batch ON ticket_issue(rush_batch_reference);

INSERT INTO app_setting(setting_key,setting_value) VALUES
('defaultIssueMode','NORMAL'),('defaultPersonType','EMPLOYEE'),('defaultDurationMode','HOURS'),
('defaultBarcodeMode','SCAN'),('defaultRushHours','1')
ON DUPLICATE KEY UPDATE setting_value=setting_value;
