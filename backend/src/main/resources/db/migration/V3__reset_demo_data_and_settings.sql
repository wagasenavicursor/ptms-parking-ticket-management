CREATE TABLE IF NOT EXISTS app_setting (setting_key VARCHAR(80) PRIMARY KEY, setting_value VARCHAR(255) NOT NULL);

DELETE FROM reconciliation_scan;
DELETE FROM reconciliation;
DELETE FROM ticket_issue_item;
DELETE FROM ticket_issue;
DELETE FROM parking_ticket;
DELETE FROM visitor;
DELETE FROM employee;

INSERT INTO employee (employee_code,name,vehicle_number,team,department,parking_pass,default_entry_time) VALUES
('EMP1001','Dinesh Perera','CAA-1024','Engineering','Technology',TRUE,'08:00:00'),
('EMP1002','Tharushi Silva','CBF-4521','Data & Analytics','Technology',FALSE,'08:30:00'),
('EMP1003','Kavindu Fernando','CAG-8872','Sales','Commercial',TRUE,'09:00:00'),
('EMP1004','Shenali Jayawardena','CAD-3350','Finance','Finance',FALSE,'08:15:00'),
('EMP1005','Malith Gunasekara','CBK-7194','Operations','Operations',TRUE,'07:30:00');

INSERT INTO visitor (visitor_code,name,nic,vehicle_number,host_department) VALUES
('V1001','Isuru Wickramasinghe','921234567V','CAR-2210','Technology'),
('V1002','Nadeesha Ranasinghe',NULL,'CBM-4488','Finance'),
('V1003','Sahan Abeysekara','881112222V','CAX-9065','Commercial');

INSERT INTO parking_ticket (barcode,duration_hours,status,expiry_date,version) VALUES
('OGF-01H-1001',1,'AVAILABLE','2027-12-31',0),('OGF-01H-1002',1,'AVAILABLE','2027-12-31',0),('OGF-01H-1003',1,'AVAILABLE','2027-12-31',0),('OGF-01H-1004',1,'AVAILABLE','2027-12-31',0),
('OGF-02H-2001',2,'AVAILABLE','2027-12-31',0),('OGF-02H-2002',2,'AVAILABLE','2027-12-31',0),('OGF-02H-2003',2,'AVAILABLE','2027-12-31',0),('OGF-02H-2004',2,'AVAILABLE','2027-12-31',0),
('OGF-04H-4001',4,'AVAILABLE','2027-12-31',0),('OGF-04H-4002',4,'AVAILABLE','2027-12-31',0),('OGF-04H-4003',4,'AVAILABLE','2027-12-31',0),('OGF-04H-4004',4,'AVAILABLE','2027-12-31',0),
('OGF-06H-6001',6,'AVAILABLE','2027-12-31',0),('OGF-06H-6002',6,'AVAILABLE','2027-12-31',0),('OGF-06H-6003',6,'AVAILABLE','2027-12-31',0),
('OGF-08H-8001',8,'AVAILABLE','2027-12-31',0),('OGF-08H-8002',8,'AVAILABLE','2027-12-31',0),('OGF-08H-8003',8,'AVAILABLE','2027-12-31',0),
('OGF-12H-12001',12,'AVAILABLE','2027-12-31',0),('OGF-12H-12002',12,'AVAILABLE','2027-12-31',0),('OGF-12H-12003',12,'AVAILABLE','2027-12-31',0);

INSERT INTO app_setting(setting_key,setting_value) VALUES
('weekdayStart','06:00'),('weekdayEnd','12:00'),('weekendStart','06:00'),('weekendEnd','12:30'),('bufferMinutes','0'),('ticketTypes','1,2,4,6,8,12')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
