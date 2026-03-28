package nonprofitbookkeeping.importer.sclx;

/**
 * Controls how incoming SCLX account identifiers are resolved to
 * NonprofitBookkeeping accounts.
 */
public enum AccountImportMode
{
    /**
     * Use SCLX account references directly.
     */
    AS_IS,

    /**
     * Resolve SCLX account references through an explicit mapping table.
     */
    MAPPED
}
