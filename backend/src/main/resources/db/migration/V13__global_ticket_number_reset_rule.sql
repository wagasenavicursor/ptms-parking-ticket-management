INSERT INTO app_setting(setting_key,setting_value) VALUES
('ticketNumberResetMode','DAY'),
('ticketNumberResetPeriodDays','7'),
('ticketNumberResetPeriodAnchor',CURDATE()),
('ticketNumberResetStartDate',CURDATE()),
('ticketNumberResetEndDate',DATE_ADD(CURDATE(),INTERVAL 6 DAY)),
('ticketNumberManualCycle','1')
ON DUPLICATE KEY UPDATE setting_value=setting_value;
