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
  @Min(0) int warning12h
){}
