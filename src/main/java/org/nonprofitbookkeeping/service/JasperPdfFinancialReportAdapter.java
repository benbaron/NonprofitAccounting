package org.nonprofitbookkeeping.service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PDF adapter using JasperReports directly.
 */
public class JasperPdfFinancialReportAdapter implements FinancialReportExportAdapter
{
    @Override
    public FinancialReportExportFormat format()
    {
        return FinancialReportExportFormat.PDF;
    }

    @Override
    public byte[] render(String reportName, String textPreview, String csvBody)
    {
        try
        {
            System.setProperty("java.awt.headless", "true");
            JasperDesign design = design();
            var report = JasperCompileManager.compileReport(design);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("REPORT_TITLE", reportName == null ? "Report" : reportName);
            params.put("REPORT_BODY", textPreview == null ? "" : textPreview);

            JRDataSource ds = new JREmptyDataSource(1);
            JasperPrint print = JasperFillManager.fillReport(report, params, ds);
            return JasperExportManager.exportReportToPdf(print);
        }
        catch (JRException ex)
        {
            throw new IllegalStateException("Could not render PDF via JasperReports.", ex);
        }
    }

    private static JasperDesign design() throws JRException
    {
        JasperDesign design = new JasperDesign();
        design.setName("financial_report");
        design.setPageWidth(595);
        design.setPageHeight(842);
        design.setLeftMargin(40);
        design.setRightMargin(40);
        design.setTopMargin(30);
        design.setBottomMargin(30);
        design.setColumnWidth(515);

        JRDesignParameter titleParam = new JRDesignParameter();
        titleParam.setName("REPORT_TITLE");
        titleParam.setValueClass(String.class);
        design.addParameter(titleParam);

        JRDesignParameter bodyParam = new JRDesignParameter();
        bodyParam.setName("REPORT_BODY");
        bodyParam.setValueClass(String.class);
        design.addParameter(bodyParam);

        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(760);

        JRDesignTextField titleField = new JRDesignTextField();
        titleField.setX(0);
        titleField.setY(0);
        titleField.setWidth(515);
        titleField.setHeight(20);
        titleField.setFontSize(14f);
        titleField.setBold(true);
        titleField.setExpression(new JRDesignExpression("$P{REPORT_TITLE}"));
        detailBand.addElement(titleField);

        JRDesignTextField bodyField = new JRDesignTextField();
        bodyField.setX(0);
        bodyField.setY(24);
        bodyField.setWidth(515);
        bodyField.setHeight(716);
        bodyField.setFontSize(10f);
        bodyField.setExpression(new JRDesignExpression("$P{REPORT_BODY}"));
        detailBand.addElement(bodyField);

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);
        return design;
    }
}
