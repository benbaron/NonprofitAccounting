
package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;

import nonprofitbookkeeping.ui.NonprofitBookkeeping;
import nonprofitbookkeeping.ui.panels.PageViewer;

import java.awt.event.ActionEvent;
import java.io.File;

// writes edited data back to sheet
public class SaveModifiedCopyAction extends AbstractAction
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -7922132907022354298L;

	@Override public void actionPerformed(ActionEvent e)
	{
		
		if (NonprofitBookkeeping.currentInputFile == null)
		{
			JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(),
				"No input file loaded or ledger selected.");
			return;
		}
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save Modified Workbook");
		int result = chooser.showSaveDialog(NonprofitBookkeeping.getFrame());
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File outputFile = chooser.getSelectedFile();
			
			try
			{
				ExcelDataWriter.writeModifiedCopy(
					NonprofitBookkeeping.currentInputFile,
					outputFile,
					null,
					PageViewer.getTableModel());
				JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(), "Workbook saved successfully.");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(), "Failed to save workbook.");
			}
			
		}
		
	}
	
}
