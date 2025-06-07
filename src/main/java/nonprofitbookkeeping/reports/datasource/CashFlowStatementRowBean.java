package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

public class CashFlowStatementRowBean {
    private String sectionName; // e.g., "Operating Activities", "Investing Activities", "Financing Activities"
    private String itemDescription;
    private BigDecimal itemAmount;
    private boolean isSubtotal; // To differentiate items from subtotals within a section
    private int sortOrder; // To help sort items correctly within and across sections

    // Constructor for regular items
    public CashFlowStatementRowBean(String sectionName, String itemDescription, BigDecimal itemAmount, int sortOrder) {
        this(sectionName, itemDescription, itemAmount, false, sortOrder);
    }

    // Constructor for subtotals or special lines
    public CashFlowStatementRowBean(String sectionName, String itemDescription, BigDecimal itemAmount, boolean isSubtotal, int sortOrder) {
        this.sectionName = sectionName;
        this.itemDescription = itemDescription;
        this.itemAmount = itemAmount;
        this.isSubtotal = isSubtotal;
        this.sortOrder = sortOrder;
    }

    // Getters
    public String getSectionName() {
        return sectionName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public BigDecimal getItemAmount() {
        return itemAmount;
    }

    public boolean getIsSubtotal() { // Jasper field access might prefer isIsSubtotal or isSubtotal directly
        return isSubtotal;
    }

    public boolean isSubtotal() { // Standard JavaBean convention for boolean
        return isSubtotal;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    // Setters (optional, but good practice)
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount;
    }

    public void setIsSubtotal(boolean isSubtotal) {
        this.isSubtotal = isSubtotal;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
