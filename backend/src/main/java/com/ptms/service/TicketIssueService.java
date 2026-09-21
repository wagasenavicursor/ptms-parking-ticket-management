package com.ptms.service;

import com.ptms.domain.*;
import com.ptms.dto.*;
import com.ptms.exception.*;
import com.ptms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class TicketIssueService {
  private final TicketIssueRepository ir;
  private final ParkingTicketRepository tr;
  private final EmployeeRepository er;
  private final VisitorRepository vr;
  private final ParkingDurationService ds;
  private final TicketCombinationService cs;

  public TicketIssueService(TicketIssueRepository ir, ParkingTicketRepository tr, EmployeeRepository er, VisitorRepository vr, ParkingDurationService ds, TicketCombinationService cs) {
    this.ir = ir;
    this.tr = tr;
    this.er = er;
    this.vr = vr;
    this.ds = ds;
    this.cs = cs;
  }

  public IssuePreviewResponse preview(IssuePreviewRequest q) {
    boolean pass = hasParkingPass(q.personType(), q.personReference());
    int h;
    if (q.manualHours() != null) {
      h = q.manualHours();
    } else {
      if (q.entryTime() == null || q.exitTime() == null) throw new BusinessRuleException("Entry and exit time are required when using time range mode");
      h = ds.requiredTicketHours(q.personType(), pass, q.visitDate(), q.entryTime(), q.exitTime(), q.extraHours());
    }
    List<List<Integer>> available = cs.combinationsFor(h).stream().filter(this::inventoryCanSupply).toList();
    if (h > 0 && available.isEmpty()) throw new BusinessRuleException("No available ticket combination can cover " + h + " hour(s) with current inventory");
    return new IssuePreviewResponse(h, available);
  }

  private boolean hasParkingPass(PersonType type, String personReference) {
    if (type == PersonType.EMPLOYEE) {
      return er.findByEmployeeCode(personReference).orElseThrow(() -> new ResourceNotFoundException("Employee not found")).isParkingPass();
    }
    vr.findByVisitorCode(personReference).orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));
    return false;
  }

  private boolean inventoryCanSupply(List<Integer> combo) {
    Map<Integer, Long> need = new HashMap<>();
    combo.forEach(x -> need.merge(x, 1L, Long::sum));
    return need.entrySet().stream().allMatch(e -> tr.countByStatusAndDurationHours(TicketStatus.AVAILABLE, e.getKey()) >= e.getValue());
  }

  public IssueResponse create(CreateIssueRequest q) {
    IssuePreviewResponse p = preview(new IssuePreviewRequest(q.personType(), q.personReference(), q.visitDate(), q.entryTime(), q.exitTime(), q.extraHours(), q.manualHours()));
    if (q.selectedCombination().stream().mapToInt(Integer::intValue).sum() != p.requiredHours() || !p.combinations().contains(q.selectedCombination())) throw new BusinessRuleException("Invalid or unavailable ticket combination");
    if (new HashSet<>(q.barcodes().stream().map(String::toLowerCase).toList()).size() != q.barcodes().size()) throw new BusinessRuleException("Duplicate barcode in request");
    if (q.barcodes().size() != q.selectedCombination().size()) throw new BusinessRuleException("Barcode count mismatch");

    List<ParkingTicket> ts = q.barcodes().stream()
      .map(b -> tr.findByBarcodeIgnoreCase(b).orElseThrow(() -> new ResourceNotFoundException("Barcode not found: " + b)))
      .toList();

    if (ts.stream().anyMatch(t -> t.getStatus() != TicketStatus.AVAILABLE)) throw new BusinessRuleException("All tickets must be available");
    if (ts.stream().anyMatch(t -> ir.countByTicketIdAndStatus(t.getId(), IssueStatus.PENDING) > 0)) throw new BusinessRuleException("One or more tickets are already assigned to a pending FIFO request");
    if (ts.stream().anyMatch(t -> t.getExpiryDate() != null && t.getExpiryDate().isBefore(q.visitDate()))) throw new BusinessRuleException("Expired ticket cannot be issued");

    List<Integer> actual = ts.stream().map(ParkingTicket::getDurationHours).sorted().toList();
    List<Integer> expected = q.selectedCombination().stream().sorted().toList();
    if (!actual.equals(expected)) throw new BusinessRuleException("Scanned ticket types do not match combination");

    TicketIssue i = new TicketIssue();
    i.setRequestNumber("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    i.setPersonType(q.personType());
    i.setPersonReference(q.personReference());
    i.setVisitDate(q.visitDate());
    i.setEntryTime(q.entryTime());
    i.setExitTime(q.exitTime());
    i.setExtraHours(q.extraHours() == null ? 0 : q.extraHours());
    i.setRequiredHours(p.requiredHours());
    i.setReason(allowedReason(q.personType(), q.personReference(), q.reason()));
    ts.forEach(i::addTicket);
    return dto(ir.save(i));
  }

  public IssueResponse createRush(RushIssueRequest q) {
    hasParkingPass(PersonType.EMPLOYEE, q.employeeReference());
    if (cs.combinationsFor(q.requiredHours()).isEmpty()) throw new BusinessRuleException("Required hours cannot be covered by configured ticket types");
    TicketIssue i = new TicketIssue();
    i.setRequestNumber("RUSH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    i.setPersonType(PersonType.EMPLOYEE);
    i.setPersonReference(q.employeeReference());
    i.setVisitDate(q.visitDate());
    i.setEntryTime(null);
    i.setExitTime(null);
    i.setExtraHours(0);
    i.setRequiredHours(q.requiredHours());
    i.setReason(allowedReason(PersonType.EMPLOYEE, q.employeeReference(), q.reason()));
    return dto(ir.save(i));
  }

  public IssueResponse finalizeRush(Long id, FinalizeRushIssueRequest q) {
    TicketIssue i = ir.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rush request not found"));
    if (i.getStatus() != IssueStatus.PENDING || !i.getItems().isEmpty()) throw new BusinessRuleException("Only an unallocated rush request can be finalized");
    List<String> barcodes = q.barcodes().stream().map(String::trim).filter(x -> !x.isBlank()).toList();
    if (new HashSet<>(barcodes.stream().map(String::toLowerCase).toList()).size() != barcodes.size()) throw new BusinessRuleException("Duplicate barcode in request");
    List<ParkingTicket> tickets = barcodes.stream().map(b -> tr.findByBarcodeIgnoreCase(b).orElseThrow(() -> new ResourceNotFoundException("Barcode not found: " + b))).toList();
    if (tickets.stream().anyMatch(t -> t.getStatus() != TicketStatus.AVAILABLE)) throw new BusinessRuleException("All tickets must be available");
    if (tickets.stream().anyMatch(t -> ir.countByTicketIdAndStatus(t.getId(), IssueStatus.PENDING) > 0)) throw new BusinessRuleException("One or more tickets are already assigned to another pending request");
    if (tickets.stream().anyMatch(t -> t.getExpiryDate() != null && t.getExpiryDate().isBefore(i.getVisitDate()))) throw new BusinessRuleException("Expired ticket cannot be issued");
    List<Integer> actual = tickets.stream().map(ParkingTicket::getDurationHours).sorted().toList();
    boolean valid = cs.combinationsFor(i.getRequiredHours()).stream().map(x -> x.stream().sorted().toList()).anyMatch(actual::equals);
    if (!valid) throw new BusinessRuleException("Ticket types do not cover the required " + i.getRequiredHours() + " hour(s)");
    tickets.forEach(i::addTicket);
    LocalDateTime now = LocalDateTime.now();
    tickets.forEach(t -> { t.setStatus(TicketStatus.ISSUED); t.setIssuedAt(now); t.setIssuedToReference(i.getPersonReference()); });
    i.setStatus(IssueStatus.COMPLETED);
    i.setCompletedAt(now);
    return dto(ir.save(i));
  }

  public IssueResponse update(Long id, UpdateIssueRequest q, boolean canManageClosed) {
    TicketIssue i = ir.findById(id).orElseThrow();
    ensureClosedAccess(i, canManageClosed, "change");
    boolean pass = hasParkingPass(i.getPersonType(), i.getPersonReference());
    int h = ds.requiredTicketHours(i.getPersonType(), pass, q.visitDate(), q.entryTime(), q.exitTime(), q.extraHours());
    i.setVisitDate(q.visitDate());
    i.setEntryTime(q.entryTime());
    i.setExitTime(q.exitTime());
    i.setExtraHours(q.extraHours() == null ? 0 : q.extraHours());
    i.setRequiredHours(h);
    i.setReason(q.reason());
    return dto(ir.save(i));
  }

  public IssueResponse complete(Long id) {
    TicketIssue i = ir.findById(id).orElseThrow();
    if (i.getStatus() != IssueStatus.PENDING) throw new BusinessRuleException("Only pending requests can be completed");
    if (i.getItems().isEmpty()) throw new BusinessRuleException("Assign physical tickets before completing this rush request");
    LocalDateTime now = LocalDateTime.now();
    for (TicketIssueItem x : i.getItems()) {
      ParkingTicket t = x.getTicket();
      if (t.getStatus() != TicketStatus.AVAILABLE) throw new BusinessRuleException("Ticket no longer available");
      t.setStatus(TicketStatus.ISSUED);
      t.setIssuedAt(now);
      t.setIssuedToReference(i.getPersonReference());
    }
    i.setStatus(IssueStatus.COMPLETED);
    i.setCompletedAt(now);
    return dto(ir.save(i));
  }

  public IssueResponse cancel(Long id) {
    TicketIssue i = ir.findById(id).orElseThrow();
    if (i.getStatus() != IssueStatus.PENDING) throw new BusinessRuleException("Only pending requests can be cancelled");
    i.setStatus(IssueStatus.CANCELLED);
    i.setCompletedAt(LocalDateTime.now());
    return dto(ir.save(i));
  }

  public void delete(Long id, boolean canManageClosed) {
    TicketIssue i = ir.findById(id).orElseThrow();
    ensureClosedAccess(i, canManageClosed, "delete");
    if (i.getStatus() == IssueStatus.COMPLETED) releaseCompletedTickets(i);
    ir.delete(i);
  }

  private void ensureClosedAccess(TicketIssue i, boolean canManageClosed, String action) {
    if (isClosed(i) && !canManageClosed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Super User or Admin can " + action + " issued register FIFO records");
    }
  }

  private boolean isClosed(TicketIssue i) {
    return i.getStatus() == IssueStatus.COMPLETED || i.getStatus() == IssueStatus.CANCELLED;
  }

  private String allowedReason(PersonType type, String reference, String reason) {
    if (reason == null || reason.isBlank()) return null;
    if (type == PersonType.VISITOR) return reason.trim();
    return er.findByEmployeeCode(reference).filter(Employee::isParkingPass).map(e -> reason.trim()).orElse(null);
  }

  private void releaseCompletedTickets(TicketIssue i) {
    for (TicketIssueItem x : i.getItems()) {
      ParkingTicket t = x.getTicket();
      if (t.getStatus() == TicketStatus.ISSUED && Objects.equals(t.getIssuedToReference(), i.getPersonReference())) {
        t.setStatus(TicketStatus.AVAILABLE);
        t.setIssuedAt(null);
        t.setIssuedToReference(null);
      }
    }
  }

  public List<IssueResponse> all() {
    return ir.findAllByOrderByCreatedAtDesc().stream().map(this::dto).toList();
  }

  private IssueResponse dto(TicketIssue i) {
    return new IssueResponse(
      i.getId(),
      i.getRequestNumber(),
      i.getPersonType(),
      i.getPersonReference(),
      i.getVisitDate(),
      i.getEntryTime(),
      i.getExitTime(),
      i.getExtraHours(),
      i.getRequiredHours(),
      i.getReason(),
      i.getStatus(),
      i.getCreatedAt(),
      i.getCompletedAt(),
      i.getItems().stream().map(x -> x.getTicket().getBarcode()).toList()
    );
  }
}
