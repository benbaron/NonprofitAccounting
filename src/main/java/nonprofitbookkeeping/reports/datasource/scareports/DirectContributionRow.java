package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a direct contribution from an individual or group.
 */
public class DirectContributionRow {
    private String from;
    private String description;
    private BigDecimal amount;

    public DirectContributionRow() {
    }

    public DirectContributionRow(String from, String description, BigDecimal amount) {
        this.from = from;
        this.description = description;
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
