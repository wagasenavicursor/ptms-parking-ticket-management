package com.ptms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_time", nullable = false)
  private LocalDateTime eventTime;

  @Column(nullable = false, length = 120)
  private String username;

  @Column(name = "user_role", length = 40)
  private String userRole;

  @Column(nullable = false, length = 40)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 80)
  private String entityType;

  @Column(name = "entity_id", length = 120)
  private String entityId;

  @Column(nullable = false, length = 500)
  private String summary;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String details;

  public Long getId(){return id;}
  public LocalDateTime getEventTime(){return eventTime;}
  public void setEventTime(LocalDateTime v){eventTime=v;}
  public String getUsername(){return username;}
  public void setUsername(String v){username=v;}
  public String getUserRole(){return userRole;}
  public void setUserRole(String v){userRole=v;}
  public String getAction(){return action;}
  public void setAction(String v){action=v;}
  public String getEntityType(){return entityType;}
  public void setEntityType(String v){entityType=v;}
  public String getEntityId(){return entityId;}
  public void setEntityId(String v){entityId=v;}
  public String getSummary(){return summary;}
  public void setSummary(String v){summary=v;}
  public String getDetails(){return details;}
  public void setDetails(String v){details=v;}
}
