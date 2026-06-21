ALTER TABLE donation_record ADD COLUMN IF NOT EXISTS receipt_required BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE donation_record ADD COLUMN IF NOT EXISTS receipt_sent_at TIMESTAMP;
CREATE INDEX IF NOT EXISTS ix_donation_record_receipt_status ON donation_record(receipt_required, receipt_sent_at);
