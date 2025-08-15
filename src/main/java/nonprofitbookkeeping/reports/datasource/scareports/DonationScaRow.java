package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a donation received from an SCA group.
 */
public class DonationScaRow {
    private String donorGroup;
    private String description;
    private String notes;
    private BigDecimal amount;

    public DonationScaRow() {
    }

    public DonationScaRow(String donorGroup, String description, String notes, BigDecimal amount) {
        this.donorGroup = donorGroup;
        this.description = description;
        this.notes = notes;
        this.amount = amount;
    }

    public String getDonorGroup() {
        return donorGroup;
    }

    public void setDonorGroup(String donorGroup) {
        this.donorGroup = donorGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
