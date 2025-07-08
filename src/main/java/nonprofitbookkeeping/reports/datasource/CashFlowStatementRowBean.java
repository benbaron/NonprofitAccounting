package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Represents a single row in a Cash Flow Statement report.
 * This bean is used as a data source for report generation, typically with JasperReports or similar libraries.
 * It includes fields for the section name, item description, amount, a flag for subtotals, and sort order.
 */
public class CashFlowStatementRowBean {
    /** The name of the section this row belongs to (e.g., "Operating Activities", "Investing Activities", "Financing Activities"). */
    private String sectionName;
    /** The description of the cash flow item (e.g., "Net Income", "Depreciation", "Purchase of Equipment"). */
    private String itemDescription;
    /** The monetary amount associated with this item. Positive for cash inflows, negative for cash outflows. */
    private BigDecimal itemAmount;
    /** A boolean flag indicating if this row represents a subtotal (true) or a regular item (false). */
    private boolean isSubtotal;
    /** An integer value used to sort items correctly within their sections and across the report. */
    private int sortOrder;

    /**
     * Constructs a {@code CashFlowStatementRowBean} for a regular cash flow item.
     * The {@code isSubtotal} flag will be set to false.
     *
     * @param sectionName The name of the section this item belongs to.
     * @param itemDescription The description of the cash flow item.
     * @param itemAmount The monetary amount of the item.
     * @param sortOrder The sort order for this item.
     */
    public CashFlowStatementRowBean(String sectionName, String itemDescription, BigDecimal itemAmount, int sortOrder) {
        this(sectionName, itemDescription, itemAmount, false, sortOrder);
    }

    /**
     * Constructs a {@code CashFlowStatementRowBean} allowing explicit setting of the {@code isSubtotal} flag.
     * This constructor can be used for both regular items and subtotal lines.
     *
     * @param sectionName The name of the section this row belongs to.
     * @param itemDescription The description of the item or subtotal line (e.g., "Net Cash from Operating Activities").
     * @param itemAmount The monetary amount. For subtotals, this is the calculated subtotal amount.
     * @param isSubtotal True if this row represents a subtotal, false otherwise.
     * @param sortOrder The sort order for this row.
     */
    public CashFlowStatementRowBean(String sectionName, String itemDescription, BigDecimal itemAmount, boolean isSubtotal, int sortOrder) {
        this.sectionName = sectionName;
        this.itemDescription = itemDescription;
        this.itemAmount = itemAmount;
        this.isSubtotal = isSubtotal;
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the name of the section this row belongs to.
     * @return The section name.
     */
    public String getSectionName() {
        return this.sectionName;
    }

    /**
     * Gets the description of the cash flow item.
     * @return The item description.
     */
    public String getItemDescription() {
        return this.itemDescription;
    }

    /**
     * Gets the monetary amount of this item.
     * @return The item amount as a {@link BigDecimal}.
     */
    public BigDecimal getItemAmount() {
        return this.itemAmount;
    }

    // ------------------------------------------------------------------
    // Convenience getters matching field names in cash_flow_statement.jrxml
    // ------------------------------------------------------------------

    /**
     * Alias for {@link #getSectionName()} to match JRXML field name
     * {@code section_name}.
     *
     * @return the section name
     */
    public String getSection_name() {
        return getSectionName();
    }

    /**
     * Alias for {@link #getItemDescription()} to match JRXML field name
     * {@code item_description}.
     *
     * @return the item description
     */
    public String getItem_description() {
        return getItemDescription();
    }

    /**
     * Alias for {@link #getItemAmount()} to match JRXML field name
     * {@code item_amount}.
     *
     * @return the item amount
     */
    public BigDecimal getItem_amount() {
        return getItemAmount();
    }

    /**
     * Checks if this row represents a subtotal.
     * This getter follows a less common naming convention (getIsType).
     * {@link #isSubtotal()} is the standard JavaBean convention.
     * @return True if this row is a subtotal, false otherwise.
     */
    public boolean getIsSubtotal() {
        return this.isSubtotal;
    }

    /**
     * Checks if this row represents a subtotal.
     * This is the standard JavaBean naming convention for boolean getters.
     * @return True if this row is a subtotal, false otherwise.
     */
    public boolean isSubtotal() {
        return this.isSubtotal;
    }

    /**
     * Gets the sort order value for this row.
     * @return The sort order.
     */
    public int getSortOrder() {
        return this.sortOrder;
    }

    /**
     * Sets the name of the section this row belongs to.
     * @param sectionName The section name to set.
     */
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    /**
     * Sets the description of the cash flow item.
     * @param itemDescription The item description to set.
     */
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    /**
     * Sets the monetary amount of this item.
     * @param itemAmount The item amount to set.
     */
    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount;
    }

    /**
     * Sets whether this row represents a subtotal.
     * @param isSubtotal True to mark this row as a subtotal, false otherwise.
     */
    public void setIsSubtotal(boolean isSubtotal) {
        this.isSubtotal = isSubtotal;
    }

    /**
     * Sets the sort order value for this row.
     * @param sortOrder The sort order to set.
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
