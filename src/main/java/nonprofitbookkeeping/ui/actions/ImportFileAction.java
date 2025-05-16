
package nonprofitbookkeeping.ui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import nonprofitbookkeeping.service.FileImportService; // Assume service exists for importing

public class ImportFileAction extends AbstractAction
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 8718475554662793041L;

	public ImportFileAction()
	{
		super("Import File");
	}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		JFileChooser fileChooser = new JFileChooser();
		int returnValue = fileChooser.showOpenDialog(null);
		
		if (returnValue == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fileChooser.getSelectedFile();
			
			// Assume the FileImportService handles importing files.
			FileImportService importService = new FileImportService();
			boolean success = FileImportService.importFile(selectedFile);
			
			if (success)
			{
				JOptionPane.showMessageDialog(null, "File imported successfully.");
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Failed to import the file.");
			}
			
		}
		
	}
	
}
