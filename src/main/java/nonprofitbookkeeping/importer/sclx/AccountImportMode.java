package nonprofitbookkeeping.importer.sclx;

/**
 * Controls how SCLX account identifiers are resolved during import.
 */
public enum AccountImportMode
{
    /**
     * Import account identifiers exactly as they appear in SCLX.
     */
    AS_IS,

    /**
     * Resolve SCLX account identifiers through an explicit account mapping table.
     */
    MAPPED
}
