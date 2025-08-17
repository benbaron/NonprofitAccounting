package nonprofitbookkeeping.reports.datasource.scareports;

/**
 * Base class for rows in Statement of Cash Activities reports. Provides an
 * optional {@code transactionId} field allowing report rows to be linked back
 * to their originating {@code AccountingTransaction}.
 */
public abstract class ScaRowBase {

    /** Identifier of the originating transaction, if applicable. */
    private String transactionId;

    /** Default constructor. */
    protected ScaRowBase() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}

