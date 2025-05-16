
package nonprofitbookkeeping.ui.actions;

import javax.swing.*;

import nonprofitbookkeeping.preferences.*;
import nonprofitbookkeeping.ui.NonprofitBookkeeping;
import nonprofitbookkeeping.ui.actions.scaledger.SCALedgerDataLoader;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

// loads XLSM and maps beans

public class InputFileAction extends AbstractAction
{
	public static Map<String, Object> beans;
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6512987497206913040L;
	
	@Override public void actionPerformed(ActionEvent e)
	{
		chooseFile();
		
	}

	/**
	 * @throws HeadlessException
	 */
	static void chooseFile() throws HeadlessException
	{
		JFileChooser chooser = new JFileChooser();
		
		// Set the current directory to the last used directory, or user.home if not set
        chooser.setCurrentDirectory(new File(PreferencesManager.getLastDirectory()));
		chooser.setDialogTitle("Select Input File");
		
		int result = chooser.showOpenDialog(NonprofitBookkeeping.getFrame());
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			NonprofitBookkeeping.currentInputFile = chooser.getSelectedFile();
			
			// Save the directory of the selected file for future use
            PreferencesManager.setLastDirectory(NonprofitBookkeeping.
            	currentInputFile.getParent());
			printCurrentWorkingDirectory();
			
			File mappingFile = new File("jxlsMapping.xml");		
			
			try
			{
				NonprofitBookkeeping.beans = SCALedgerDataLoader.
					loadData(mappingFile, NonprofitBookkeeping.currentInputFile);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

			
		}
	}


	/**
	 * 
	 */
	public static void printCurrentWorkingDirectory()
	{
		String cwd = System.getProperty("user.dir");
		System.out.println("Current working directory: " + cwd);
	}
}
