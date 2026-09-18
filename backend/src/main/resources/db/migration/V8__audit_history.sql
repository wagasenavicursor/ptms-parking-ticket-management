CREATE TABLE audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_time DATETIME(6) NOT NULL,
  username VARCHAR(120) NOT NULL,
  user_role VARCHAR(40) NULL,
  action VARCHAR(40) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id VARCHAR(120) NULL,
  summary VARCHAR(500) NOT NULL,
  details LONGTEXT NULL,
  PRIMARY KEY (id),
  INDEX idx_audit_event_time (event_time),
  INDEX idx_audit_username (username),
  INDEX idx_audit_entity_type (entity_type),
  INDEX idx_audit_action (action)
);
