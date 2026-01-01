package nonprofitbookkeeping.reports.jasper.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory representation of a single sheet's field mapping CSV.
 *
 * Typically created via {@link FieldMapLoader}.
 */
public final class FieldMap {

    private final String sheetName;
    private final List<FieldMapEntry> entries;
    private final Map<String, FieldMapEntry> byFieldName;

    FieldMap(String sheetName, List<FieldMapEntry> entries) {
        this.sheetName = sheetName;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));

        Map<String, FieldMapEntry> tmp = new LinkedHashMap<>();
        for (FieldMapEntry e : entries) {
            tmp.put(e.getFieldName(), e);
        }
        this.byFieldName = Collections.unmodifiableMap(tmp);
    }

    public String getSheetName() {
        return this.sheetName;
    }

    /**
     * All entries, in CSV order.
     */
    public List<FieldMapEntry> getEntries() {
        return this.entries;
    }

    /**
     * Lookup by JRXML/bean field name.
     */
    public FieldMapEntry getByFieldName(String fieldName) {
        return this.byFieldName.get(fieldName);
    }

    /**
     * Map of fieldName -> FieldMapEntry.
     */
    public Map<String, FieldMapEntry> getEntriesByFieldName() {
        return this.byFieldName;
    }

    /**
     * Build a SELECT list using only the dbExpr column, e.g.:
     *
     *   a.name as accountname,
     *   s.opening_balance as openingbalance,
     *   s.closing_balance as closingbalance
     *
     * Only entries with non-null/non-blank dbExpr are included.
     * If no entries have dbExpr, the result is the empty string.
     */
    public String buildSelectListFromDbExprs() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (FieldMapEntry e : this.entries) {
            String expr = e.getDbExpr();
            if (expr == null || expr.trim().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(",\n");
            }
            sb.append(expr).append(" as ").append(e.getFieldName());
            first = false;
        }

        return sb.toString();
    }

    /**
     * Build a SELECT list using dbExpr when present; otherwise use the supplied
     * fallback expression (e.g., "null") for every field.
     */
    public String buildSelectListWithFallback(String fallbackExpr) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (FieldMapEntry e : this.entries) {
            String expr = e.getDbExpr();
            if (expr == null || expr.trim().isEmpty()) {
                expr = fallbackExpr;
            }
            if (!first) {
                sb.append(",\n");
            }
            sb.append(expr).append(" as ").append(e.getFieldName());
            first = false;
        }

        return sb.toString();
    }
}
