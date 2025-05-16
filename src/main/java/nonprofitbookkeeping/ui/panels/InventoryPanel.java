
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5163435844038626654L;
	private final InventoryService service;
	private JTable table;
	private DefaultTableModel model;
	
	public InventoryPanel(InventoryService service)
	{
		this.service = service;
		buildUI();
		refresh();
	}
	
	private void buildUI()
	{
		setLayout(new BorderLayout());
		
		String[] cols =
		{ "ID", "Name", "Qty", "Cost" };
		this.model = new DefaultTableModel(cols, 0);
		this.table = new JTable(this.model);
		add(new JScrollPane(this.table), BorderLayout.CENTER);
		
		JButton updateBtn = new JButton("Update Selected");
		updateBtn.addActionListener(e -> updateSelected());
		add(updateBtn, BorderLayout.SOUTH);
	}
	
	private void refresh()
	{
		this.model.setRowCount(0);
		List<String[]> items = null;
		
		try
		{
			items = InventoryService.getInventoryItems();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		for (String[] row : items)
		{
			this.model.addRow(row);
		}
		
	}
	
	private void updateSelected()
	{
		int row = this.table.getSelectedRow();
		
		if (row != -1)
		{
			String id = (String) this.model.getValueAt(row, 0);
			String name = (String) this.model.getValueAt(row, 1);
			int qty = Integer.parseInt(this.model.getValueAt(row, 2).toString());
			double cost = Double.parseDouble(this.model.getValueAt(row, 3).toString());
			this.service.updateInventoryItem(id, name, qty, cost);
			JOptionPane.showMessageDialog(this, "Updated.");
		}
		
	}
	
}
