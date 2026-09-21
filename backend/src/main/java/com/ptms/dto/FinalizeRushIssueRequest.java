package com.ptms.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record FinalizeRushIssueRequest(
  @NotEmpty List<@NotBlank String> barcodes
) {}
