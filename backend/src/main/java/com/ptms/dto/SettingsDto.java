package com.ptms.dto;
import jakarta.validation.constraints.*;

public record SettingsDto(
  @NotBlank String weekdayStart,
  @NotBlank String weekdayEnd,
  @NotBlank String weekendStart,
  @NotBlank String weekendEnd,
  @Min(0) int bufferMinutes,
  @NotBlank String ticketTypes,
  @Min(0) int warning1h,
  @Min(0) int warning2h,
  @Min(0) int warning4h,
  @Min(0) int warning6h,
  @Min(0) int warning8h,
  @Min(0) int warning12h,
  @NotBlank String ticketNumberResetMode,
  @Min(1) int ticketNumberResetPeriodDays,
  String ticketNumberResetStartDate,
  String ticketNumberResetEndDate,
  @Min(1) int inventoryDefaultDuration,
  @NotBlank String inventoryDefaultExpiryMode,
  @NotBlank String inventoryDefaultEntryMode,
  @NotBlank String defaultIssueMode,
  @NotBlank String defaultPersonType,
  @NotBlank String defaultDurationMode,
  @NotBlank String defaultBarcodeMode,
  @Min(1) int defaultRushHours
){}
