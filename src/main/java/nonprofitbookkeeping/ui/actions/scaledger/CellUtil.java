
package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

// TODO: Auto-generated Javadoc
/**
 * Helpers for extracting typed values from POI cells,
 * with sane fallbacks, without throwing.
 */
public final class CellUtil
{
	
	/**
	 * Instantiates a new cell util.
	 */
	private CellUtil()
	{
	
	}
	
	/**
	 * Read string.
	 *
	 * @param row the row
	 * @param colIdx the col idx
	 * @return the string
	 */
	public static String readString(Row row, int colIdx)
	{
		
		if (row == null || colIdx < 0)
		{
			return null;
		}
		
		Cell cell =
			row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		
		if (cell == null)
		{
			return null;
		}
		
		CellType type = cell.getCellType();
		
		if (type == CellType.STRING)
		{
			return cell.getStringCellValue();
		}
		else if (type == CellType.NUMERIC &&
			!DateUtil.isCellDateFormatted(cell))
		{
			return Double.toString(cell.getNumericCellValue());
		}
		else if (type == CellType.FORMULA)
		{
			// For headers we sometimes only care about the formula text.
			return cell.getCellFormula();
		}
		
		return null;
		
	}
	
	/**
	 * Read date.
	 *
	 * @param row the row
	 * @param colIdx the col idx
	 * @return the local date
	 */
	public static LocalDate readDate(Row row, int colIdx)
	{
		
		if (row == null || colIdx < 0)
		{
			return null;
		}
		
		Cell cell =
			row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		
		if (cell == null)
		{
			return null;
		}
		
		if (cell.getCellType() == CellType.NUMERIC &&
			DateUtil.isCellDateFormatted(cell))
		{
			return cell.getDateCellValue()
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}
		
		if (cell.getCellType() == CellType.STRING)
		{
			// Fallback parsing ("MM/DD/YYYY") could be added here if needed.
			return null;
		}
		
		return null;
		
	}
	
	/**
	 * Read amount.
	 *
	 * @param row the row
	 * @param colIdx the col idx
	 * @return the big decimal
	 */
	public static BigDecimal readAmount(Row row, int colIdx)
	{
		
		if (row == null || colIdx < 0)
		{
			return java.math.BigDecimal.ZERO;
		}
		
		Cell cell =
			row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		
		if (cell == null)
		{
			return java.math.BigDecimal.ZERO;
		}
		
		switch(cell.getCellType())
		{
			case NUMERIC:
				// Amount columns (M, R, X, AC) are numeric inputs
				return java.math.BigDecimal.valueOf(cell.getNumericCellValue());
			
			case STRING:
				String s = cell.getStringCellValue();
				if (s == null)
				{
					return java.math.BigDecimal.ZERO;
				}
				s = s.trim();
				if (s.isEmpty())
				{
					return java.math.BigDecimal.ZERO;
				}
				s = s.replace(",", "");
				try
				{
					return new java.math.BigDecimal(s);
				}
				catch (NumberFormatException ex)
				{
					return java.math.BigDecimal.ZERO;
				}
				
			default:
				return java.math.BigDecimal.ZERO;
		}
		
	}
	
}
