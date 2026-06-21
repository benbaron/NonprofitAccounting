-- Compatibility fixes discovered after switching Hibernate to validate.
--
-- Counterparty maps notes as a LOB field, but the transitional V001
-- counterparty table did not include that column.

ALTER TABLE counterparty
ADD COLUMN IF NOT EXISTS notes CLOB;
