
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.service.DocumentStorageService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DocumentsPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 7849253176450078187L;
	private final DocumentStorageService service;
	
	public DocumentsPanel(DocumentStorageService service)
	{
		this.service = service;
		buildUI();
	}
	
	/**
	 * 
	 */
	private void buildUI()
	{
		setLayout(new BorderLayout());
		
		JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		uploadPanel.setBorder(new TitledBorder("Attach Document"));
		
		JTextField txnIdField = new JTextField(10);
		JButton chooseBtn = new JButton("Choose File");
		JButton attachBtn = new JButton("Attach");
		
		final File[] selectedFile =
		{ null };
		
		chooseBtn.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION)
			{
				selectedFile[0] = chooser.getSelectedFile();
			}
			
		});
		
		attachBtn.addActionListener(e -> {
			
			if (selectedFile[0] != null && !txnIdField.getText().isBlank())
			{
				
				try
				{
					this.service.attachDocumentToTransaction(txnIdField.getText(), selectedFile[0]);
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				JOptionPane.showMessageDialog(this, "Document attached.");
			}
			
		});
		
		uploadPanel.add(new JLabel("Transaction ID:"));
		uploadPanel.add(txnIdField);
		uploadPanel.add(chooseBtn);
		uploadPanel.add(attachBtn);
		
		add(uploadPanel, BorderLayout.NORTH);
	}
	
}
