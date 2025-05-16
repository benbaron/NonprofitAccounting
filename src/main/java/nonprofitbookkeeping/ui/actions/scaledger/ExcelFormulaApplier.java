package nonprofitbookkeeping.ui.actions.scaledger;

import java.util.Vector;

import nonprofitbookkeeping.model.ExcelWorkbookPage;

/**
 * Placeholder for applying spreadsheet-style formulas to evaluated values manually.
 */
public class ExcelFormulaApplier
{
	public static ExcelWorkbookPage apply(ExcelWorkbookPage valuesExcelWorkbookPage)
	{
	    // Just return a clone of evaluated values as the "result"
	    ExcelWorkbookPage resultExcelWorkbookPage = new ExcelWorkbookPage();

	    for (Vector<String> row : valuesExcelWorkbookPage.getData())
	    {
	        Vector<String> newRow = new Vector<>(row);
	        resultExcelWorkbookPage.addRow(newRow);
	    }

	    return resultExcelWorkbookPage;
	}

}
