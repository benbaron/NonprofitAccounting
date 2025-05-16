
package nonprofitbookkeeping.ui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import nonprofitbookkeeping.service.FileExportService; // Assume service exists for exporting

public class ExportFileAction extends AbstractAction
{
	
	public ExportFileAction()
	{
		super("Export File");
	}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save File");
		
		int returnValue = fileChooser.showSaveDialog(null);
		
		if (returnValue == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fileChooser.getSelectedFile();
			
			// Assume FileExportService handles the export logic.
			FileExportService exportService = new FileExportService();
			boolean success = FileExportService.exportFile(selectedFile);
			
			if (success)
			{
				JOptionPane.showMessageDialog(null, "File exported successfully.");
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Failed to export the file.");
			}
			
		}
		
	}
	
}
