-- Move safe party/counterparty compatibility repairs out of startup backfills.

UPDATE donor
SET external_id = name
WHERE external_id IS NULL
  AND name IS NOT NULL;

UPDATE person
SET type = 'DONOR'
WHERE type IS NULL
   OR TRIM(type) = '';

INSERT INTO counterparty(display_name, kind, email, phone, is_active)
SELECT p.name,
       UPPER(COALESCE(NULLIF(TRIM(p.type), ''), 'DONOR')),
       p.email,
       p.phone,
       TRUE
FROM person p
WHERE p.name IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM counterparty c
    WHERE c.display_name = p.name
      AND c.kind = UPPER(COALESCE(NULLIF(TRIM(p.type), ''), 'DONOR'))
  );

INSERT INTO counterparty(display_name, kind, email, phone, is_active)
SELECT d.name,
       'DONOR',
       d.email,
       d.phone,
       TRUE
FROM donor d
WHERE d.name IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM counterparty c
    WHERE c.display_name = d.name
      AND c.kind = 'DONOR'
  );
