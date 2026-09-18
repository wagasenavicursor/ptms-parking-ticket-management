package com.ptms.domain;
import jakarta.persistence.*;
@Entity @Table(name="team")
public class Team {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String name;
 private String department;
 private String description;
 @Column(nullable=false) private boolean enabled=true;
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
}
