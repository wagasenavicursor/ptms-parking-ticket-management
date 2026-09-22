-- Replace all operational/demo records with one internally consistent PTMS dataset.
-- User accounts and application settings are intentionally preserved.
DELETE FROM reconciliation_scan;
DELETE FROM reconciliation;
DELETE FROM ticket_issue_item;
DELETE FROM ticket_issue;
DELETE FROM parking_ticket;
DELETE FROM visitor;
DELETE FROM employee;
DELETE FROM team;
DELETE FROM department;
DELETE FROM audit_log;

ALTER TABLE reconciliation_scan AUTO_INCREMENT=1;
ALTER TABLE reconciliation AUTO_INCREMENT=1;
ALTER TABLE ticket_issue_item AUTO_INCREMENT=1;
ALTER TABLE ticket_issue AUTO_INCREMENT=1;
ALTER TABLE parking_ticket AUTO_INCREMENT=1;
ALTER TABLE visitor AUTO_INCREMENT=1;
ALTER TABLE employee AUTO_INCREMENT=1;
ALTER TABLE team AUTO_INCREMENT=1;
ALTER TABLE department AUTO_INCREMENT=1;
ALTER TABLE audit_log AUTO_INCREMENT=1;

INSERT INTO department(name,description,enabled) VALUES
('Technology','Software engineering, infrastructure and data services',TRUE),
('Finance','Finance, accounting and procurement',TRUE),
('Operations','Facilities and day-to-day office operations',TRUE),
('Commercial','Sales, partnerships and customer success',TRUE),
('People and Culture','Human resources and employee experience',TRUE),
('Legal and Compliance','Legal, risk and compliance services',TRUE);

INSERT INTO team(name,department,description,enabled) VALUES
('Application Engineering','Technology','Business application engineering',TRUE),
('Data and Analytics','Technology','Data engineering and analytics',TRUE),
('Infrastructure','Technology','Cloud and workplace infrastructure',TRUE),
('Financial Operations','Finance','Accounting and payment operations',TRUE),
('Facilities','Operations','Office and parking operations',TRUE),
('Corporate Sales','Commercial','Corporate customer accounts',TRUE),
('People Operations','People and Culture','Employee services and administration',TRUE),
('Risk and Compliance','Legal and Compliance','Corporate compliance operations',TRUE);

INSERT INTO employee(employee_code,name,vehicle_number,team,department,parking_pass,default_entry_time) VALUES
('EMP3001','Amaya Perera','CAA-4172','Application Engineering','Technology',TRUE,'08:00:00'),
('EMP3002','Dinuka Silva','CBF-8236','Data and Analytics','Technology',FALSE,'08:30:00'),
('EMP3003','Nethmi Fernando','CAD-6094','Corporate Sales','Commercial',TRUE,'09:00:00'),
('EMP3004','Kavindu Jayasinghe','CBK-7351','Infrastructure','Technology',FALSE,'07:45:00'),
('EMP3005','Sewwandi De Alwis','CAX-2918','Financial Operations','Finance',FALSE,'08:15:00'),
('EMP3006','Tharindu Wijeratne','CBE-5643','Facilities','Operations',TRUE,'07:30:00'),
('EMP3007','Rashmi Gunawardena','CAT-9085','People Operations','People and Culture',FALSE,'08:45:00'),
('EMP3008','Malith Peris','CBN-3476','Risk and Compliance','Legal and Compliance',FALSE,'08:30:00'),
('EMP3009','Ishani Madushika','CAQ-6824','Application Engineering','Technology',TRUE,'08:00:00'),
('EMP3010','Supun Ranasinghe','CBL-1597','Corporate Sales','Commercial',FALSE,'09:00:00');

INSERT INTO visitor(visitor_code,name,nic,vehicle_number,host_department) VALUES
('VIS3001','Shenali Abeysekara','956420781V','CAR-5526','Technology'),
('VIS3002','Lasith Ekanayake','921730465V','CBM-3185','Finance'),
('VIS3003','Piumi Rathnayake',NULL,'CAG-7748','Commercial'),
('VIS3004','Ashan Samarawickrama','901240678V','CAC-4269','Legal and Compliance');

INSERT INTO parking_ticket(barcode,physical_ticket_number,duration_hours,ticket_number,stock_issue_date,ticket_number_cycle,status,expiry_date,created_at,issued_at,issued_to_reference,version) VALUES
('3751001','298700',1,1,'2026-09-22','DAY:2026-09-22','ISSUED','2026-12-31','2026-09-22 07:00:00','2026-09-22 09:12:00','VIS3001',0),
('3751002','298700',1,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('3751003','298700',1,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('3751004','298700',1,4,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('3751005','298700',1,5,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('3751006','298700',1,6,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('4852001','398800',2,1,'2026-09-22','DAY:2026-09-22','ISSUED','2026-12-31','2026-09-22 07:00:00','2026-09-22 10:05:00','EMP3002',0),
('4852002','398800',2,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('4852003','398800',2,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('4852004','398800',2,4,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('4852005','398800',2,5,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('5943001','498900',4,1,'2026-09-22','DAY:2026-09-22','ISSUED','2026-12-31','2026-09-22 07:00:00','2026-09-22 08:42:00','EMP3004',0),
('5943002','498900',4,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('5943003','498900',4,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('5943004','498900',4,4,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('7064001','599000',6,1,'2026-09-22','DAY:2026-09-22','ISSUED','2026-12-31','2026-09-22 07:00:00','2026-09-22 08:44:00','EMP3005',0),
('7064002','599000',6,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('7064003','599000',6,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('7064004','599000',6,4,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('8175001','699100',8,1,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('8175002','699100',8,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('8175003','699100',8,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('9286001','799200',12,1,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('9286002','799200',12,2,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0),
('9286003','799200',12,3,'2026-09-22','DAY:2026-09-22','AVAILABLE','2026-12-31','2026-09-22 07:00:00',NULL,NULL,0);

INSERT INTO ticket_issue(request_number,issue_mode,rush_batch_reference,person_type,person_reference,visit_date,entry_time,exit_time,extra_hours,required_hours,reason,status,created_at,completed_at) VALUES
('REQ-3001','NORMAL',NULL,'VISITOR','VIS3001','2026-09-22','09:00:00','10:00:00',0,1,'Client meeting','COMPLETED','2026-09-22 09:10:00','2026-09-22 09:12:00'),
('REQ-3002','NORMAL',NULL,'EMPLOYEE','EMP3002','2026-09-22','08:30:00','10:30:00',0,2,NULL,'COMPLETED','2026-09-22 10:02:00','2026-09-22 10:05:00'),
('RUSH-3001','QUICK','BATCH-20260922083000','EMPLOYEE','EMP3004','2026-09-22',NULL,NULL,0,4,NULL,'COMPLETED','2026-09-22 08:30:00','2026-09-22 08:42:00'),
('RUSH-3002','QUICK','BATCH-20260922083000','EMPLOYEE','EMP3005','2026-09-22',NULL,NULL,0,6,NULL,'COMPLETED','2026-09-22 08:31:00','2026-09-22 08:44:00'),
('RUSH-3003','QUICK','BATCH-20260922113000','EMPLOYEE','EMP3007','2026-09-22',NULL,NULL,0,8,NULL,'PENDING','2026-09-22 11:30:00',NULL),
('RUSH-3004','QUICK','BATCH-20260922113000','EMPLOYEE','EMP3008','2026-09-22',NULL,NULL,0,4,NULL,'PENDING','2026-09-22 11:31:00',NULL),
('REQ-3003','NORMAL',NULL,'VISITOR','VIS3003','2026-09-22','13:00:00','15:00:00',0,2,'Supplier discussion','PENDING','2026-09-22 12:50:00',NULL),
('REQ-3004','NORMAL',NULL,'EMPLOYEE','EMP3010','2026-09-21','09:00:00','10:00:00',0,1,NULL,'CANCELLED','2026-09-21 08:55:00','2026-09-21 08:58:00');

INSERT INTO ticket_issue_item(ticket_issue_id,parking_ticket_id) VALUES
((SELECT id FROM ticket_issue WHERE request_number='REQ-3001'),(SELECT id FROM parking_ticket WHERE barcode='3751001')),
((SELECT id FROM ticket_issue WHERE request_number='REQ-3002'),(SELECT id FROM parking_ticket WHERE barcode='4852001')),
((SELECT id FROM ticket_issue WHERE request_number='RUSH-3001'),(SELECT id FROM parking_ticket WHERE barcode='5943001')),
((SELECT id FROM ticket_issue WHERE request_number='RUSH-3002'),(SELECT id FROM parking_ticket WHERE barcode='7064001'));
