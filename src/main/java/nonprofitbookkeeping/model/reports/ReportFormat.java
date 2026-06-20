package nonprofitbookkeeping.model.reports;

/** Output formats exposed by the alternate reports workspace. */
public enum ReportFormat
{
    TEXT("Text", "txt", true, "Plain text report output."),
    CSV("CSV", "csv", true, "Comma-separated report output."),
    PDF("PDF", "pdf", false, "PDF export is not implemented for semantic reports yet."),
    XLSX("Excel", "xlsx", false, "Excel export is not implemented for semantic reports yet.");

    private final String label;
    private final String extension;
    private final boolean supported;
    private final String explanation;

    ReportFormat(String label, String extension, boolean supported, String explanation)
    {
        this.label = label;
        this.extension = extension;
        this.supported = supported;
        this.explanation = explanation;
    }

    public String label() { return label; }
    public String extension() { return extension; }
    public boolean supported() { return supported; }
    public String explanation() { return explanation; }

    @Override public String toString() { return label; }
}
