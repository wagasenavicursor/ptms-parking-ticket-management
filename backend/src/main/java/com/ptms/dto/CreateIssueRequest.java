package com.ptms.dto;
import com.ptms.domain.PersonType;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public record CreateIssueRequest(
  @NotNull PersonType personType,
  @NotBlank String personReference,
  @NotNull LocalDate visitDate,
  LocalTime entryTime,
  LocalTime exitTime,
  @Min(0) Integer extraHours,
  @Min(1) Integer manualHours,
  String reason,
  @NotEmpty List<Integer> selectedCombination,
  @NotEmpty List<@NotBlank String> barcodes
){}
