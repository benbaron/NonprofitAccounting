package nonprofitbookkeeping.importer.sclx;

import java.util.Map;

/**
 * Options controlling SCLX import behavior.
 */
public record SclxImportOptions(
    boolean failOnUnknownVersion,
    boolean requireStrictFormat,
    boolean trimEmptyCollections,
    boolean allowSingleSidedTransactions,
    String cashAccountReference,
    String importRunId,
    AccountImportMode accountImportMode,
    Map<String, String> accountMapping)
{
    public SclxImportOptions
    {
        accountImportMode = accountImportMode == null ? AccountImportMode.AS_IS : accountImportMode;
        accountMapping = accountMapping == null ? Map.of() : Map.copyOf(accountMapping);

        if (accountImportMode == AccountImportMode.MAPPED && accountMapping.isEmpty())
        {
            throw new IllegalArgumentException("accountMapping is required when accountImportMode=MAPPED.");
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
            null,
            AccountImportMode.AS_IS,
            Map.of());
    }

    public String resolveAccountReference(String sclxReference)
    {
        if (sclxReference == null || sclxReference.isBlank())
        {
            return sclxReference;
        }
        if (accountImportMode == AccountImportMode.MAPPED)
        {
            return accountMapping.getOrDefault(sclxReference, sclxReference);
        }
        return sclxReference;
    }

    public boolean hasCashAccountReference()
    {
        return cashAccountReference != null && !cashAccountReference.isBlank();
    }

    public String effectiveImportRunId()
    {
        if (importRunId == null || importRunId.isBlank())
        {
            return "run-" + java.time.Instant.now().toString();
        }
        return importRunId;
    }
}
