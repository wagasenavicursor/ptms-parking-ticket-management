DELETE FROM reconciliation_scan;
DELETE FROM reconciliation;
DELETE FROM ticket_issue_item;
DELETE FROM ticket_issue;
DELETE FROM parking_ticket;
DELETE FROM visitor;
DELETE FROM employee;
DELETE FROM team;
DELETE FROM department;

ALTER TABLE reconciliation_scan AUTO_INCREMENT=1;
ALTER TABLE reconciliation AUTO_INCREMENT=1;
ALTER TABLE ticket_issue_item AUTO_INCREMENT=1;
ALTER TABLE ticket_issue AUTO_INCREMENT=1;
ALTER TABLE parking_ticket AUTO_INCREMENT=1;
ALTER TABLE visitor AUTO_INCREMENT=1;
ALTER TABLE employee AUTO_INCREMENT=1;

INSERT INTO department(name,description,enabled) VALUES
('Technology','Software engineering and data teams',TRUE),
('Finance','Finance and accounting',TRUE),
('Operations','Office and parking operations',TRUE),
('Commercial','Sales and customer operations',TRUE);

INSERT INTO team(name,department,description,enabled) VALUES
('Platform Engineering','Technology','Application engineering team',TRUE),
('Data Services','Technology','Data and analytics team',TRUE),
('Security Desk','Operations','Parking security operations',TRUE),
('Corporate Sales','Commercial','Corporate sales team',TRUE);

INSERT INTO employee(employee_code,name,vehicle_number,team,department,parking_pass,default_entry_time) VALUES
('EMP2001','Anuki Perera','CAA-2481','Platform Engineering','Technology',TRUE,'08:00:00'),
('EMP2002','Ravindu Silva','CBF-9134','Data Services','Technology',FALSE,'08:30:00'),
('EMP2003','Thilini Fernando','CAD-5072','Corporate Sales','Commercial',TRUE,'09:00:00'),
('EMP2004','Nimesh Jayasinghe','CBK-6631','Security Desk','Operations',FALSE,'07:30:00'),
('EMP2005','Sachini De Alwis','CAX-1840','Data Services','Technology',FALSE,'08:15:00');

INSERT INTO visitor(visitor_code,name,nic,vehicle_number,host_department) VALUES
('VIS2001','Kasun Wijeratne','941230001V','CAR-5520','Technology'),
('VIS2002','Ishara Madushani','967540002V','CBM-3188','Finance'),
('VIS2003','Dulanjana Peiris',NULL,'CAG-7712','Commercial');

INSERT INTO parking_ticket(barcode,physical_ticket_number,duration_hours,ticket_number,stock_issue_date,ticket_number_cycle,status,expiry_date,created_at,version) VALUES
('3750645','298604',1,1,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('3750646','298604',1,2,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('3750647','298604',1,3,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('3750648','298604',1,4,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('3750649','298604',1,5,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('3750650','298604',1,6,'2026-09-01','VALIDITY:2026-09-01:2027-01-01','AVAILABLE','2027-01-01',CURRENT_TIMESTAMP,0),
('4851201','398701',2,1,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('4851202','398702',2,2,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('5942301','498801',4,1,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('5942302','498802',4,2,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('7063401','598901',6,1,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('8174501','699001',8,1,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0),
('9285601','799101',12,1,'2026-09-01','DAY:2026-09-01','AVAILABLE','2027-03-01',CURRENT_TIMESTAMP,0);
