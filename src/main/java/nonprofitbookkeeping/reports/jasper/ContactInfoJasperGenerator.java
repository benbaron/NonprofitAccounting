package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportBundles;
import nonprofitbookkeeping.reports.datasource.scareports.CONTACT_INFO_1Bean;
import nonprofitbookkeeping.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jasper generator for the SCA Contact Info 1 sheet.
 *
 * This generator expects the caller to supply the ContactInfo beans through the
 * {@link ReportContext} to keep the mapping flexible while still letting the
 * report be driven through the standard Jasper pipeline.
 */
public class ContactInfoJasperGenerator extends AbstractReportGenerator
{
        private final ReportContext reportContext;

        public ContactInfoJasperGenerator(ReportContext reportContext,
                ReportService reportService)
        {
                this.reportContext = reportContext;
        }

        @Override
        protected List<?> getReportData()
        {
                List<?> beans = reportContext != null ? reportContext.getBeans() : null;

                if (beans != null && !beans.isEmpty())
                {
                        return beans;
                }

                return Collections.singletonList(new CONTACT_INFO_1Bean());
        }

        @Override
        protected Map<String, Object> getReportParameters()
        {
                Map<String, Object> params = new HashMap<>();
                params.put("P_GENERATED_ON", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
                return params;
        }

        @Override
        protected String getReportPath() throws ActionCancelledException, NoFileCreatedException
        {
                return ReportBundles.bundleForGenerator(getClass()).jrxmlResource();
        }

        @Override
        public String getBaseName()
        {
                return "Contact_Info_1_" + LocalDate.now();
        }
}
