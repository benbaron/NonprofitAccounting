
package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionEvent;
import java.io.File;

import nonprofitbookkeeping.ui.NonprofitBookkeeping;
import nonprofitbookkeeping.ui.panels.PageViewer;

public class LoadXlsmTableAction extends AbstractAction
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -3005167755594562327L;
	
	@Override public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select XLSM File");
		int result = chooser.showOpenDialog(NonprofitBookkeeping.getFrame());
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			
			try
			{
				// Read the table model from the XLSM file; sheetIndex is 0 (modify as needed)
				DefaultTableModel model = XlsmTableViewer.readXlsmToTableModel(file, 0);
				// Set the table model in the PageViewer.
				// (Assumes PageViewer has a method setTableModel(DefaultTableModel))
				PageViewer.setTableModel(model);
				NonprofitBookkeeping.currentInputFile = file;
				NonprofitBookkeeping.getFrame().setTitle("SCALedger - " + file.getName());
				createAndShowTable(model);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(),
					"Error reading XLSM file:\n" + ex.getMessage());
			}
			
		}
		
	}
	
	/**
	 * Creates a JTable with the given table model and displays it in a JFrame.
	 *
	 * @param model the table model to display
	 */
	public static void createAndShowTable(DefaultTableModel model)
	{
		JTable table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		JFrame frame = new JFrame("XLSM Table Viewer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(scrollPane);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null); // center the frame
		frame.setVisible(true);
	}
	
}
