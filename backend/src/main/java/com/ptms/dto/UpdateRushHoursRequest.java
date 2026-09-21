package com.ptms.dto;

import jakarta.validation.constraints.Min;

public record UpdateRushHoursRequest(@Min(1) int requiredHours) {}
