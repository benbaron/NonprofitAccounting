package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Jasper generator that renders a bundled JRXML template discovered by
 * {@link ReportBundles}.
 */
public class BundledTemplateJasperGenerator extends AbstractReportGenerator
{
    private final ReportBundles.Bundle bundle;
    private final ReportContext context;

    public BundledTemplateJasperGenerator(ReportBundles.Bundle bundle,
        ReportContext context)
    {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
        this.context = context;
    }

    @Override
    protected java.util.List<?> getReportData()
    {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        return Collections.emptyMap();
    }

    @Override
    protected String getReportPath()
        throws ActionCancelledException, NoFileCreatedException
    {
        return this.bundle.jrxmlResource();
    }

    @Override
    public String getBaseName()
    {
        String name = this.bundle.displayName();
        return (name == null || name.isBlank())
            ? simpleNameFromId(this.bundle.id())
            : name.replace(' ', '_');
    }

    private static String simpleNameFromId(String id)
    {
        if (id == null || id.isBlank())
        {
            return "report";
        }

        int slash = id.lastIndexOf('/');
        String name = slash >= 0 ? id.substring(slash + 1) : id;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }
}
