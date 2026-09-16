package com.ptms.service;
import com.ptms.domain.*;import com.ptms.dto.*;import com.ptms.exception.*;import com.ptms.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @Transactional public class TicketInventoryService{
 private static final Set<Integer> TYPES=Set.of(1,2,4,6,8,12); private final ParkingTicketRepository r;
 public TicketInventoryService(ParkingTicketRepository r){this.r=r;}
 public List<TicketDto>all(){return r.findAll().stream().map(this::dto).toList();}
 public List<TicketDto>remaining(){return r.findByStatusOrderByDurationHoursAscBarcodeAsc(TicketStatus.AVAILABLE).stream().map(this::dto).toList();}
 public TicketDto create(TicketDto q){validate(q);if(r.findByBarcodeIgnoreCase(q.barcode().trim()).isPresent())throw new BusinessRuleException("Duplicate barcode: "+q.barcode());ParkingTicket t=new ParkingTicket();apply(t,q);return dto(r.save(t));}
 public TicketDto update(Long id,TicketDto q){validate(q);ParkingTicket t=r.findById(id).orElseThrow(()->new ResourceNotFoundException("Ticket not found"));r.findByBarcodeIgnoreCase(q.barcode().trim()).filter(x->!x.getId().equals(id)).ifPresent(x->{throw new BusinessRuleException("Duplicate barcode: "+q.barcode());});if(t.getStatus()==TicketStatus.ISSUED&&(q.status()==null||q.status()!=TicketStatus.ISSUED))throw new BusinessRuleException("Issued ticket status cannot be reset from inventory");apply(t,q);return dto(r.save(t));}
 public void delete(Long id){ParkingTicket t=r.findById(id).orElseThrow(()->new ResourceNotFoundException("Ticket not found"));if(t.getStatus()==TicketStatus.ISSUED)throw new BusinessRuleException("Issued tickets cannot be deleted");r.delete(t);}
 public List<TicketDto>bulk(BulkTicketScanRequest q){if(!TYPES.contains(q.durationHours()))throw new BusinessRuleException("Unsupported ticket type");Set<String>s=new HashSet<>();List<TicketDto>out=new ArrayList<>();for(String raw:q.barcodes()){String b=raw.trim();if(b.isBlank())continue;if(!s.add(b.toLowerCase())||r.findByBarcodeIgnoreCase(b).isPresent())throw new BusinessRuleException("Duplicate barcode: "+b);ParkingTicket t=new ParkingTicket();t.setBarcode(b);t.setDurationHours(q.durationHours());t.setExpiryDate(q.expiryDate());out.add(dto(r.save(t)));}return out;}
 private void validate(TicketDto q){if(q.barcode()==null||q.barcode().isBlank())throw new BusinessRuleException("Barcode is required");if(!TYPES.contains(q.durationHours()))throw new BusinessRuleException("Ticket type must be 1, 2, 4, 6, 8 or 12 hours");}
 private void apply(ParkingTicket t,TicketDto q){t.setBarcode(q.barcode().trim());t.setDurationHours(q.durationHours());t.setExpiryDate(q.expiryDate());if(q.status()!=null)t.setStatus(q.status());}
 private TicketDto dto(ParkingTicket t){return new TicketDto(t.getId(),t.getBarcode(),t.getDurationHours(),t.getStatus(),t.getExpiryDate(),t.getIssuedAt(),t.getIssuedToReference());}
}
