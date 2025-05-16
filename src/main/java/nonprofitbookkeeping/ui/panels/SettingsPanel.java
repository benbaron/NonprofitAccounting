
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5974347401299557803L;
	
	public SettingsPanel()
	{
		setLayout(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel companyPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		companyPanel.setBorder(new TitledBorder("Company Information"));
		companyPanel.add(new JLabel("Organization Name:"));
		companyPanel.add(new JTextField("My Nonprofit"));
		companyPanel.add(new JLabel("Fiscal Year Start:"));
		companyPanel.add(new JTextField("2025-01-01"));
		companyPanel.add(new JLabel("Default Currency:"));
		companyPanel.add(new JComboBox<>(new String[]
		{ "USD", "EUR", "GBP" }));
		
		JPanel userPanel = new JPanel(new BorderLayout());
		userPanel.setBorder(new TitledBorder("User Management"));
		JTable userTable = new JTable(new Object[][]
		{
			{ "admin", "Administrator" },
			{ "user1", "Viewer" }
		}, new String[]
		{ "Username", "Role" });
		userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
		
		JPanel accountingPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		accountingPanel.setBorder(new TitledBorder("Accounting Settings"));
		accountingPanel.add(new JLabel("Default Income Account:"));
		accountingPanel.add(new JTextField("Donations"));
		accountingPanel.add(new JLabel("Default Expense Account:"));
		accountingPanel.add(new JTextField("Office Supplies"));
		accountingPanel.add(new JLabel("Auto-Number Vouchers:"));
		accountingPanel.add(new JCheckBox("Enabled", true));
		
		JPanel backupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		backupPanel.setBorder(new TitledBorder("Backup & Restore"));
		JButton backupBtn = new JButton("Create Backup");
		JButton restoreBtn = new JButton("Restore Backup");
		backupBtn.addActionListener(
			e -> JOptionPane.showMessageDialog(this, "Backup process would run here."));
		restoreBtn.addActionListener(
			e -> JOptionPane.showMessageDialog(this, "Restore process would run here."));
		backupPanel.add(backupBtn);
		backupPanel.add(restoreBtn);
		
		JPanel uiPanel = new JPanel(new GridLayout(2, 2, 10, 10));
		uiPanel.setBorder(new TitledBorder("UI Preferences"));
		uiPanel.add(new JLabel("Theme:"));
		uiPanel.add(new JComboBox<>(new String[]
		{ "Light", "Dark", "System" }));
		uiPanel.add(new JLabel("Language:"));
		uiPanel.add(new JComboBox<>(new String[]
		{ "English", "Spanish", "French" }));
		
		tabs.add("Company Info", companyPanel);
		tabs.add("Users", userPanel);
		tabs.add("Accounting", accountingPanel);
		tabs.add("Backup", backupPanel);
		tabs.add("UI Preferences", uiPanel);
		
		add(tabs, BorderLayout.CENTER);
	}
	
	// Main method for testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new SettingsPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
