package com.ptms.controller;

import com.ptms.domain.AuditLog;
import com.ptms.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
  private final AuditService service;
  public AuditController(AuditService service){this.service=service;}

  @GetMapping
  public List<AuditLog> history(
      @RequestParam(required=false) String username,
      @RequestParam(required=false) String action,
      @RequestParam(required=false) String entityType,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to){
    return service.history(username,action,entityType,from,to);
  }
}
