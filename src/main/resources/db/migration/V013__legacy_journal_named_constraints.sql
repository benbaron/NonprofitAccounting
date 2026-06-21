-- Name legacy journal supplemental-line constraints so Flyway owns the same
-- compatibility artifacts previously added by Database.ensureSchema().

ALTER TABLE txn_supplemental_line
  ADD CONSTRAINT IF NOT EXISTS fk_txn_supplemental_entry
  FOREIGN KEY (entry_id) REFERENCES journal_entry(id) ON DELETE SET NULL;

ALTER TABLE txn_supplemental_line
  ADD CONSTRAINT IF NOT EXISTS fk_txn_supplemental_person
  FOREIGN KEY (counterparty_person_id) REFERENCES person(id) ON DELETE SET NULL;
