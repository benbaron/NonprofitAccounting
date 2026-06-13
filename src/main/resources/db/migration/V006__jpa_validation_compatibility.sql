-- Compatibility fixes discovered after switching Hibernate to validate.
--
-- Legacy tests and older import paths insert account_subtype_schedule_default
-- rows with only subtype and schedule_kind_id. The legacy meaning is that the
-- schedule is required, so keep that insert form valid by supplying a default.

ALTER TABLE account_subtype_schedule_default
ALTER COLUMN is_required SET DEFAULT TRUE;

UPDATE account_subtype_schedule_default
SET is_required = TRUE
WHERE is_required IS NULL;

ALTER TABLE account_subtype_schedule_default
ALTER COLUMN is_required SET NOT NULL;
