package com.ptms.service;

import com.ptms.dto.SettingsDto;
import com.ptms.exception.BusinessRuleException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @Transactional
public class SettingsService {
  private static final Set<Integer> HOURS=Set.of(1,2,4,6,8,12);
  private static final Set<String> RESET_MODES=Set.of("DAY","VALIDITY","DAYS","DATE_RANGE","MANUAL");
  private final JdbcTemplate db;
  public SettingsService(JdbcTemplate db){this.db=db;}
  private String getValue(String k,String d){List<String>x=db.query("select setting_value from app_setting where setting_key=?",(r,n)->r.getString(1),k);return x.isEmpty()?d:x.get(0);}
  public String value(String key,String fallback){return getValue(key,fallback);}
  public int manualCycle(){return Integer.parseInt(getValue("ticketNumberManualCycle","1"));}
  public int resetManualCycle(){int next=manualCycle()+1;put("ticketNumberManualCycle",String.valueOf(next));return next;}
  public SettingsDto get(){return new SettingsDto(
    getValue("weekdayStart","06:00"),getValue("weekdayEnd","12:00"),getValue("weekendStart","06:00"),getValue("weekendEnd","12:30"),
    intValue("bufferMinutes",0),getValue("ticketTypes","1,2,4,6,8,12"),intValue("warning1h",5),intValue("warning2h",5),intValue("warning4h",5),intValue("warning6h",5),intValue("warning8h",5),intValue("warning12h",5),
    getValue("ticketNumberResetMode","DAY"),intValue("ticketNumberResetPeriodDays",7),getValue("ticketNumberResetStartDate",""),getValue("ticketNumberResetEndDate",""),
    intValue("inventoryDefaultDuration",1),getValue("inventoryDefaultExpiryMode","NONE"),getValue("inventoryDefaultEntryMode","PHOTO"),getValue("defaultIssueMode","NORMAL"),getValue("defaultPersonType","EMPLOYEE"),getValue("defaultDurationMode","HOURS"),getValue("defaultBarcodeMode","SCAN"),intValue("defaultRushHours",1),
    boolValue("quickBatchSecurityEnabled",true),boolValue("quickBatchAdminEnabled",true),boolValue("inventoryPhotoSecurityEnabled",true),boolValue("inventoryPhotoAdminEnabled",true),boolValue("inventoryScannerSecurityEnabled",true),boolValue("inventoryScannerAdminEnabled",true),boolValue("inventoryManualSecurityEnabled",true),boolValue("inventoryManualAdminEnabled",true));}
  public SettingsDto save(SettingsDto s){
    validateTime(s.weekdayStart(),s.weekdayEnd());validateTime(s.weekendStart(),s.weekendEnd());
    try{for(String x:s.ticketTypes().split(","))if(!HOURS.contains(Integer.parseInt(x.trim())))throw new Exception();}catch(Exception e){throw new BusinessRuleException("Ticket types must use 1, 2, 4, 6, 8 or 12 hours");}
    Map<String,String> values=new LinkedHashMap<>();values.put("weekdayStart",s.weekdayStart());values.put("weekdayEnd",s.weekdayEnd());values.put("weekendStart",s.weekendStart());values.put("weekendEnd",s.weekendEnd());values.put("bufferMinutes",String.valueOf(s.bufferMinutes()));values.put("ticketTypes",s.ticketTypes());
    int[] warnings={s.warning1h(),s.warning2h(),s.warning4h(),s.warning6h(),s.warning8h(),s.warning12h()};int[] hours={1,2,4,6,8,12};for(int i=0;i<hours.length;i++)values.put("warning"+hours[i]+"h",String.valueOf(warnings[i]));
    String mode=s.ticketNumberResetMode().toUpperCase(Locale.ROOT);if(!RESET_MODES.contains(mode))throw new BusinessRuleException("Invalid ticket number reset mode");
    if("DAYS".equals(mode)&&s.ticketNumberResetPeriodDays()<1)throw new BusinessRuleException("Reset period must be at least 1 day");
    if("DATE_RANGE".equals(mode)){LocalDate start=parseDate(s.ticketNumberResetStartDate(),"Reset period start date is required");LocalDate end=parseDate(s.ticketNumberResetEndDate(),"Reset period end date is required");if(end.isBefore(start))throw new BusinessRuleException("Reset period end date cannot be before its start date");}
    String previousMode=getValue("ticketNumberResetMode","DAY"),previousDays=getValue("ticketNumberResetPeriodDays","7");if("DAYS".equals(mode)&&(!mode.equals(previousMode)||!String.valueOf(s.ticketNumberResetPeriodDays()).equals(previousDays)))values.put("ticketNumberResetPeriodAnchor",LocalDate.now().toString());
    values.put("ticketNumberResetMode",mode);values.put("ticketNumberResetPeriodDays",String.valueOf(s.ticketNumberResetPeriodDays()));values.put("ticketNumberResetStartDate",Objects.toString(s.ticketNumberResetStartDate(),""));values.put("ticketNumberResetEndDate",Objects.toString(s.ticketNumberResetEndDate(),""));
    if(!HOURS.contains(s.inventoryDefaultDuration()))throw new BusinessRuleException("Invalid default ticket duration");if(!Set.of("NONE","DATE").contains(s.inventoryDefaultExpiryMode()))throw new BusinessRuleException("Invalid default expiry mode");if(!Set.of("PHOTO","SCANNER","MANUAL").contains(s.inventoryDefaultEntryMode()))throw new BusinessRuleException("Invalid default inventory entry mode");
    if(!Set.of("NORMAL","QUICK").contains(s.defaultIssueMode())||!Set.of("EMPLOYEE","VISITOR").contains(s.defaultPersonType())||!Set.of("HOURS","TIME").contains(s.defaultDurationMode())||!Set.of("SCAN","MANUAL").contains(s.defaultBarcodeMode()))throw new BusinessRuleException("Invalid issue-screen default setting");
    values.put("inventoryDefaultDuration",String.valueOf(s.inventoryDefaultDuration()));values.put("inventoryDefaultExpiryMode",s.inventoryDefaultExpiryMode());values.put("inventoryDefaultEntryMode",s.inventoryDefaultEntryMode());values.put("defaultIssueMode",s.defaultIssueMode());values.put("defaultPersonType",s.defaultPersonType());values.put("defaultDurationMode",s.defaultDurationMode());values.put("defaultBarcodeMode",s.defaultBarcodeMode());values.put("defaultRushHours",String.valueOf(s.defaultRushHours()));
    values.put("quickBatchSecurityEnabled",String.valueOf(s.quickBatchSecurityEnabled()));values.put("quickBatchAdminEnabled",String.valueOf(s.quickBatchAdminEnabled()));values.put("inventoryPhotoSecurityEnabled",String.valueOf(s.inventoryPhotoSecurityEnabled()));values.put("inventoryPhotoAdminEnabled",String.valueOf(s.inventoryPhotoAdminEnabled()));values.put("inventoryScannerSecurityEnabled",String.valueOf(s.inventoryScannerSecurityEnabled()));values.put("inventoryScannerAdminEnabled",String.valueOf(s.inventoryScannerAdminEnabled()));values.put("inventoryManualSecurityEnabled",String.valueOf(s.inventoryManualSecurityEnabled()));values.put("inventoryManualAdminEnabled",String.valueOf(s.inventoryManualAdminEnabled()));values.forEach(this::put);return get();
  }
  private int intValue(String key,int fallback){return Integer.parseInt(getValue(key,String.valueOf(fallback)));}
  private boolean boolValue(String key,boolean fallback){return Boolean.parseBoolean(getValue(key,String.valueOf(fallback)));}
  private LocalDate parseDate(String value,String message){try{return LocalDate.parse(value);}catch(Exception e){throw new BusinessRuleException(message);}}
  private void validateTime(String a,String b){if(!LocalTime.parse(b).isAfter(LocalTime.parse(a)))throw new BusinessRuleException("Parking pass end time must be after start time");}
  private void put(String k,String v){db.update("insert into app_setting(setting_key,setting_value) values(?,?) on duplicate key update setting_value=values(setting_value)",k,v);}
}
