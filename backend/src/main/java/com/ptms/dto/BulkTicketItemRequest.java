package com.ptms.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BulkTicketItemRequest(
  @NotBlank String barcode,
  @Min(1) int durationHours,
  @Min(1) Integer ticketNumber,
  LocalDate stockIssueDate,
  LocalDate expiryDate
) {}
