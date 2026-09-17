package com.ptms.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_issue_item")
public class TicketIssueItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ticket_issue_id")
  private TicketIssue issue;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "parking_ticket_id")
  private ParkingTicket ticket;

  public void setIssue(TicketIssue v) { issue = v; }
  public TicketIssue getIssue() { return issue; }
  public ParkingTicket getTicket() { return ticket; }
  public void setTicket(ParkingTicket v) { ticket = v; }
}
