package nonprofitbookkeeping.importer.sclx;

/**
 * Resolution mode to use when an SCLX import encounters an existing record.
 */
public enum DuplicateImportMode
{
    OVERWRITE,
    IGNORE,
    ALLOW
}
