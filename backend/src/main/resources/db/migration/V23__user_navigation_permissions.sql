ALTER TABLE app_user ADD COLUMN navigation_permissions VARCHAR(500) NULL;
INSERT INTO app_setting(setting_key,setting_value) VALUES
('adminNavigationPermissions','home,dashboard,issue,register,employees,visitors,inventory,remaining,reports,settings'),
('securityNavigationPermissions','home,dashboard,issue,register,employees,visitors,inventory,remaining,reports')
ON DUPLICATE KEY UPDATE setting_value=setting_value;
