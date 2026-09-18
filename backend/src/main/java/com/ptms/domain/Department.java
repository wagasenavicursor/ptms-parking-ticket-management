package com.ptms.domain;
import jakarta.persistence.*;
@Entity @Table(name="department")
public class Department {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String name;
 private String description;
 @Column(nullable=false) private boolean enabled=true;
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
}
