package nonprofitbookkeeping.importer.sclx;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Options controlling SCLX import behavior.
 *
 * @param failOnUnknownVersion whether to reject unsupported SCLX versions
 * @param requireStrictFormat whether format must be exactly {@code SCLX}
 * @param trimEmptyCollections whether empty top-level collections should be treated as absent
 * @param allowSingleSidedTransactions whether worksheet-native single-line transactions are allowed
 * @param cashAccountReference target NonprofitBookkeeping account reference used to balance imported
 *        single-sided or net-unbalanced transactions. This may be an account number, code, id, or
 *        any other lookup token understood by the concrete import target.
 * @param accountImportMode account resolution mode: import SCLX account ids as-is, or resolve them
 *        through {@code accountMapping}
 * @param accountMapping mapping from SCLX account ids/numbers to NonprofitBookkeeping account
 *        references. Used only when {@code accountImportMode == MAPPED}.
 */
public record SclxImportOptions(
    boolean failOnUnknownVersion,
    boolean requireStrictFormat,
    boolean trimEmptyCollections,
    boolean allowSingleSidedTransactions,
    String cashAccountReference,
    AccountImportMode accountImportMode,
    Map<String, String> accountMapping)
{
    public SclxImportOptions
    {
        accountImportMode = Objects.requireNonNullElse(accountImportMode, AccountImportMode.AS_IS);
        accountMapping = accountMapping == null ? Collections.emptyMap() : Map.copyOf(accountMapping);

        if (accountImportMode == AccountImportMode.MAPPED && accountMapping.isEmpty())
        {
            throw new IllegalArgumentException(
                "accountMapping must be provided when accountImportMode is MAPPED");
        }
    }

    public static SclxImportOptions defaults()
    {
        return new SclxImportOptions(
            true,
            true,
            true,
            true,
            null,
            AccountImportMode.AS_IS,
            Collections.emptyMap());
    }

    /**
     * Resolves one SCLX account reference into the account reference that the concrete
     * NonprofitBookkeeping target should use.
     */
    public String resolveAccountReference(String sclxAccountReference)
    {
        if (sclxAccountReference == null || sclxAccountReference.isBlank())
        {
            return sclxAccountReference;
        }

        if (accountImportMode == AccountImportMode.AS_IS)
        {
            return sclxAccountReference;
        }

        return accountMapping.getOrDefault(sclxAccountReference, sclxAccountReference);
    }

    /**
     * Returns true when the importer has a configured cash account reference for balancing logic.
     */
    public boolean hasCashAccountReference()
    {
        return cashAccountReference != null && !cashAccountReference.isBlank();
    }
}
