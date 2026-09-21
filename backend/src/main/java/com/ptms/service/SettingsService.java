package com.ptms.service;

import com.ptms.dto.SettingsDto;
import com.ptms.exception.BusinessRuleException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.*;

@Service @Transactional
public class SettingsService {
  private static final Set<Integer> HOURS=Set.of(1,2,4,6,8,12);
  private static final Set<String> RESET_MODES=Set.of("DAY","VALIDITY","MANUAL");
  private final JdbcTemplate db;
  public SettingsService(JdbcTemplate db){this.db=db;}
  private String getValue(String k,String d){List<String>x=db.query("select setting_value from app_setting where setting_key=?",(r,n)->r.getString(1),k);return x.isEmpty()?d:x.get(0);}
  public String value(String key,String fallback){return getValue(key,fallback);}
  public int manualCycle(int hours){return Integer.parseInt(getValue("ticketNumberManualCycle"+hours+"h","1"));}
  public int resetManualCycle(int hours){if(!HOURS.contains(hours))throw new BusinessRuleException("Unsupported ticket duration");int next=manualCycle(hours)+1;put("ticketNumberManualCycle"+hours+"h",String.valueOf(next));return next;}
  public SettingsDto get(){return new SettingsDto(
    getValue("weekdayStart","06:00"),getValue("weekdayEnd","12:00"),getValue("weekendStart","06:00"),getValue("weekendEnd","12:30"),
    intValue("bufferMinutes",0),getValue("ticketTypes","1,2,4,6,8,12"),intValue("warning1h",5),intValue("warning2h",5),intValue("warning4h",5),intValue("warning6h",5),intValue("warning8h",5),intValue("warning12h",5),
    getValue("ticketNumberReset1h","DAY"),getValue("ticketNumberReset2h","DAY"),getValue("ticketNumberReset4h","DAY"),getValue("ticketNumberReset6h","DAY"),getValue("ticketNumberReset8h","DAY"),getValue("ticketNumberReset12h","DAY"),
    intValue("inventoryDefaultDuration",1),getValue("inventoryDefaultExpiryMode","NONE"),getValue("inventoryDefaultEntryMode","PHOTO"));}
  public SettingsDto save(SettingsDto s){
    validateTime(s.weekdayStart(),s.weekdayEnd());validateTime(s.weekendStart(),s.weekendEnd());
    try{for(String x:s.ticketTypes().split(","))if(!HOURS.contains(Integer.parseInt(x.trim())))throw new Exception();}catch(Exception e){throw new BusinessRuleException("Ticket types must use 1, 2, 4, 6, 8 or 12 hours");}
    Map<String,String> values=new LinkedHashMap<>();values.put("weekdayStart",s.weekdayStart());values.put("weekdayEnd",s.weekdayEnd());values.put("weekendStart",s.weekendStart());values.put("weekendEnd",s.weekendEnd());values.put("bufferMinutes",String.valueOf(s.bufferMinutes()));values.put("ticketTypes",s.ticketTypes());
    int[] warnings={s.warning1h(),s.warning2h(),s.warning4h(),s.warning6h(),s.warning8h(),s.warning12h()};int[] hours={1,2,4,6,8,12};String[] modes={s.ticketNumberReset1h(),s.ticketNumberReset2h(),s.ticketNumberReset4h(),s.ticketNumberReset6h(),s.ticketNumberReset8h(),s.ticketNumberReset12h()};
    for(int i=0;i<hours.length;i++){String mode=modes[i].toUpperCase(Locale.ROOT);if(!RESET_MODES.contains(mode))throw new BusinessRuleException("Reset mode must be DAY, VALIDITY or MANUAL");values.put("warning"+hours[i]+"h",String.valueOf(warnings[i]));values.put("ticketNumberReset"+hours[i]+"h",mode);}
    if(!HOURS.contains(s.inventoryDefaultDuration()))throw new BusinessRuleException("Invalid default ticket duration");if(!Set.of("NONE","DATE").contains(s.inventoryDefaultExpiryMode()))throw new BusinessRuleException("Invalid default expiry mode");if(!Set.of("PHOTO","SCANNER","MANUAL").contains(s.inventoryDefaultEntryMode()))throw new BusinessRuleException("Invalid default inventory entry mode");
    values.put("inventoryDefaultDuration",String.valueOf(s.inventoryDefaultDuration()));values.put("inventoryDefaultExpiryMode",s.inventoryDefaultExpiryMode());values.put("inventoryDefaultEntryMode",s.inventoryDefaultEntryMode());values.forEach(this::put);return get();
  }
  private int intValue(String key,int fallback){return Integer.parseInt(getValue(key,String.valueOf(fallback)));}
  private void validateTime(String a,String b){if(!LocalTime.parse(b).isAfter(LocalTime.parse(a)))throw new BusinessRuleException("Parking pass end time must be after start time");}
  private void put(String k,String v){db.update("insert into app_setting(setting_key,setting_value) values(?,?) on duplicate key update setting_value=values(setting_value)",k,v);}
}
