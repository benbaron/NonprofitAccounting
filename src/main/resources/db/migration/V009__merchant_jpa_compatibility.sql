-- Compatibility fixes discovered after switching Hibernate to validate.
--
-- Merchant maps notes as a LOB field, but the transitional V001 merchant table
-- did not include that column.

ALTER TABLE merchant
ADD COLUMN IF NOT EXISTS notes CLOB;
