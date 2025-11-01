# Jasper Report Bundles

Each Jasper generator and its data bean now live alongside the corresponding
JRXML template under `src/main/resources/nonprofitbookkeeping/reports/bundles/`.
Every bundle directory contains:

* the JRXML template file that Jasper compiles,
* one or more `.properties` files documenting the display name, generator
  implementation, report type, and data bean,
* optional descriptive text stored in the `description` property to help future
  maintainers understand the schedule.

The `nonprofitbookkeeping.reports.ReportBundles` loader scans these directories
at runtime, and `ReportTemplates` builds the UI catalog from the collected
metadata. Generators call `bundledReportPath()` to retrieve the JRXML resource
resolved through the bundle metadata, keeping paths, beans, and documentation in
sync.
