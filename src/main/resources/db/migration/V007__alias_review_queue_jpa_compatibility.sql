-- Compatibility fixes discovered after switching Hibernate to validate.
--
-- AliasReviewQueue maps resolver and resolution_note, but the transitional
-- V001 alias_review_queue table did not include those columns.

ALTER TABLE alias_review_queue
ADD COLUMN IF NOT EXISTS resolver VARCHAR(80);

ALTER TABLE alias_review_queue
ADD COLUMN IF NOT EXISTS resolution_note VARCHAR(1000);
