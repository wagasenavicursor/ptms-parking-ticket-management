package com.ptms.domain;
import jakarta.persistence.*; import java.time.LocalTime;
@Entity @Table(name="employee")
public class Employee {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="employee_code",nullable=false,unique=true) private String employeeCode;
 @Column(nullable=false) private String name;
 @Column(name="vehicle_number") private String vehicleNumber;
 private String team; private String department;
 @Column(name="parking_pass",nullable=false) private boolean parkingPass;
 @Column(name="default_entry_time") private LocalTime defaultEntryTime;
 public Long getId(){return id;} public String getEmployeeCode(){return employeeCode;} public void setEmployeeCode(String v){employeeCode=v;}
 public String getName(){return name;} public void setName(String v){name=v;} public String getVehicleNumber(){return vehicleNumber;} public void setVehicleNumber(String v){vehicleNumber=v;}
 public String getTeam(){return team;} public void setTeam(String v){team=v;} public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
 public boolean isParkingPass(){return parkingPass;} public void setParkingPass(boolean v){parkingPass=v;} public LocalTime getDefaultEntryTime(){return defaultEntryTime;} public void setDefaultEntryTime(LocalTime v){defaultEntryTime=v;}
}
