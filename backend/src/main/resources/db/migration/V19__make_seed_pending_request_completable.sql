-- A normal pending demo request must retain its selected physical ticket so it
-- can be completed directly from the Live FIFO Guard Queue.
INSERT INTO ticket_issue_item(ticket_issue_id, parking_ticket_id)
SELECT i.id, t.id
FROM ticket_issue i
JOIN parking_ticket t ON t.barcode = '4852002'
WHERE i.request_number = 'REQ-3003'
  AND i.issue_mode = 'NORMAL'
  AND i.status = 'PENDING'
  AND t.status = 'AVAILABLE'
  AND NOT EXISTS (
    SELECT 1 FROM ticket_issue_item x
    WHERE x.ticket_issue_id = i.id OR x.parking_ticket_id = t.id
  );
