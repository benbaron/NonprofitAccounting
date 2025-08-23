# Project TODOs

This list reflects the outstanding work discussed with the maintainer.

1. **Resolve "Error: No account" after COA import**
   - Imported chart of accounts does not link to transactions, causing "Error: No account" in the Journal pane.
   - Investigate import order and ensure accounts are persisted and applied before transactions load.

2. **Chart‑of‑accounts conflict handling**
   - When importing a new chart into a company that already has accounts, prompt the user to **overwrite**, **merge**, or **cancel**.

3. **Row bean data model**
   - Implement persistent `RowBean` entities that store all bean fields.
   - Each row bean links one‑to‑one with a transaction but can be removed or replaced.
   - Provide queries to list beans by type or field values for reporting.

4. **Row bean UI integration**
   - On transaction entry, present checkboxes for available bean types.
   - Selecting a bean adds its data entry pane to the transaction form.
   - Editing a transaction should display and allow modification of its attached beans.

5. **Row bean reporting**
   - Create report panes capable of tabulating all beans of a selected type (e.g., all "transfers out").

