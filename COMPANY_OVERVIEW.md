# Company Overview

This repository contains the source code for a desktop bookkeeping application aimed at nonprofit organizations. The project is built with JavaFX and relies on a variety of open-source libraries for data storage, reporting, and testing.

## Recent Progress

- Implemented `CurrentCompany.CompanyListener.getListeners()` to expose registered listeners for testing purposes.
- Implemented `CurrentCompany.forceCompanyLoad()` to directly set the active company and trigger change notifications.

These helpers were added to support UI tests that need to simulate opening or closing companies without going through the full UI workflow.

## Outstanding Items

- Maven-based tests fail during plugin resolution in this environment. Dependencies may need to be preinstalled or the build adjusted.
- Dashboard panel reset functionality should be verified once tests can run successfully.

