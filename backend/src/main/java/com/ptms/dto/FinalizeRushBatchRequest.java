package com.ptms.dto;
import jakarta.validation.constraints.*;
import java.util.*;
public record FinalizeRushBatchRequest(@NotEmpty Map<@NotNull Long,@NotEmpty List<@NotBlank String>> assignments) {}
