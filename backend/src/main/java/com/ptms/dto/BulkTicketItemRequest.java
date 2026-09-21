package com.ptms.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BulkTicketItemRequest(
  @NotBlank String barcode,
  String physicalTicketNumber,
  @Min(1) int durationHours,
  @Min(1) Integer ticketNumber,
  LocalDate stockIssueDate,
  LocalDate expiryDate
) {}
