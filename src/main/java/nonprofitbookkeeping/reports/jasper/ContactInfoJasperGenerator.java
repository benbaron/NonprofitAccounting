package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.ReportBundles;
import nonprofitbookkeeping.reports.ReportContext;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight generator for the SCA Contact Info report. The data beans for
 * this report are provided externally (for example, by the spreadsheet import
 * workflow) and injected through {@link ReportContext#setBeans(List)}.
 */
public class ContactInfoJasperGenerator extends AbstractReportGenerator
{
        private final ReportContext context;

        public ContactInfoJasperGenerator(ReportContext context)
        {
                this.context = context;

                if (context != null && context.getBeans() != null)
                {
                        setReportData(context.getBeans());
                }
        }

        @Override
        protected List<?> getReportData()
        {
                if (this.context == null || this.context.getBeans() == null)
                {
                        return Collections.emptyList();
                }

                return this.context.getBeans();
        }

        @Override
        protected Map<String, Object> getReportParameters()
        {
                Map<String, Object> params = new HashMap<>();
                params.put("P_GENERATION_DATE", LocalDate.now());
                return params;
        }

        @Override
        protected String getReportPath() throws ActionCancelledException, NoFileCreatedException
        {
                return bundledReportPath();
        }

        @Override
        public String getBaseName()
        {
                String display = ReportBundles.bundleForGenerator(getClass()).displayName();
                return display.replaceAll("\\s+", "_") + "_" + LocalDate.now();
        }
}
