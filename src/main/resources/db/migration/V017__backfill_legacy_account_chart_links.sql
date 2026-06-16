-- Move safe legacy account-to-chart linking out of startup compatibility backfills.

UPDATE account
SET chart_id = (SELECT MIN(id) FROM chart_of_accounts)
WHERE chart_id IS NULL;
