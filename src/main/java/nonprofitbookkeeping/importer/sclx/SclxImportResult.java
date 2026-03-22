package nonprofitbookkeeping.importer.sclx;

/**
 * Summary counts for one completed SCLX import pass.
 */
public record SclxImportResult(
    String version,
    int accountCount,
    int fundCount,
    int budgetCount,
    int personCount,
    int eventCount,
    int documentCount,
    int transactionCount,
    int transactionLineCount,
    int outstandingItemCount,
    int otherAssetItemCount,
    int supplementalItemCount,
    int assetCount,
    int supplyCount,
    int bankingItemCount,
    int bankStatementImportCount)
{
}
