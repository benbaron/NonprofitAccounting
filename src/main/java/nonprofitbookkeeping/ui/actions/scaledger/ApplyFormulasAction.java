
package nonprofitbookkeeping.ui.actions.scaledger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.service.FinancialFormulaService;
import nonprofitbookkeeping.ui.NonprofitBookkeeping;

public class ApplyFormulasAction implements ActionListener
{
    /**
     * Invoked when an action occurs.
     * Retrieves the ledger, applies formulas, and displays the computed result.
     *
     * @param e the ActionEvent.
     */
	@Override public void actionPerformed(ActionEvent e)
	{
		// Retrieve the ledger from the main application beans.
		Object ledgerObj = NonprofitBookkeeping.beans.get("ledger");
		
		if (ledgerObj == null || !(ledgerObj instanceof Ledger))
		{
			JOptionPane.showMessageDialog(null, "No ledger loaded. Please load a ledger first.");
			return;
		}
		
		Ledger ledger = (Ledger) ledgerObj;
		
		try
		{
			// Apply real financial formulas using a service.
			// FinancialFormulaService.applyFormulas(ledger) calculates a meaningful result.
			double computedValue = FinancialFormulaService.applyFormulas(ledger);
			
			// Store the result in the global beans for further use.
			NonprofitBookkeeping.beans.put("formulaResult", computedValue);
			
			// Display the computed total to the user.
			JOptionPane.showMessageDialog(null,
				"Formulas applied successfully.\nTotal computed value: $" +
					String.format("%.2f", computedValue));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error applying formulas: " + ex.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
}
