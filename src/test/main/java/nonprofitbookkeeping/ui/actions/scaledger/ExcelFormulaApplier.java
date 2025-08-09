package nonprofitbookkeeping.ui.actions.scaledger;

import java.util.Vector;

import nonprofitbookkeeping.model.ExcelWorkbookPage;

/**
 * Placeholder class intended for applying spreadsheet-style formulas to manually
 * evaluated values within an {@link ExcelWorkbookPage}.
 * Currently, this class does not implement actual formula evaluation logic.
 */
public class ExcelFormulaApplier
{
	/**
	 * Applies formulas to the given workbook page data.
	 * <p>
	 * Note: This is currently a placeholder implementation. It does not parse or evaluate
	 * any formulas. Instead, it creates and returns a deep clone of the input
	 * {@code valuesExcelWorkbookPage}. The intention is for this method to eventually
	 * process cells that contain formulas, calculate their results based on other cell values,
	 * and replace the formula string with the calculated value in the output page.
	 * </p>
	 *
	 * @param valuesExcelWorkbookPage The {@link ExcelWorkbookPage} containing the data,
	 *                                which may include cells with formula strings or pre-evaluated values.
	 * @return A new {@link ExcelWorkbookPage} instance. In the current placeholder implementation,
	 *         this is a deep clone of the input page. In a full implementation, this would be
	 *         a page with formulas evaluated and replaced by their results.
	 */
	public static ExcelWorkbookPage apply(ExcelWorkbookPage valuesExcelWorkbookPage)
	{
	    // Just return a clone of evaluated values as the "result"
	    ExcelWorkbookPage resultExcelWorkbookPage = new ExcelWorkbookPage();

		if (valuesExcelWorkbookPage != null && valuesExcelWorkbookPage.getData() != null) {
		    for (Vector<String> row : valuesExcelWorkbookPage.getData())
		    {
		        Vector<String> newRow = new Vector<>(row); // Clone the row
		        resultExcelWorkbookPage.addRow(newRow);
		    }
		}

	    return resultExcelWorkbookPage;
	}

}
