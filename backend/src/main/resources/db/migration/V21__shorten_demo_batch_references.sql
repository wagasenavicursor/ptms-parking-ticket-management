-- Bring the existing clean demo batches in line with the daily batch sequence.
UPDATE ticket_issue
SET rush_batch_reference = 'BATCH-260922-01'
WHERE rush_batch_reference = 'BATCH-20260922083000';

UPDATE ticket_issue
SET rush_batch_reference = 'BATCH-260922-02'
WHERE rush_batch_reference = 'BATCH-20260922113000';
