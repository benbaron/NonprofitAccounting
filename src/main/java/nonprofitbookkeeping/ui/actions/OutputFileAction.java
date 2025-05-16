package nonprofitbookkeeping.ui.actions;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * Opens a file chooser to select an output file location.
 */
public class OutputFileAction implements java.awt.event.ActionListener
{
    private Component frame;

	@Override public void actionPerformed(java.awt.event.ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Output File");
        
        // Set the current directory to the last used directory, or user.home if not set
        chooser.setCurrentDirectory(new File(PreferencesManager.getLastWriteDirectory()));
        int result = chooser.showSaveDialog(this.frame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = chooser.getSelectedFile();
            // Save the directory of the selected file for future use
            PreferencesManager.setLastDirectory(selectedFile.getParent());
            
            JOptionPane.showMessageDialog(this.frame,
                    "Selected Output File:\n" + selectedFile.getAbsolutePath());
        }
    }
}

