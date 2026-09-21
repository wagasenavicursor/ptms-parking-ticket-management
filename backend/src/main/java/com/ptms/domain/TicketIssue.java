package com.ptms.domain;
import jakarta.persistence.*; import java.time.*; import java.util.*;
@Entity @Table(name="ticket_issue")
public class TicketIssue {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="request_number",nullable=false,unique=true) private String requestNumber;
 @Column(name="issue_mode",nullable=false) private String issueMode="NORMAL";
 @Column(name="rush_batch_reference") private String rushBatchReference;
 @Enumerated(EnumType.STRING) @Column(name="person_type",nullable=false) private PersonType personType;
 @Column(name="person_reference",nullable=false) private String personReference;
 @Column(name="visit_date",nullable=false) private LocalDate visitDate; @Column(name="entry_time") private LocalTime entryTime; @Column(name="exit_time") private LocalTime exitTime;
 @Column(name="extra_hours",nullable=false) private int extraHours; @Column(name="required_hours",nullable=false) private int requiredHours; private String reason;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private IssueStatus status=IssueStatus.PENDING;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now(); @Column(name="completed_at") private LocalDateTime completedAt;
 @OneToMany(mappedBy="issue",cascade=CascadeType.ALL,orphanRemoval=true) private List<TicketIssueItem> items=new ArrayList<>();
 public void addTicket(ParkingTicket t){TicketIssueItem i=new TicketIssueItem();i.setIssue(this);i.setTicket(t);items.add(i);} public Long getId(){return id;} public String getRequestNumber(){return requestNumber;} public void setRequestNumber(String v){requestNumber=v;}
 public String getIssueMode(){return issueMode;} public void setIssueMode(String v){issueMode=v;} public String getRushBatchReference(){return rushBatchReference;} public void setRushBatchReference(String v){rushBatchReference=v;}
 public PersonType getPersonType(){return personType;} public void setPersonType(PersonType v){personType=v;} public String getPersonReference(){return personReference;} public void setPersonReference(String v){personReference=v;}
 public LocalDate getVisitDate(){return visitDate;} public void setVisitDate(LocalDate v){visitDate=v;} public LocalTime getEntryTime(){return entryTime;} public void setEntryTime(LocalTime v){entryTime=v;} public LocalTime getExitTime(){return exitTime;} public void setExitTime(LocalTime v){exitTime=v;}
 public int getExtraHours(){return extraHours;} public void setExtraHours(int v){extraHours=v;} public int getRequiredHours(){return requiredHours;} public void setRequiredHours(int v){requiredHours=v;} public String getReason(){return reason;} public void setReason(String v){reason=v;}
 public IssueStatus getStatus(){return status;} public void setStatus(IssueStatus v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;} public List<TicketIssueItem> getItems(){return items;}
}
