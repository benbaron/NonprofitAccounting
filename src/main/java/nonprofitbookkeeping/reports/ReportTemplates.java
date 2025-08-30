package nonprofitbookkeeping.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nonprofitbookkeeping.reports.generator.AbstractReportGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.TrialBalanceJasperGenerator;

/**
 * Registry of available Jasper report templates.
 * Each entry maps a user facing display name to the JRXML template
 * and the generator class responsible for binding data.
 */
public final class ReportTemplates {

    /** Immutable holder for template metadata. */
    public record TemplateInfo(String displayName,
                               String jrxmlPath,
                               Class<? extends AbstractReportGenerator> binderClass) {
        /**
         * Derives the report type key used by {@link nonprofitbookkeeping.service.ReportService}
         * from the binder class name. For example, a binder class named
         * {@code TrialBalanceJasperGenerator} becomes {@code "trial_balance_jasper"}.
         *
         * @return report type identifier
         */
        public String reportTypeKey() {
            String base = binderClass.getSimpleName().replaceFirst("JasperGenerator$", "");
            String snake = base.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            return snake + "_jasper";
        }
    }

    private static final Map<String, TemplateInfo> TEMPLATES = createTemplates();

    private ReportTemplates() {}

    private static Map<String, TemplateInfo> createTemplates() {
        Map<String, TemplateInfo> map = new LinkedHashMap<>();
        map.put("Income Statement",
                new TemplateInfo("Income Statement",
                        "jrxml/IncomeStatementAlt.jrxml",
                        IncomeStatementJasperGenerator.class));
        map.put("Trial Balance",
                new TemplateInfo("Trial Balance",
                        "jrxml/TrialBalance.jrxml",
                        TrialBalanceJasperGenerator.class));
        return Map.copyOf(map);
    }

    /**
     * Provides the map of available templates keyed by their display names.
     *
     * @return immutable mapping of display names to template info
     */
    public static Map<String, TemplateInfo> templates() {
        return TEMPLATES;
    }
}

