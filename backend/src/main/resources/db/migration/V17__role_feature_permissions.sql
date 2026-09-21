INSERT INTO app_setting(setting_key,setting_value) VALUES
('quickBatchSecurityEnabled','true'),('quickBatchAdminEnabled','true'),
('inventoryPhotoSecurityEnabled','true'),('inventoryPhotoAdminEnabled','true'),
('inventoryScannerSecurityEnabled','true'),('inventoryScannerAdminEnabled','true'),
('inventoryManualSecurityEnabled','true'),('inventoryManualAdminEnabled','true')
ON DUPLICATE KEY UPDATE setting_value=setting_value;
