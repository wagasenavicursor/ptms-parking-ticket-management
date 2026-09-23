INSERT INTO app_setting(setting_key, setting_value)
VALUES ('defaultRegisterView', 'ISSUED')
ON DUPLICATE KEY UPDATE setting_value = setting_value;
