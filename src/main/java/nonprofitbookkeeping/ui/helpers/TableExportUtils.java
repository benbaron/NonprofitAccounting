package nonprofitbookkeeping.ui.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.print.PrinterJob;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Utility methods to print and export JavaFX {@link TableView} data.
 */
public final class TableExportUtils
{
	private TableExportUtils()
	{
		// Utility class
	}

	/**
	 * Prompts the user with a printer dialog and prints the provided table.
	 *
	 * @param table the {@link TableView} to print
	 */
	public static void printTable(TableView<?> table)
	{
		if (table == null || table.getScene() == null)
		{
			return;
		}

		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null)
		{
			return;
		}

		Window owner = table.getScene().getWindow();
		boolean proceed = job.showPrintDialog(owner);
		if (proceed)
		{
			job.printPage(table);
			job.endJob();
		}
	}

	/**
	 * Exports the table data to a PDF file using a simple text layout.
	 *
	 * @param table the {@link TableView} to export
	 * @param title title to render at the top of the PDF
	 * @param owner the owner window for the file chooser
	 */
	public static void exportTableToPdf(TableView<?> table, String title,
		Window owner)
	{
		if (table == null)
		{
			return;
		}

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export PDF");
		chooser.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
		File file = chooser.showSaveDialog(owner);
		if (file == null)
		{
			return;
		}

		List<TableColumn<?, ?>> columns = getLeafColumns(table);

		try (PDDocument document = new PDDocument())
		{
			PDPage page = new PDPage(PDRectangle.LETTER);
			document.addPage(page);

			float margin = 40f;
			float y = page.getMediaBox().getHeight() - margin;
			PDPageContentStream content =
				new PDPageContentStream(document, page);
			content.setFont(PDType1Font.HELVETICA, 10);

			y = writePdfLine(content, margin, y, title, true);
			y -= 8f;
			y = writePdfLine(content, margin, y,
				String.join(" | ", columnTitles(columns)), false);

			for (Object item : table.getItems())
			{
				String line = buildRowLine(columns, item);
				y -= 4f;
				if (y < margin)
				{
					content.close();
					page = new PDPage(PDRectangle.LETTER);
					document.addPage(page);
					y = page.getMediaBox().getHeight() - margin;
					content = new PDPageContentStream(document, page);
					content.setFont(PDType1Font.HELVETICA, 10);
				}
				y = writePdfLine(content, margin, y, line, false);
			}

			content.close();
			document.save(file);
		}
		catch (IOException ex)
		{
			AlertBox.showError(owner, "Export Failed",
				"Unable to export PDF: " + ex.getMessage());
		}
	}

	/**
	 * Exports the table data to an XLSX file.
	 *
	 * @param table the {@link TableView} to export
	 * @param sheetName name for the Excel sheet
	 * @param owner the owner window for the file chooser
	 */
	public static void exportTableToXlsx(TableView<?> table, String sheetName,
		Window owner)
	{
		if (table == null)
		{
			return;
		}

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export Excel");
		chooser.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
		File file = chooser.showSaveDialog(owner);
		if (file == null)
		{
			return;
		}

		List<TableColumn<?, ?>> columns = getLeafColumns(table);

		try (Workbook workbook = new XSSFWorkbook())
		{
			Sheet sheet = workbook.createSheet(
				sheetName == null || sheetName.isBlank() ? "Report" : sheetName);
			int rowIndex = 0;
			Row header = sheet.createRow(rowIndex++);
			for (int colIndex = 0; colIndex < columns.size(); colIndex++)
			{
				header.createCell(colIndex)
					.setCellValue(columns.get(colIndex).getText());
			}

			for (Object item : table.getItems())
			{
				Row row = sheet.createRow(rowIndex++);
				for (int colIndex = 0; colIndex < columns.size(); colIndex++)
				{
					TableColumn<?, ?> column = columns.get(colIndex);
					Object value = getCellValue(column, item);
					row.createCell(colIndex)
						.setCellValue(value != null ? value.toString() : "");
				}
			}

			for (int colIndex = 0; colIndex < columns.size(); colIndex++)
			{
				sheet.autoSizeColumn(colIndex);
			}

			try (FileOutputStream out = new FileOutputStream(file))
			{
				workbook.write(out);
			}
		}
		catch (IOException ex)
		{
			AlertBox.showError(owner, "Export Failed",
				"Unable to export Excel: " + ex.getMessage());
		}
	}

	private static List<TableColumn<?, ?>> getLeafColumns(TableView<?> table)
	{
		List<TableColumn<?, ?>> columns = new ArrayList<>();
		for (TableColumn<?, ?> column : table.getColumns())
		{
			collectLeafColumns(column, columns);
		}
		return columns;
	}

	private static void collectLeafColumns(TableColumn<?, ?> column,
		List<TableColumn<?, ?>> columns)
	{
		if (column.getColumns().isEmpty())
		{
			columns.add(column);
			return;
		}

		for (TableColumn<?, ?> child : column.getColumns())
		{
			collectLeafColumns(child, columns);
		}
	}

	private static List<String> columnTitles(List<TableColumn<?, ?>> columns)
	{
		List<String> titles = new ArrayList<>();
		for (TableColumn<?, ?> column : columns)
		{
			titles.add(column.getText());
		}
		return titles;
	}

	private static String buildRowLine(List<TableColumn<?, ?>> columns,
		Object item)
	{
		List<String> values = new ArrayList<>();
		for (TableColumn<?, ?> column : columns)
		{
			Object value = getCellValue(column, item);
			values.add(value != null ? value.toString() : "");
		}
		String line = String.join(" | ", values);
		return line.length() > 180 ? line.substring(0, 177) + "..." : line;
	}

	private static Object getCellValue(TableColumn<?, ?> column, Object item)
	{
		if (column == null)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		TableColumn<Object, ?> typedColumn = (TableColumn<Object, ?>) column;
		return typedColumn.getCellData(item);
	}

	private static float writePdfLine(PDPageContentStream content, float x,
		float y, String line, boolean bold) throws IOException
	{
		content.beginText();
		content.setFont(bold ? PDType1Font.HELVETICA_BOLD :
			PDType1Font.HELVETICA, 10);
		content.newLineAtOffset(x, y);
		content.showText(line != null ? line : "");
		content.endText();
		return y - 12f;
	}
}
