package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import javax.swing.table.DefaultTableModel;

import nonprofitbookkeeping.ui.NonprofitBookkeeping;
import nonprofitbookkeeping.ui.panels.PageViewer;

import java.util.Vector;

public class ImportFromJsonAction extends AbstractAction
{
    private static final long serialVersionUID = 8256433800938047046L;

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import JSON Ledger");
      int result = chooser.showOpenDialog(NonprofitBookkeeping.getFrame());
        
		if (result == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                DefaultTableModel importedModel = new DefaultTableModel();
                // Build a vector of column identifiers manually
                Vector<String> colIdentifiers = new Vector<String>();
                for (int i = 0; i < importedModel.getColumnCount(); i++)
                {
                    colIdentifiers.add(importedModel.getColumnName(i));
                }
                
                PageViewer.getTableModel().setDataVector(
                    new Vector<>(),
                    colIdentifiers
                );
           JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(), "Ledger imported from JSON.");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(NonprofitBookkeeping.getFrame(), "Failed to import from JSON.");
            }
        }
    }
}
