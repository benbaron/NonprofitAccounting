package nonprofitbookkeeping.importer.sclx;

/**
 * Runtime exception used for parse, compatibility, and staging failures.
 */
public class SclxImportException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public SclxImportException(String message)
    {
        super(message);
    }

    public SclxImportException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
