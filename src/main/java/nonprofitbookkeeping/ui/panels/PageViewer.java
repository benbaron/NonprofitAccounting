// JTable view with editing and undo tracking
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import nonprofitbookkeeping.model.ExcelWorkbookPage;

import java.awt.*;
import java.util.*;

public class PageViewer extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -3730954517017185434L;
	private JTable table;
	private static DefaultTableModel tableModel;
	private static Vector<Vector<String>> originalData = null;
	static Set<Point> editedCells = new HashSet<>();
	private Deque<CellEdit> undoStack = new ArrayDeque<>();
	
	/**
	 * Constructor PageViewer
	 */
	public PageViewer()
	{
		setLayout(new BorderLayout());
		PageViewer.tableModel = new DefaultTableModel();
		
		this.table = new JTable(PageViewer.tableModel)
		{
			/**
			 * serialVersionUID : long
			 */
			private static final long serialVersionUID = -8375586395844586809L;
			
			@Override public boolean isCellEditable(int row, int column)
			{
				return true;
			}
			
		};
		
		this.table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			/**
			 * serialVersionUID : long
			 */
			private static final long serialVersionUID = -2589190398241038369L;
			
			@Override public Component 
				getTableCellRendererComponent(	JTable table1, 
				                              	Object value,
				                              	boolean isSelected,
				                              	boolean hasFocus,
				                              	int row, int column)
			{
				Component c = super.getTableCellRendererComponent(table1, value, isSelected,
					hasFocus, row, column);
				
				if (PageViewer.editedCells.contains(new Point(row, column)))
				{
					c.setBackground(Color.YELLOW);
				}
				else
				{
					c.setBackground(isSelected ? table1.getSelectionBackground() : Color.WHITE);
				}
				
				return c;
			}
			
		});
		
		/**
		 * Set listener for changes
		 */
		PageViewer.tableModel.addTableModelListener(e -> {
			int row = e.getFirstRow();
			int col = e.getColumn();
			
			if (PageViewer.originalData != null && row < PageViewer.originalData.size() && col >= 0)
			{
				String oldValue = PageViewer.originalData.get(row).get(col);
				String newValue = (String) PageViewer.tableModel.getValueAt(row, col);
				
				if (!Objects.equals(oldValue, newValue))
				{
					PageViewer.editedCells.add(new Point(row, col));
				}
				else
				{
					PageViewer.editedCells.remove(new Point(row, col));
				}
				
				this.undoStack.push(new CellEdit(row, col, oldValue, newValue));
				repaint();
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane(this.table);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * 
	 * @param excelWorkbookPage
	 */
	public void setExcelWorkbookPage(ExcelWorkbookPage excelWorkbookPage)
	{
		setExcelWorkbookPage(excelWorkbookPage, new Vector<>());
	}
	
	/**
	 * 
	 * @param excelWorkbookPage
	 * @param columnNames
	 */
	public static void setExcelWorkbookPage(ExcelWorkbookPage excelWorkbookPage, Vector<String> columnNames)
	{
		Vector<Vector<String>> data = excelWorkbookPage.getData();
		tableModel.setDataVector(data, columnNames);
		setOriginalData(data);
	}
	
	/**
	 * Get a reference to the table model
	 * 
	 * @return table model
	 */
	public static DefaultTableModel getTableModel()
	{
		return tableModel;
	}
	
	/**
	 * Set the original data vectors
	 * 
	 * @param data
	 */
	public static void setOriginalData(Vector<Vector<String>> data)
	{
		originalData = new Vector<>();
		
		for (Vector<String> row : data)
		{
			originalData.add(new Vector<>(row));
		}
		
		editedCells.clear();
		//repaint();
	}
	
	/**
	 * 
	 */
	public void undoLastEdit()
	{
		
		if (this.undoStack.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Nothing to undo.");
			return;
		}
		
		CellEdit edit = this.undoStack.pop();
		PageViewer.tableModel.setValueAt(edit.oldValue, edit.row, edit.col);
		String original = PageViewer.originalData.get(edit.row).get(edit.col);
		String current = (String) PageViewer.tableModel.getValueAt(edit.row, edit.col);
		Point p = new Point(edit.row, edit.col);
		
		if (!Objects.equals(original, current))
		{
			PageViewer.editedCells.add(p);
		}
		else
		{
			PageViewer.editedCells.remove(p);
		}
		
		repaint();
	}
	
	/**
	 * Cell editor class
	 */
	private static class CellEdit
	{
		int row, col;
		String oldValue;
		
		public CellEdit(int row, int col, String oldValue, String newValue)
		{
			this.row = row;
			this.col = col;
			this.oldValue = oldValue;
		}
		
	}

	/**
	 * 
	 * @param model
	 */
	public static void setTableModel(DefaultTableModel model)
	{
		tableModel = model;		
	}
	
}
