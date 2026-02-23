package nonprofitbookkeeping.reports.excel;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMap;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapEntry;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContextHolder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Writes report values into a pre-existing XLSX template using field map metadata.
 */
public class ExcelTemplateWriter
{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(ExcelTemplateWriter.class);

	/**
	 * Write template.
	 *
	 * @param templateFile the template file
	 * @param fieldMap the field map
	 * @param bean the bean
	 * @param outputFile the output file
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public File writeTemplate(File templateFile, FieldMap fieldMap, Object bean, File outputFile)
		throws IOException
	{
		if (templateFile == null || !templateFile.exists())
		{
			throw new IOException("Template file does not exist: " + templateFile);
		}
		if (fieldMap == null)
		{
			throw new IOException("Field map is required to write template output.");
		}

		try (FileInputStream in = new FileInputStream(templateFile);
			Workbook workbook = WorkbookFactory.create(in))
		{
			writeFields(workbook, fieldMap, bean, null, true);
			try (FileOutputStream out = new FileOutputStream(outputFile))
			{
				workbook.write(out);
			}
		}

		return outputFile;
	}

	/**
	 * Write template.
	 *
	 * @param workbook the workbook
	 * @param fieldMap the field map
	 * @param bean the bean
	 * @param sheetName the sheet name
	 * @param createMissingSheets the create missing sheets
	 */
	public void writeTemplate(Workbook workbook, FieldMap fieldMap, Object bean,
		String sheetName, boolean createMissingSheets)
	{
		if (workbook == null)
		{
			throw new IllegalArgumentException("Workbook is required.");
		}
		if (fieldMap == null)
		{
			throw new IllegalArgumentException("Field map is required.");
		}

		writeFields(workbook, fieldMap, bean, sheetName, createMissingSheets);
	}

	/**
	 * Write fields.
	 *
	 * @param workbook the workbook
	 * @param fieldMap the field map
	 * @param bean the bean
	 * @param sheetNameFilter the sheet name filter
	 * @param createMissingSheets the create missing sheets
	 */
	private void writeFields(Workbook workbook, FieldMap fieldMap, Object bean,
		String sheetNameFilter, boolean createMissingSheets)
	{
		if (bean == null)
		{
			return;
		}

		DataFormat dataFormat = workbook.createDataFormat();
		Map<String, CellStyle> styleCache = new HashMap<>();

		for (FieldMapEntry entry : fieldMap.getEntries())
		{
			String sheetName = entry.getSheetName();
			String cellRef = entry.getCellRef();
			String fieldName = entry.getFieldName();

			if (sheetName == null || sheetName.isBlank()
				|| cellRef == null || cellRef.isBlank()
				|| fieldName == null || fieldName.isBlank())
			{
				continue;
			}

			if (sheetNameFilter != null && !sheetNameFilter.equals(sheetName))
			{
				continue;
			}

			Object value = readBeanValue(bean, fieldName);
			String format = entry.getExcelFormat();

			writeCell(workbook, dataFormat, styleCache, sheetName, cellRef,
				fieldName, value, format, createMissingSheets);
		}
	}

	/**
	 * Read bean value.
	 *
	 * @param bean the bean
	 * @param fieldName the field name
	 * @return the object
	 */
	private Object readBeanValue(Object bean, String fieldName)
	{
		if (bean == null || fieldName == null || fieldName.isBlank())
		{
			return null;
		}

		String methodName = "get" + capitalize(fieldName.trim());
		try
		{
			Method method = bean.getClass().getMethod(methodName);
			return method.invoke(bean);
		}
		catch (ReflectiveOperationException ex)
		{
			LOGGER.warn("Unable to resolve getter {} on bean {}",
				methodName, bean.getClass().getName(), ex);
			return null;
		}
	}

	/**
	 * Write cell.
	 *
	 * @param workbook the workbook
	 * @param dataFormat the data format
	 * @param styleCache the style cache
	 * @param sheetName the sheet name
	 * @param cellRef the cell ref
	 * @param fieldName the field name
	 * @param value the value
	 * @param format the format
	 * @param createMissingSheets the create missing sheets
	 */
	private void writeCell(Workbook workbook, DataFormat dataFormat,
		Map<String, CellStyle> styleCache, String sheetName, String cellRef,
		String fieldName, Object value, String format, boolean createMissingSheets)
	{
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null)
		{
			if (createMissingSheets)
			{
				sheet = workbook.createSheet(sheetName);
			}
			else
			{
				throw new IllegalStateException(
					"Sheet '" + sheetName + "' not found in workbook.");
			}
		}

		CellReference ref = new CellReference(cellRef);
		Row row = sheet.getRow(ref.getRow());
		if (row == null)
		{
			row = sheet.createRow(ref.getRow());
		}

		Cell cell = row.getCell(ref.getCol());
		boolean existingCell = cell != null;
		if (cell == null)
		{
			cell = row.createCell(ref.getCol());
		}

		if (existingCell && sheet.getProtect()
			&& cell.getCellStyle() != null
			&& cell.getCellStyle().getLocked())
		{
			LOGGER.info(
				"Skipping locked cell {} on sheet {} for field {}.",
				cellRef, sheetName, fieldName);
			return;
		}

		if (value == null)
		{
			applyNullPlaceholder(cell, fieldName);
		}
		else if (value instanceof Number number)
		{
			cell.setCellValue(number.doubleValue());
			cell.setCellType(CellType.NUMERIC);
		}
		else if (value instanceof Boolean bool)
		{
			cell.setCellValue(bool);
			cell.setCellType(CellType.BOOLEAN);
		}
		else if (value instanceof LocalDate date)
		{
			cell.setCellValue(java.sql.Date.valueOf(date));
			cell.setCellType(CellType.NUMERIC);
		}
		else
		{
			cell.setCellValue(value.toString());
			cell.setCellType(CellType.STRING);
		}

		if (format != null && !format.isBlank())
		{
			CellStyle style = styleCache.computeIfAbsent(format, key -> {
				CellStyle created = workbook.createCellStyle();
				created.setDataFormat(dataFormat.getFormat(key));
				return created;
			});
			cell.setCellStyle(style);
		}
	}

	/**
	 * Apply null placeholder.
	 *
	 * @param cell the cell
	 * @param fieldName the field name
	 */
	private void applyNullPlaceholder(Cell cell, String fieldName)
	{
		ReportContext context = ReportContextHolder.get();
		if (context != null && context.getNullPlaceholderSkipFields() != null
			&& fieldName != null
			&& context.getNullPlaceholderSkipFields().contains(fieldName))
		{
			cell.setBlank();
			return;
		}

		if (context == null || context.getNullPlaceholder() == null)
		{
			cell.setBlank();
			return;
		}

		cell.setCellValue(context.getNullPlaceholder());
		cell.setCellType(CellType.STRING);
	}

	/**
	 * Capitalize.
	 *
	 * @param value the value
	 * @return the string
	 */
	private static String capitalize(String value)
	{
		if (value == null || value.isEmpty())
		{
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
