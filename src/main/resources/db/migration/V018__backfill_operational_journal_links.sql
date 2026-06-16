-- Move deterministic operational journal-link compatibility backfills out of startup code.

UPDATE donation_record d
SET journal_txn_id = (
  SELECT MIN(jt.id)
  FROM journal_transaction jt
  JOIN journal_entry je
    ON je.txn_id = jt.id
   AND UPPER(COALESCE(je.account_side, 'DEBIT')) = 'CREDIT'
   AND je.amount = d.amount
   AND je.account_number = d.revenue_account_number
  WHERE COALESCE(jt.date_text, '') = COALESCE(CAST(d.donation_date AS VARCHAR(32)), '')
    AND COALESCE(jt.to_from, '') = COALESCE(d.donor_external_id, '')
    AND NOT EXISTS (
      SELECT 1
      FROM donation_record dx
      WHERE dx.donation_id <> d.donation_id
        AND dx.journal_txn_id = jt.id
    )
)
WHERE d.journal_txn_id IS NULL;

INSERT INTO donation_journal_link(donation_id, journal_txn_id, link_role, created_at, updated_at)
SELECT d.donation_id, d.journal_txn_id, 'ORIGINAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM donation_record d
WHERE d.journal_txn_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM donation_journal_link l
    WHERE l.donation_id = d.donation_id
      AND l.journal_txn_id = d.journal_txn_id
      AND l.link_role = 'ORIGINAL'
  );

UPDATE grant_record g
SET journal_txn_id = (
  SELECT MIN(ti.txn_id)
  FROM transaction_info ti
  WHERE ti.k = 'domain_record_id'
    AND ti.v = g.grant_record_id
)
WHERE g.journal_txn_id IS NULL;

INSERT INTO operational_link_backfill_queue(module_name, domain_id, issue_code, issue_detail)
SELECT 'DONATION',
       d.donation_id,
       'NO_MATCHED_JOURNAL_TXN',
       'No deterministic journal transaction match found during phase-2 backfill.'
FROM donation_record d
WHERE d.journal_txn_id IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM operational_link_backfill_queue q
    WHERE q.module_name = 'DONATION'
      AND q.domain_id = d.donation_id
      AND q.issue_code = 'NO_MATCHED_JOURNAL_TXN'
  );

INSERT INTO operational_link_backfill_queue(module_name, domain_id, issue_code, issue_detail)
SELECT 'GRANT',
       g.grant_record_id,
       'NO_MATCHED_JOURNAL_TXN',
       'No transaction_info(domain_record_id) match found during phase-2 backfill.'
FROM grant_record g
WHERE g.journal_txn_id IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM operational_link_backfill_queue q
    WHERE q.module_name = 'GRANT'
      AND q.domain_id = g.grant_record_id
      AND q.issue_code = 'NO_MATCHED_JOURNAL_TXN'
  );
