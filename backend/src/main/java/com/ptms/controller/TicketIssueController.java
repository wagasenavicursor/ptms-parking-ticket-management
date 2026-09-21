package com.ptms.controller;

import com.ptms.dto.*;
import com.ptms.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/issues")
public class TicketIssueController {
  private final TicketIssueService s;

  public TicketIssueController(TicketIssueService s) {
    this.s = s;
  }

  @PostMapping("/preview")
  public IssuePreviewResponse preview(@Valid @RequestBody IssuePreviewRequest q) {
    return s.preview(q);
  }

  @PostMapping
  public IssueResponse create(@Valid @RequestBody CreateIssueRequest q) {
    return s.create(q);
  }

  @PostMapping("/rush")
  public IssueResponse createRush(@Valid @RequestBody RushIssueRequest q) {
    return s.createRush(q);
  }

  @PostMapping("/{id}/rush-finalize")
  public IssueResponse finalizeRush(@PathVariable Long id, @Valid @RequestBody FinalizeRushIssueRequest q) {
    return s.finalizeRush(id, q);
  }

  @PutMapping("/{id}/rush-hours")
  public IssueResponse updateRushHours(@PathVariable Long id, @Valid @RequestBody UpdateRushHoursRequest q) {
    return s.updateRushHours(id, q.requiredHours());
  }

  @PostMapping("/rush-batch/finalize")
  public List<IssueResponse> finalizeRushBatch(@Valid @RequestBody FinalizeRushBatchRequest q) {
    return s.finalizeRushBatch(q);
  }

  @PutMapping("/{id}")
  public IssueResponse update(@PathVariable Long id, @Valid @RequestBody UpdateIssueRequest q, HttpSession session) {
    return s.update(id, q, canManageClosed(session));
  }

  @PostMapping("/{id}/complete")
  public IssueResponse complete(@PathVariable Long id) {
    return s.complete(id);
  }

  @PostMapping("/{id}/cancel")
  public IssueResponse cancel(@PathVariable Long id) {
    return s.cancel(id);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, HttpSession session) {
    s.delete(id, canManageClosed(session));
  }

  @GetMapping
  public List<IssueResponse> all() {
    return s.all();
  }

  private boolean canManageClosed(HttpSession session) {
    String role = session == null ? null : (String) session.getAttribute("role");
    return "SUPER_USER".equals(role) || "ADMIN".equals(role);
  }
}
