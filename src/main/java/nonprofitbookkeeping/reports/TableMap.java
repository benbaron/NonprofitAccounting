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
/**
 * A TableModel decorator that wraps another TableModel.
 * This class extends {@link AbstractTableModel} and implements {@link TableModelListener}.
 * It delegates most of the TableModel interface methods to the underlying model.
 * This can be used as a base for further TableModel transformations or filtering.
 */
public class TableMap extends AbstractTableModel implements TableModelListener
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 2746620878654011822L;
	protected TableModel model;


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code getColumnClass} method.
	 * @param aColumn the column being queried.
	 * @return the Class of the objects in the specified column.
	 */
	@Override public Class<?> getColumnClass(int aColumn)
	{
		return this.model.getColumnClass(aColumn);
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code getColumnCount} method.
	 * Returns 0 if the underlying model is null.
	 * @return the number of columns in the model.
	 */
	@Override public int getColumnCount()
	{
		return (this.model == null) ? 0 : this.model.getColumnCount();
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code getColumnName} method.
	 * @param aColumn the column being queried.
	 * @return a string containing the name of {@code aColumn}.
	 */
	@Override public String getColumnName(int aColumn)
	{
		return this.model.getColumnName(aColumn);
	}


	/**
	 * Gets the underlying {@link TableModel} that this {@code TableMap} is wrapping.
	 *
	 * @return The underlying table model.
	 */
	public TableModel getModel()
	{
		return this.model;
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code getRowCount} method.
	 * Returns 0 if the underlying model is null.
	 * @return the number of rows in the model.
	 */
	@Override public int getRowCount()
	{
		return (this.model == null) ? 0 : this.model.getRowCount();
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code getValueAt} method.
	 * @param aRow the row whose value is to be queried.
	 * @param aColumn the column whose value is to be queried.
	 * @return the value Object at the specified cell.
	 */
	@Override public Object getValueAt(int aRow, int aColumn)
	{
		return this.model.getValueAt(aRow, aColumn);
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code isCellEditable} method.
	 * @param row the row whose value is to be queried.
	 * @param column the column whose value is to be queried.
	 * @return true if the cell is editable.
	 */
	@Override public boolean isCellEditable(int row, int column)
	{
		return this.model.isCellEditable(row, column);
	}


	/**
	 * Sets the underlying {@link TableModel} that this {@code TableMap} will wrap.
	 * This {@code TableMap} will also be added as a {@link TableModelListener} to the new model.
	 *
	 * @param model The new underlying table model to set.
	 */
	public void setModel(TableModel model)
	{
		this.model = model;
		model.addTableModelListener(this);
	}


	/**
	 * {@inheritDoc}
	 * This implementation delegates to the underlying model's {@code setValueAt} method.
	 * @param aValue the new value.
	 * @param aRow the row whose value is to be changed.
	 * @param aColumn the column whose value is to be changed.
	 */
	@Override public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		this.model.setValueAt(aValue, aRow, aColumn);
	}


	/**
	 * {@inheritDoc}
	 * This implementation is called when the underlying model changes.
	 * It forwards the {@link TableModelEvent} to listeners of this {@code TableMap}
	 * by calling {@code fireTableChanged(e)}.
	 * @param e a {@code TableModelEvent} tolanguageTagthis model's listeners.
	 */
	@Override public void tableChanged(TableModelEvent e)
	{
		fireTableChanged(e);
	}

}
