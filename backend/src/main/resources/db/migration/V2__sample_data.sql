INSERT IGNORE INTO employee (employee_code,name,vehicle_number,team,department,parking_pass,default_entry_time) VALUES
('EMP001','Nimal Perera','CAB-1234','Platform','Engineering',TRUE,'08:00:00'),
('EMP002','Kasuni Silva','CAA-7788','Data','Technology',FALSE,'08:30:00'),
('EMP003','Ruwan Fernando','CAR-4567','Sales Ops','Sales',TRUE,'09:00:00');

INSERT IGNORE INTO visitor (visitor_code,name,nic,vehicle_number,host_department) VALUES
('V0001','Amal Jayasinghe','901234567V','CBF-2233','Engineering'),
('V0002','Dilani Perera',NULL,'CAD-9911','Finance');

INSERT IGNORE INTO parking_ticket (barcode,duration_hours,status,expiry_date,version) VALUES
('PT-1H-001',1,'AVAILABLE','2027-12-31',0),('PT-1H-002',1,'AVAILABLE','2027-12-31',0),
('PT-2H-001',2,'AVAILABLE','2027-12-31',0),('PT-2H-002',2,'AVAILABLE','2027-12-31',0),
('PT-4H-001',4,'AVAILABLE','2027-12-31',0),('PT-4H-002',4,'AVAILABLE','2027-12-31',0),
('PT-6H-001',6,'AVAILABLE','2027-12-31',0),('PT-6H-002',6,'AVAILABLE','2027-12-31',0),
('PT-8H-001',8,'AVAILABLE','2027-12-31',0),('PT-8H-002',8,'AVAILABLE','2027-12-31',0),
('PT-12H-001',12,'AVAILABLE','2027-12-31',0),('PT-12H-002',12,'AVAILABLE','2027-12-31',0);
