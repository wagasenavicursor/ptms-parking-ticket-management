ALTER TABLE ticket_issue_item DROP INDEX parking_ticket_id;
CREATE INDEX idx_issue_item_ticket ON ticket_issue_item(parking_ticket_id);
