package com.ptms.domain;
import jakarta.persistence.*;
@Entity @Table(name="visitor")
public class Visitor {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="visitor_code",nullable=false,unique=true) private String visitorCode;
 @Column(nullable=false) private String name; private String nic;
 @Column(name="vehicle_number") private String vehicleNumber;
 @Column(name="host_department") private String hostDepartment;
 public Long getId(){return id;} public String getVisitorCode(){return visitorCode;} public void setVisitorCode(String v){visitorCode=v;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getNic(){return nic;} public void setNic(String v){nic=v;} public String getVehicleNumber(){return vehicleNumber;} public void setVehicleNumber(String v){vehicleNumber=v;} public String getHostDepartment(){return hostDepartment;} public void setHostDepartment(String v){hostDepartment=v;}
}
