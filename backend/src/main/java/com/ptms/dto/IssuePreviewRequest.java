package com.ptms.dto;
import com.ptms.domain.PersonType;
import jakarta.validation.constraints.*;
import java.time.*;

public record IssuePreviewRequest(
  @NotNull PersonType personType,
  @NotBlank String personReference,
  @NotNull LocalDate visitDate,
  LocalTime entryTime,
  LocalTime exitTime,
  @Min(0) Integer extraHours,
  @Min(1) Integer manualHours
){}
