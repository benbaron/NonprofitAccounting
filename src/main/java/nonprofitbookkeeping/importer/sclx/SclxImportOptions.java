package nonprofitbookkeeping.importer.sclx;

import java.util.Map;

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
        accountImportMode = accountImportMode == null ? AccountImportMode.AS_IS : accountImportMode;
        accountMapping = accountMapping == null ? Map.of() : Map.copyOf(accountMapping);

        if (accountImportMode == AccountImportMode.MAPPED && accountMapping.isEmpty())
        {
            throw new IllegalArgumentException("accountMapping is required when accountImportMode=MAPPED");
        }
    }

    public static SclxImportOptions defaults()
    {
        return new SclxImportOptions(true, true, true, true, null, AccountImportMode.AS_IS, Map.of());
    }

    public boolean hasCashAccountReference()
    {
        return cashAccountReference != null && !cashAccountReference.isBlank();
    }

    public String resolveAccountReference(String sclxAccountReference)
    {
        if (sclxAccountReference == null || sclxAccountReference.isBlank())
        {
            return sclxAccountReference;
        }
        if (accountImportMode == AccountImportMode.MAPPED)
        {
            return accountMapping.getOrDefault(sclxAccountReference, sclxAccountReference);
        }
        return sclxAccountReference;
    }
}
