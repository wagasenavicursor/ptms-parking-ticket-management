package com.ptms.domain; import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="reconciliation") public class Reconciliation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="reconciliation_date",nullable=false) private LocalDate reconciliationDate; @Column(name="performed_at",nullable=false) private LocalDateTime performedAt;
 @Column(name="expected_available",nullable=false) private int expectedAvailable; @Column(name="scanned_count",nullable=false) private int scannedCount; @Column(name="missing_count",nullable=false) private int missingCount; @Column(name="unknown_count",nullable=false) private int unknownCount;
 public void setReconciliationDate(LocalDate v){reconciliationDate=v;} public void setPerformedAt(LocalDateTime v){performedAt=v;} public void setExpectedAvailable(int v){expectedAvailable=v;} public void setScannedCount(int v){scannedCount=v;} public void setMissingCount(int v){missingCount=v;} public void setUnknownCount(int v){unknownCount=v;}
}
