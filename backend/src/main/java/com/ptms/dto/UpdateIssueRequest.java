package com.ptms.dto;

import jakarta.validation.constraints.NotNull;
import java.time.*;

public record UpdateIssueRequest(
  @NotNull LocalDate visitDate,
  @NotNull LocalTime entryTime,
  @NotNull LocalTime exitTime,
  Integer extraHours,
  String reason
) {}
