package com.ptms.domain;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="parking_ticket")
public class ParkingTicket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String barcode;
 @Column(name="physical_ticket_number") private String physicalTicketNumber;
 @Column(name="duration_hours",nullable=false) private int durationHours;
 @Column(name="ticket_number",nullable=false) private Integer ticketNumber;
 @Column(name="stock_issue_date") private LocalDate stockIssueDate;
 @Column(name="ticket_number_cycle",nullable=false) private String ticketNumberCycle;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private TicketStatus status=TicketStatus.AVAILABLE;
 @Column(name="expiry_date") private LocalDate expiryDate; @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt; @Column(name="issued_at") private LocalDateTime issuedAt; @Column(name="issued_to_reference") private String issuedToReference; @Version private long version;
 public Long getId(){return id;} public String getBarcode(){return barcode;} public void setBarcode(String v){barcode=v;} public int getDurationHours(){return durationHours;} public void setDurationHours(int v){durationHours=v;}
 public String getPhysicalTicketNumber(){return physicalTicketNumber;} public void setPhysicalTicketNumber(String v){physicalTicketNumber=v;}
 public Integer getTicketNumber(){return ticketNumber;} public void setTicketNumber(Integer v){ticketNumber=v;}
 public LocalDate getStockIssueDate(){return stockIssueDate;} public void setStockIssueDate(LocalDate v){stockIssueDate=v;} public String getTicketNumberCycle(){return ticketNumberCycle;} public void setTicketNumberCycle(String v){ticketNumberCycle=v;}
 public TicketStatus getStatus(){return status;} public void setStatus(TicketStatus v){status=v;} public LocalDate getExpiryDate(){return expiryDate;} public void setExpiryDate(LocalDate v){expiryDate=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
 @PrePersist public void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();}
 public LocalDateTime getIssuedAt(){return issuedAt;} public void setIssuedAt(LocalDateTime v){issuedAt=v;} public String getIssuedToReference(){return issuedToReference;} public void setIssuedToReference(String v){issuedToReference=v;}
}
