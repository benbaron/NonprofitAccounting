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

## No-data behavior

Report generators now treat empty JDBC results as a valid outcome (they return
an empty list instead of throwing). Templates that need explicit output for
empty results should configure `whenNoDataType` or define a `noData` band in the
JRXML (for example, to render a "No results" message).

## Packaging bundles for distribution

Use `nonprofitbookkeeping.reports.ReportBundlePackager` to export the discovered
bundles (metadata, JRXML templates, and their associated bean sources) into ZIP
archives. This is handy when distributing the reports to external consumers who
need the compiled template and the bean contract in a single artifact. The
packager automatically embeds each bundle's `beanName` alongside the source so
downstream tooling can reference the correct data bean key.
