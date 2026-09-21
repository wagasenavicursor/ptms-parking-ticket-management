package com.ptms.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RushIssueRequest(
  @NotBlank String employeeReference,
  @NotNull LocalDate visitDate,
  @Min(1) int requiredHours,
  String reason,
  String batchReference
) {}
