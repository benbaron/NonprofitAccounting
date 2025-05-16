
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.service.CompanyLoader;
import nonprofitbookkeeping.service.NoFileException;
import nonprofitbookkeeping.service.PreferencesService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CompanySelectionPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -7273279006543547793L;
	private JList<String> companyList;
	private DefaultListModel<String> listModel;
	private List<File> npbkFiles;
	private JTextArea previewArea;
	
	public CompanySelectionPanel()
	{
		setLayout(new BorderLayout());
		
		// List of companies
		this.listModel = new DefaultListModel<>();
		this.companyList = new JList<>(this.listModel);
		JScrollPane listScroll = new JScrollPane(this.companyList);
		listScroll.setBorder(new TitledBorder("Available Companies"));
		
		// Preview info
		this.previewArea = new JTextArea(10, 40);
		this.previewArea.setEditable(false);
		JScrollPane previewScroll = new JScrollPane(this.previewArea);
		previewScroll.setBorder(new TitledBorder("Preview"));
		
		JSplitPane splitPane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, previewScroll);
		splitPane.setDividerLocation(300);
		
		// Load companies
		reloadCompanyList();
		
		this.companyList.addListSelectionListener(e -> {
			int index = this.companyList.getSelectedIndex();
			
			if (index >= 0 && index < this.npbkFiles.size())
			{
				File file = this.npbkFiles.get(index);
				CompanyDataFile model = null;
				
				try
				{
					model = CompanyLoader.loadCompanyProfile(file);
				}
				catch (NoFileException e1)
				{
					e1.printStackTrace();
				}
				
				if (model != null)
				{
					this.previewArea.setText(model.toString());
				}
				else
				{
					this.previewArea.setText("Failed to load company.");
				}
				
			}
			
		});
		
		// Buttons
		JButton openButton = new JButton("Open Selected");
		JButton createButton = new JButton("Create New Company...");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(createButton);
		
		add(splitPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void reloadCompanyList()
	{
		String dirPath = PreferencesService.getDefaultCompanyDir();
		File dir = new File(dirPath);
		
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		
		this.npbkFiles = CompanyLoader.findCompanyFiles(dir);
		this.listModel.clear();
		
		for (File f : this.npbkFiles)
		{
			this.listModel.addElement(f.getName());
		}
		
	}
	
	// Main method for testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("SSBudgetMainPanel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new CompanySelectionPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
