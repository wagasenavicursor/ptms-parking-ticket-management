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
  @Min(1) int defaultRushHours,
  boolean quickBatchSecurityEnabled,
  boolean quickBatchAdminEnabled,
  boolean inventoryPhotoSecurityEnabled,
  boolean inventoryPhotoAdminEnabled,
  boolean inventoryScannerSecurityEnabled,
  boolean inventoryScannerAdminEnabled,
  boolean inventoryManualSecurityEnabled,
  boolean inventoryManualAdminEnabled,
  @NotBlank String uiTheme,
  @NotBlank String defaultRegisterView
){
  public SettingsDto(
    String weekdayStart,String weekdayEnd,String weekendStart,String weekendEnd,
    int bufferMinutes,String ticketTypes,
    int warning1h,int warning2h,int warning4h,int warning6h,int warning8h,int warning12h,
    String ticketNumberResetMode,int ticketNumberResetPeriodDays,String ticketNumberResetStartDate,String ticketNumberResetEndDate,
    int inventoryDefaultDuration,String inventoryDefaultExpiryMode,String inventoryDefaultEntryMode,
    String defaultIssueMode,String defaultPersonType,String defaultDurationMode,String defaultBarcodeMode,int defaultRushHours,
    boolean quickBatchSecurityEnabled,boolean quickBatchAdminEnabled,
    boolean inventoryPhotoSecurityEnabled,boolean inventoryPhotoAdminEnabled,
    boolean inventoryScannerSecurityEnabled,boolean inventoryScannerAdminEnabled,
    boolean inventoryManualSecurityEnabled,boolean inventoryManualAdminEnabled
  ){
    this(weekdayStart,weekdayEnd,weekendStart,weekendEnd,bufferMinutes,ticketTypes,
      warning1h,warning2h,warning4h,warning6h,warning8h,warning12h,
      ticketNumberResetMode,ticketNumberResetPeriodDays,ticketNumberResetStartDate,ticketNumberResetEndDate,
      inventoryDefaultDuration,inventoryDefaultExpiryMode,inventoryDefaultEntryMode,
      defaultIssueMode,defaultPersonType,defaultDurationMode,defaultBarcodeMode,defaultRushHours,
      quickBatchSecurityEnabled,quickBatchAdminEnabled,
      inventoryPhotoSecurityEnabled,inventoryPhotoAdminEnabled,
      inventoryScannerSecurityEnabled,inventoryScannerAdminEnabled,
      inventoryManualSecurityEnabled,inventoryManualAdminEnabled,"COLOR","ISSUED");
  }
}
