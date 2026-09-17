-- Allow ticket history rows to keep the original issued barcode while still allowing
-- the same physical ticket to be returned to inventory and reused after an Admin/Super User
-- edits, cancels, or deletes a completed FIFO register record.
--
-- MySQL/InnoDB uses the unique parking_ticket_id index for the foreign key, so the
-- foreign key must be dropped first before replacing that unique index with a normal index.

ALTER TABLE ticket_issue_item DROP FOREIGN KEY fk_issue_item_ticket;
ALTER TABLE ticket_issue_item DROP INDEX parking_ticket_id;
CREATE INDEX idx_issue_item_ticket ON ticket_issue_item(parking_ticket_id);
ALTER TABLE ticket_issue_item
  ADD CONSTRAINT fk_issue_item_ticket
  FOREIGN KEY (parking_ticket_id) REFERENCES parking_ticket(id);
