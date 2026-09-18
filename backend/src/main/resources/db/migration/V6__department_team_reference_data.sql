CREATE TABLE IF NOT EXISTS department (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(160) NOT NULL UNIQUE,
  description VARCHAR(500),
  enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS team (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(160) NOT NULL UNIQUE,
  department VARCHAR(160),
  description VARCHAR(500),
  enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT IGNORE INTO department(name, description, enabled)
SELECT DISTINCT e.department, 'Migrated from employee records', TRUE
FROM employee e
WHERE e.department IS NOT NULL AND TRIM(e.department) <> '';

INSERT IGNORE INTO department(name, description, enabled)
SELECT DISTINCT v.host_department, 'Migrated from visitor host departments', TRUE
FROM visitor v
WHERE v.host_department IS NOT NULL AND TRIM(v.host_department) <> '';

INSERT IGNORE INTO department(name, description, enabled) VALUES
('Administration', 'Default department', TRUE),
('Engineering', 'Default department', TRUE),
('Finance', 'Default department', TRUE),
('Operations', 'Default department', TRUE);

INSERT IGNORE INTO team(name, department, description, enabled)
SELECT DISTINCT e.team, e.department, 'Migrated from employee records', TRUE
FROM employee e
WHERE e.team IS NOT NULL AND TRIM(e.team) <> '';

INSERT IGNORE INTO team(name, department, description, enabled) VALUES
('Security Desk', 'Operations', 'Default team', TRUE),
('Admin Team', 'Administration', 'Default team', TRUE),
('Engineering Team', 'Engineering', 'Default team', TRUE);
