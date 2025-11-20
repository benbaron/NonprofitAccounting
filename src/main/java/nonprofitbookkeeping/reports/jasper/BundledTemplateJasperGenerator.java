package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.ReportBundles;
import nonprofitbookkeeping.reports.ReportContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generic Jasper generator for bundles that only need a template path and optional
 * externally supplied beans. This helps keep legacy bundle metadata working even
 * when the concrete generator classes have been relocated or removed.
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
        protected List<?> getReportData()
        {
                if (this.context != null && this.context.getBeans() != null)
                {
                        return this.context.getBeans();
                }

                return Collections.emptyList();
        }

        @Override
        protected Map<String, Object> getReportParameters()
        {
                Map<String, Object> params = new HashMap<>();
                params.put("P_REPORT_TITLE", this.bundle.displayName());

                if (this.context != null)
                {
                        if (this.context.getStartDate() != null)
                        {
                                params.put("P_REPORT_START",
                                        this.context.getStartDate()
                                                .format(DateTimeFormatter.ISO_DATE));
                        }

                        if (this.context.getEndDate() != null)
                        {
                                params.put("P_REPORT_END",
                                        this.context.getEndDate()
                                                .format(DateTimeFormatter.ISO_DATE));
                        }
                }

                return params;
        }

        @Override
        protected String getReportPath()
        {
                return this.bundle.jrxmlResource();
        }

        @Override
        public String getBaseName()
        {
                String sanitized = this.bundle.displayName().replaceAll("[^A-Za-z0-9]+", "_");
                return sanitized + "_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }
}
