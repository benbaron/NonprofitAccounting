package nonprofitbookkeeping.reports.generator.fieldmap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.generator.model.CellItem;
import nonprofitbookkeeping.reports.generator.model.FieldInfo;
import nonprofitbookkeeping.reports.generator.model.SheetModel;

/**
 * Generates a fieldmap CSV from a SheetModel. The CSV has the columns:
 * sheetName,cellRef,fieldName,javaType,excelFormat
 */
public final class FieldMappingGenerator
{
    private FieldMappingGenerator()
    {
        // utility class
    }

    public static List<String> generateMappings(SheetModel model)
    {
        List<String> rows = new ArrayList<>();
        rows.add("sheetName,cellRef,fieldName,javaType,excelFormat");

        Map<String, FieldInfo> fields = model.fields();

        for (CellItem ci : model.items())
        {
            if (!ci.isDynamic())
            {
                continue;
            }

            String fieldName = ci.fieldName();
            if (fieldName == null || fieldName.isBlank())
            {
                continue;
            }

            String cellRef = toCellRef(ci.row(), ci.col());
            FieldInfo info = (fields != null) ? fields.get(fieldName) : null;

            String javaType =
                    (info != null && info.javaType() != null && !info.javaType().isBlank())
                            ? info.javaType()
                            : "java.lang.String";

            String excelFormat =
                    (info != null && info.excelFormat() != null)
                            ? info.excelFormat()
                            : "";

            rows.add(String.join("," ,
                    escape(model.sheetName()),
                    escape(cellRef),
                    escape(fieldName),
                    escape(javaType),
                    escape(excelFormat)));
        }

        return rows;
    }

    public static Path writeCsv(SheetModel model, Path outDir, String baseName) throws IOException
    {
        List<String> lines = generateMappings(model);
        if (baseName == null || baseName.isBlank())
        {
            baseName = model.sheetName();
        }
        Files.createDirectories(outDir);
        Path csv = outDir.resolve(baseName + "_fieldmap.csv");
        Files.write(csv, lines, StandardCharsets.UTF_8);
        return csv;
    }

    /**
     * Convert 0-based row/column to Excel-style A1 reference.
     */
    private static String toCellRef(int rowIndex, int colIndex)
    {
        int colNum = colIndex + 1; // 1-based
        StringBuilder colRef = new StringBuilder();

        while (colNum > 0)
        {
            int rem = (colNum - 1) % 26;
            colRef.insert(0, (char) ('A' + rem));
            colNum = (colNum - 1) / 26;
        }

        int rowOneBased = rowIndex + 1;
        return colRef.toString() + rowOneBased;
    }

    /**
     * Basic CSV escaping: double quotes and surround the field if needed.
     */
    private static String escape(String value)
    {
        if (value == null)
        {
            return "";
        }
        boolean mustQuote =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        String result = value.replace("\"", "\"\"");
        if (mustQuote)
        {
            result = "\"" + result + "\"";
        }
        return result;
    }
}