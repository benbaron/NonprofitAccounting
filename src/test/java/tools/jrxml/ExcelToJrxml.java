
package tools.jrxml;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;


/**
 * The Class ExcelToJrxml.
 */
public class ExcelToJrxml
{
	
	/** The Constant CELL_W. */
	private static final int CELL_W = 60;
	
	/** The Constant CELL_H. */
	private static final int CELL_H = 20;
	
	/** The Constant W_DEFAULT. */
	private static final int W_DEFAULT = 200;
	
	/** The Constant H_DEFAULT. */
	private static final int H_DEFAULT = 20;
	
	/** The blue sample rgb. */
	private static byte[] blueSampleRgb = null;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception
	{
		
		if (args.length < 2)
		{
			System.err
				.println("Usage: ExcelToJrxml <input.xlsx> <output.jrxml>");
			return;
		}
		
		String excelPath = args[0];
		String jrxmlPath = args[1];
		
		try (FileInputStream fis = new FileInputStream(excelPath);
			Workbook wb = new XSSFWorkbook(fis))
		{
			
			Sheet sheet = wb.getSheet("CONTACT_INFO_1");
			
			// Sample blue color from D10
			Row row10 = sheet.getRow(9); // 0-based
			PrintSetup ps = sheet.getPrintSetup();
			
			int paperWidth = 595; // fallback A4 points
			int paperHeight = 842;
			
			if (ps.getPaperSize() == PrintSetup.A4_PAPERSIZE)
			{
				paperWidth = 595; // Jasper expects in points
				paperHeight = 842;
			}
			else if (ps.getPaperSize() == PrintSetup.LETTER_PAPERSIZE)
			{
				paperWidth = 612;
				paperHeight = 792;
			}
			
			// Create a new document.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			
			double leftMargin = sheet.getMargin(Sheet.LeftMargin) * 72; // inches
																		// →
																		// points
			double rightMargin = sheet.getMargin(Sheet.RightMargin) * 72;
			double topMargin = sheet.getMargin(Sheet.TopMargin) * 72;
			double bottomMargin = sheet.getMargin(Sheet.BottomMargin) * 72;
			
			Element jasperReport = doc.createElement("jasperReport");
			jasperReport.setAttribute("name", sheet.getSheetName());
			jasperReport.setAttribute("language", "java");
			jasperReport.setAttribute("pageWidth", String.valueOf(paperWidth));
			jasperReport.setAttribute("pageHeight",
				String.valueOf(paperHeight));
			jasperReport.setAttribute("columnWidth",
				String.valueOf(paperWidth - leftMargin - rightMargin));
			jasperReport.setAttribute("leftMargin",
				String.valueOf((int) leftMargin));
			jasperReport.setAttribute("rightMargin",
				String.valueOf((int) rightMargin));
			jasperReport.setAttribute("topMargin",
				String.valueOf((int) topMargin));
			jasperReport.setAttribute("bottomMargin",
				String.valueOf((int) bottomMargin));
			jasperReport.setAttribute("uuid", UUID.randomUUID().toString());
			
			doc.appendChild(jasperReport);
			
			Element title = doc.createElement("title");
			jasperReport.appendChild(title);
			
			Element band = doc.createElement("band");
			band.setAttribute("height", "760");
			title.appendChild(band);
			
			// for all rows in sheet
			for (Row row : sheet)
			{
				
				// for all cells in row
				for (Cell cell : row)
				{
					
					String value = getStringValue(cell);
					
					if (value == null || value.isEmpty())
					{
						continue;
					}
					
					// ignore green (i.e. border) cells
					if (isGreen(cell))
					{
						System.out.printf("Ignored GREEN cell R%dC%d '%s'%n",
							cell.getRowIndex() + 1, cell.getColumnIndex() + 1,
							value);
						continue;
					}
					
					// build an item
					constructElement(doc, band, cell, value);
					
				}
				
			}
			
			// output source tree
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
				"2");
			t.transform(new DOMSource(doc),
				new StreamResult(new File(jrxmlPath)));
			
			System.out.println("Wrote: " + jrxmlPath);
		}
		
	}
	
	/**
	 * Gets the string value.
	 *
	 * @param cell the cell
	 * @return the string value
	 */
	private static String getStringValue(Cell cell)
	{
		String value;
		
		switch(cell.getCellType())
		{
			case STRING:
				value = cell.getStringCellValue();
				break;
				
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell))
				{
					value = cell.getDateCellValue().toString(); // or format it
				}
				else
				{
					value = Double.toString(cell.getNumericCellValue());
				}
				break;
				
			case BOOLEAN:
				value = Boolean.toString(cell.getBooleanCellValue());
				break;
				
			case FORMULA:
				switch(cell.getCachedFormulaResultType())
				{
					case STRING:
						value = cell.getRichStringCellValue().getString();
						break;
						
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell))
						{
							value = cell.getDateCellValue().toString();
						}
						else
						{
							value = Double.toString(cell.getNumericCellValue());
						}
						break;
						
					case BOOLEAN:
						value = Boolean.toString(cell.getBooleanCellValue());
						break;
						
					default:
						value = "";
				}
				break;
				
			case BLANK:
				value = "";
				break;
				
			default:
				value = "";
		}
		
		return value;
		
		
	}
	
	/**
	 * Construct element.
	 *
	 * @param doc the doc
	 * @param band the band
	 * @param cell the cell
	 * @param value the value
	 * @throws DOMException the DOM exception
	 */
	static void constructElement(Document doc, Element band, Cell cell,
		String value) throws DOMException
	{
		int x = cell.getColumnIndex() * CELL_W;
		int y = cell.getRowIndex() * CELL_H;
		
		Element element = doc.createElement("element");
		band.appendChild(element);
		
		Element reportElement = doc.createElement("reportElement");
		
		reportElement.setAttribute("x", String.valueOf(x));
		reportElement.setAttribute("y", String.valueOf(y));
		reportElement.setAttribute("width",
			String.valueOf(W_DEFAULT));
		reportElement.setAttribute("height",
			String.valueOf(H_DEFAULT));
		element.appendChild(reportElement);
		
		element.appendChild(doc.createElement("textElement"));
		
		if (isBlue(cell))
		{
			System.out.printf(
				"Dynamic BLUE cell R%dC%d -> $F{%s}%n",
				cell.getRowIndex() + 1, cell.getColumnIndex() + 1,
				"R" + (cell.getRowIndex() + 1) + "C" +
					(cell.getColumnIndex() + 1));
			
			Element expr = doc.createElement("textFieldExpression");
			expr.setTextContent(
				"$F{" + "R" + (cell.getRowIndex() + 1) +
					"C" + (cell.getColumnIndex() + 1) + "}");
			element.appendChild(expr);
		}
		else
		{
			System.out.printf("Static cell R%dC%d -> '%s'%n",
				cell.getRowIndex() + 1, cell.getColumnIndex() + 1,
				value);
			
			Element txt = doc.createElement("text");
			txt.setTextContent(value);
			element.appendChild(txt);
		}
		
	}
	
	/**
	 * Checks if is dynamic.
	 *
	 * @param cell the cell
	 * @return true, if is dynamic
	 */
	private static boolean isDynamic(Cell cell)
	{
		XSSFColor fill =
			(XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
		if (fill == null)
			return false;
		String hex = fill.getARGBHex();
		return "#FF00B0F0".equalsIgnoreCase(hex); // example blue
		
	}
	
	/**
	 * Checks if is ignored.
	 *
	 * @param cell the cell
	 * @return true, if is ignored
	 */
	private static boolean isIgnored(Cell cell)
	{
		XSSFColor fill =
			(XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
		if (fill == null)
			return false;
		String hex = fill.getARGBHex();
		return "#FF92D050".equalsIgnoreCase(hex); // example green
		
	}
	
	/**
	 * Checks if is blue.
	 *
	 * @param cell the cell
	 * @return true, if is blue
	 */
	private static boolean isBlue(Cell cell)
	{
		XSSFColor color =
			(XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
		if (color == null || blueSampleRgb == null)
			return false;
		byte[] rgb = color.getRGB();
		if (rgb == null)
			return false;
		return java.util.Arrays.equals(rgb, blueSampleRgb);
		
	}
	
	/**
	 * Checks if is green.
	 *
	 * @param cell the cell
	 * @return true, if is green
	 */
	private static boolean isGreen(Cell cell)
	{
		XSSFColor color =
			(XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
		if (color == null)
			return false;
		byte[] rgb = color.getRGB();
		if (rgb == null)
			return false;
		int r = rgb[0] & 0xFF;
		int g = rgb[1] & 0xFF;
		int b = rgb[2] & 0xFF;
		return (g > 150 && r < 100 && b < 100);
		
	}
	
	/**
	 * Bytes to hex.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	private static String bytesToHex(byte[] bytes)
	{
		if (bytes == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		
		for (byte b : bytes)
		{
			sb.append(String.format("%02X", b));
		}
		
		return sb.toString();
		
	}
	
	
}
