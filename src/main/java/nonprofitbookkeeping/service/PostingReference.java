package nonprofitbookkeeping.service;

/**
 * Reference returned by the transitional posting facade.
 *
 * <p>The legacy journal transaction id remains the primary compatibility value
 * while active services are migrated. When the facade has a canonical mirror in
 * {@code txn}, {@link #canonicalTxnId()} exposes the numeric canonical id from
 * a {@code txn:<id>} canonical reference.</p>
 */
public record PostingReference(int journalTxnId, String canonicalRef)
{
    public Long canonicalTxnId()
    {
        if (canonicalRef == null || !canonicalRef.startsWith("txn:"))
        {
            return null;
        }
        String value = canonicalRef.substring("txn:".length()).trim();
        if (value.isEmpty())
        {
            return null;
        }
        return Long.valueOf(value);
    }
}
