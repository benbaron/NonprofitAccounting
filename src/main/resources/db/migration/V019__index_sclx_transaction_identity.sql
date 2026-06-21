-- Supports idempotent SCLX transaction imports.
-- Imported transaction identities are stored as transaction_info rows with
-- k = 'sclx.transactionId'; retries use the same journal_transaction id.
CREATE INDEX IF NOT EXISTS ix_transaction_info_key_value
    ON transaction_info(k, v);
