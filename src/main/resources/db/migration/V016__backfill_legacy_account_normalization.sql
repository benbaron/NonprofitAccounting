-- Move safe legacy account normalization out of startup compatibility backfills.

UPDATE account
SET code = account_number
WHERE code IS NULL;

UPDATE account
SET normal_balance = CASE
    WHEN UPPER(COALESCE(increase_side, 'DEBIT')) IN ('CREDIT', 'CR') THEN 'CREDIT'
    ELSE 'DEBIT'
END
WHERE normal_balance IS NULL;
