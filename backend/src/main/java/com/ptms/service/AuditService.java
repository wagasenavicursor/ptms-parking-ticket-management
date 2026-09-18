package com.ptms.service;

import com.ptms.domain.AuditLog;
import com.ptms.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
  private final AuditLogRepository repository;

  public AuditService(AuditLogRepository repository){this.repository=repository;}

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(String username,String role,String action,String entityType,String entityId,String summary,String details){
    AuditLog a=new AuditLog();
    a.setEventTime(LocalDateTime.now());
    a.setUsername(username==null||username.isBlank()?"SYSTEM":username);
    a.setUserRole(role);
    a.setAction(action);
    a.setEntityType(entityType);
    a.setEntityId(entityId);
    a.setSummary(summary);
    a.setDetails(details);
    repository.save(a);
  }

  @Transactional(readOnly = true)
  public List<AuditLog> history(String username,String action,String entityType,LocalDate from,LocalDate to){
    return repository.findTop1000ByOrderByEventTimeDesc().stream()
      .filter(a->blank(username)||a.getUsername().toLowerCase().contains(username.toLowerCase()))
      .filter(a->blank(action)||a.getAction().equalsIgnoreCase(action))
      .filter(a->blank(entityType)||a.getEntityType().equalsIgnoreCase(entityType))
      .filter(a->from==null||!a.getEventTime().toLocalDate().isBefore(from))
      .filter(a->to==null||!a.getEventTime().toLocalDate().isAfter(to))
      .toList();
  }

  private boolean blank(String s){return s==null||s.isBlank();}
}
