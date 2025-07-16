# Nonprofit Accounting

This project is a Java-based bookkeeping application for nonprofits. See
`PROJECT_OVERVIEW.md` for a broader description of the modules and
technologies used.

## JRXML Validation

JRXML report templates can be validated using `scripts/validate_jrxml.sh`.
The script requires `xmllint`, which is provided by the `libxml2-utils`
package on Debian/Ubuntu systems.

Install the package with:

```bash
sudo apt-get install libxml2-utils
```

Then run:

```bash
./scripts/validate_jrxml.sh
```

The script searches for all `*.jrxml` files under `src/main/resources` and
reports any XML issues.
