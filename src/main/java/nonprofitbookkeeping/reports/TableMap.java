/* :tabSize=2:indentSize=2:noTabs=true: :folding=explicit:collapseFolds=1:
 * Copyright (C) 2009 Chaniel AB, Thomas Dilts This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA For more information, surf to
 * www.lazy8.nu or email support@lazy8.nu */

package nonprofitbookkeeping.reports;


import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;


/**
 *  Description of the Class
 *
 * @author     Lazy Eight Data HB, Thomas Dilts
 * @created    den 5 mars 2002
 */
public class TableMap extends AbstractTableModel implements TableModelListener
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 2746620878654011822L;
	protected TableModel model;
	
	
	/**
	 *  Gets the columnClass attribute of the TableMap object
	 *
	 * @param  aColumn  Description of the Parameter
	 * @return          The columnClass value
	 */
	@Override public Class<?> getColumnClass(int aColumn)
	{
		return this.model.getColumnClass(aColumn);
	}
	
	
	/**
	 *  Gets the columnCount attribute of the TableMap object
	 *
	 * @return    The columnCount value
	 */
	@Override public int getColumnCount()
	{
		return (this.model == null) ? 0 : this.model.getColumnCount();
	}
	
	
	/**
	 *  Gets the columnName attribute of the TableMap object
	 *
	 * @param  aColumn  Description of the Parameter
	 * @return          The columnName value
	 */
	@Override public String getColumnName(int aColumn)
	{
		return this.model.getColumnName(aColumn);
	}
	
	
	/**
	 *  Gets the model attribute of the TableMap object
	 *
	 * @return    The model value
	 */
	public TableModel getModel()
	{
		return this.model;
	}
	
	
	/**
	 *  Gets the rowCount attribute of the TableMap object
	 *
	 * @return    The rowCount value
	 */
	@Override public int getRowCount()
	{
		return (this.model == null) ? 0 : this.model.getRowCount();
	}
	
	
	/**
	 *  Gets the valueAt attribute of the TableMap object
	 *
	 * @param  aRow     Description of the Parameter
	 * @param  aColumn  Description of the Parameter
	 * @return          The valueAt value
	 */
	@Override public Object getValueAt(int aRow, int aColumn)
	{
		return this.model.getValueAt(aRow, aColumn);
	}
	
	
	/**
	 *  Gets the cellEditable attribute of the TableMap object
	 *
	 * @param  row     Description of the Parameter
	 * @param  column  Description of the Parameter
	 * @return         The cellEditable value
	 */
	@Override public boolean isCellEditable(int row, int column)
	{
		return this.model.isCellEditable(row, column);
	}
	
	
	/**
	 *  Sets the model attribute of the TableMap object
	 *
	 * @param  model  The new model value
	 */
	public void setModel(TableModel model)
	{
		this.model = model;
		model.addTableModelListener(this);
	}
	
	
	/**
	 *  Sets the valueAt attribute of the TableMap object
	 *
	 * @param  aValue   The new valueAt value
	 * @param  aRow     The new valueAt value
	 * @param  aColumn  The new valueAt value
	 */
	@Override public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		this.model.setValueAt(aValue, aRow, aColumn);
	}
	
	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	@Override public void tableChanged(TableModelEvent e)
	{
		fireTableChanged(e);
	}
	
}

