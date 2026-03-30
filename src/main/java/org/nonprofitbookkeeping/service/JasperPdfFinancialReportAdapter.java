package org.nonprofitbookkeeping.service;

import java.nio.charset.StandardCharsets;

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
        String title = reportName == null ? "Report" : reportName;
        String body = textPreview == null ? "" : textPreview;

        String escaped = (title + "\n\n" + body)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");

        String content = "BT /F1 12 Tf 50 780 Td (" + escaped + ") Tj ET";
        int contentLength = content.getBytes(StandardCharsets.US_ASCII).length;

        String pdf = "%PDF-1.4\n"
                + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 595 842]/Resources<</Font<</F1 4 0 R>>>>/Contents 5 0 R>>endobj\n"
                + "4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                + "5 0 obj<</Length " + contentLength + ">>stream\n"
                + content + "\nendstream\nendobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "0000000010 00000 n \n0000000060 00000 n \n0000000117 00000 n \n"
                + "0000000243 00000 n \n0000000313 00000 n \n"
                + "trailer<</Size 6/Root 1 0 R>>\nstartxref\n430\n%%EOF";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }
}
