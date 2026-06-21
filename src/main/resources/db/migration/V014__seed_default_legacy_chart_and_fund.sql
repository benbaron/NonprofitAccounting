-- Seed default compatibility chart/fund data in Flyway so startup
-- backfills no longer create canonical seed rows at runtime.

INSERT INTO chart_of_accounts(name, version, status, created_at, updated_at)
SELECT 'Default Legacy Chart', 'legacy', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM chart_of_accounts);

INSERT INTO fund(id, code, name, fund_type, is_active, created_at, updated_at)
SELECT 1, 'GENERAL', 'General Fund', 'UNRESTRICTED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM fund WHERE id = 1);
