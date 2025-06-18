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


import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 *  Description of the Class
 *
 * @author     Lazy Eight Data HB, Thomas Dilts
 * @created    den 5 mars 2002
 */
/**
 * A decorator for {@link TableModel} that provides sorting capabilities for a {@link JTable}.
 * It extends {@link TableMap} and allows users to sort table rows by clicking on column headers.
 * Supports multi-column sorting (though primary interaction is single-column via header click)
 * and ascending/descending order.
 */
public class TableSorter extends TableMap
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -1240594602058594949L;
	/** Stores the indices of columns currently used for sorting. */
	Vector<Integer> sortingColumns = new Vector<Integer>();
	/** Flag indicating whether the current sort order is ascending (true) or descending (false). */
	boolean ascending = true;
	/** Counter for the number of comparisons performed during a sort operation. */
	int compares;
	/**
	 * An array of integers mapping sorted row indices to the original model's row indices.
	 * {@code indexes[i]} is the original model row index for the sorted view's row {@code i}.
	 */
	int indexes[];

	/**
	 *  Default constructor for the TableSorter.
	 *  Initializes an empty mapping for row indexes.
	 */
	public TableSorter()
	{
		this.indexes = new int[0];
		// For consistency.
	}

	/**
	 *  Constructs a TableSorter that wraps the given {@link TableModel}.
	 *
	 * @param  model  The {@link TableModel} to be sorted.
	 */
	public TableSorter(TableModel model)
	{
		setModel(model);
	}

	/**
	 * Adds a mouse listener to the header of the specified {@link JTable}.
	 * This listener enables sorting of the table by clicking on column headers.
	 * A single click sorts in ascending order (or toggles if already sorted by that column).
	 * A Shift-click sorts in the current direction (or toggles if already sorted by that column, potentially reversing).
	 *
	 * @param  table  The {@link JTable} to which the sort-on-header-click functionality will be added.
	 */
	public void addMouseListenerToHeaderInTable(JTable table)
	{
		final TableSorter sorter = this;
		final JTable tableView = table;
		tableView.setColumnSelectionAllowed(false);
		MouseAdapter listMouseListener =
			new MouseAdapter()
			{

				@Override public void mouseClicked(MouseEvent e)
				{
					TableColumnModel columnModel = tableView.getColumnModel();
					int viewColumn = columnModel.getColumnIndexAtX(e.getX());
					int column = tableView.convertColumnIndexToModel(viewColumn);

					if (e.getClickCount() == 1 && column != -1)
					{
						// SystemLog.ProblemPrintln("Sorting ...");
						@SuppressWarnings("deprecation") int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
						boolean ascending1 = (shiftPressed == 0);
						sorter.sortByColumn(column, ascending1);
					}

				}

			};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}


	/**
	 * Checks if the internal row index mapping ({@code indexes}) is consistent with the
	 * underlying model's row count. This method currently contains commented-out logic
	 * and does not perform any actions. It might be intended for validation or re-synchronization.
	 */
	public void checkModel()
	{
		/* if (indexes.length != model.getRowCount()) { } */
	}

	/**
	 * Compares two rows based on the current sorting columns and order.
	 * Iterates through the {@link #sortingColumns} and uses {@link #compareRowsByColumn(int, int, int)}
	 * for each column until a non-zero result is found.
	 *
	 * @param  row1  The index of the first row (in the original model) to compare.
	 * @param  row2  The index of the second row (in the original model) to compare.
	 * @return A negative integer, zero, or a positive integer as the first row is less than, equal to, or greater than the second.
	 *         The result is adjusted based on the {@link #ascending} flag.
	 */
	public int compare(int row1, int row2)
	{
		this.compares++;

		for (int level = 0; level < this.sortingColumns.size(); level++)
		{
			Integer column = this.sortingColumns.elementAt(level);
			int result = compareRowsByColumn(row1, row2, column.intValue());

			if (result != 0)
			{
				return this.ascending ? result : -result;
			}

		}

		return 0;
	}

	/**
	 * Compares two rows based on the values in a specific column.
	 * Handles various data types including {@link Number}, {@link Date}, {@link String}, and {@link Boolean}.
	 * Null values are considered less than non-null values. Blank strings are typically sorted after non-blank strings.
	 *
	 * @param  row1    The index of the first row (in the original model).
	 * @param  row2    The index of the second row (in the original model).
	 * @param  column  The index of the column to compare by.
	 * @return A negative integer, zero, or a positive integer as the value in the first row/column
	 *         is less than, equal to, or greater than the value in the second row/column.
	 */
	public int compareRowsByColumn(int row1, int row2, int column)
	{
		Class<?> type = this.model.getColumnClass(column);
		TableModel data = this.model;

		// Check for nulls

		Object o1 = data.getValueAt(row1, column);
		Object o2 = data.getValueAt(row2, column);

		// If both values are null return 0
		if (o1 == null && o2 == null)
		{
			return 0;
		}
		else if (o1 == null)
		{
			// Define null less than everything.
			return -1;
		}
		else if (o2 == null)
		{
			return 1;
		}

		/* We copy all returned values from the getValue call in case an optimised model
		 * is reusing one object to return many values. The Number subclasses in the JDK
		 * are immutable and so will not be used in this way but other subclasses of
		 * Number might want to do this to save space and avoid unnecessary heap
		 * allocation. */
		if (type.getSuperclass() == java.lang.Number.class)
		{
			Number n1 = (Number) data.getValueAt(row1, column);
			double d1 = n1.doubleValue();
			Number n2 = (Number) data.getValueAt(row2, column);
			double d2 = n2.doubleValue();

			if (d1 < d2)
			{
				return -1;
			}
			else if (d1 > d2)
			{
				return 1;
			}
			else
			{
				return 0;
			}

		}
		else if (type == java.util.Date.class)
		{
			Date d1 = (Date) data.getValueAt(row1, column);
			long n1 = d1.getTime();
			Date d2 = (Date) data.getValueAt(row2, column);
			long n2 = d2.getTime();

			if (n1 < n2)
			{
				return -1;
			}
			else if (n1 > n2)
			{
				return 1;
			}
			else
			{
				return 0;
			}

		}
		else if (type == String.class)
		{
			String s1 = (String) data.getValueAt(row1, column);
			String s2 = (String) data.getValueAt(row2, column);
			int result = s1.compareTo(s2);

			// want blank strings to come last

			if (s1.length() == 0 || s2.length() == 0)
			{

				// (note rarely used exclusive or)
				if (s1.length() == 0 ^ s2.length() == 0)
				{

					if (s1.length() == 0)
					{
						return 1;
					}
					else
					{
						return -1;
					}

				}
				else
				// both strings are blank here
				// order them by the natural row numbers
				if (row1 > row2)
				{
					return 1;
				}
				else
				{
					return -1;
				}

			}

			if (result < 0)
			{
				return -1;
			}
			else if (result > 0)
			{
				return 1;
			}
			else
			{
				return 0;
			}

		}
		else if (type == Boolean.class)
		{
			Boolean bool1 = (Boolean) data.getValueAt(row1, column);
			boolean b1 = bool1.booleanValue();
			Boolean bool2 = (Boolean) data.getValueAt(row2, column);
			boolean b2 = bool2.booleanValue();

			if (b1 == b2)
			{
				return 0;
			}
			else if (b1)
			{
				// Define false < true
				return 1;
			}
			else
			{
				return -1;
			}

		}
		else
		{
			Object v1 = data.getValueAt(row1, column);
			String s1 = v1.toString();
			Object v2 = data.getValueAt(row2, column);
			String s2 = v2.toString();
			int result = s1.compareTo(s2);

			if (result < 0)
			{
				return -1;
			}
			else if (result > 0)
			{
				return 1;
			}
			else
			{
				return 0;
			}

		}

	}


	/**
	 * {@inheritDoc}
	 * Returns the value at the mapped row index in the underlying model.
	 * {@code aRow} is the row index in the sorted view, which is then mapped to
	 * the original model's row index using the {@code indexes} array.
	 * @param  aRow     The row in the sorted view whose value is to be queried.
	 * @param  aColumn  The column whose value is to be queried.
	 * @return          The value Object at the specified cell in the underlying model.
	 */
	@Override public Object getValueAt(int aRow, int aColumn)
	{
		checkModel(); // Potentially validates or updates model state
		return this.model.getValueAt(this.indexes[aRow], aColumn);
	}


	/**
	 * A simple N^2 sorting algorithm (selection sort variant).
	 * This method is present but {@link #shuttlesort} is used by the main {@link #sort} method.
	 * It sorts the {@code indexes} array in place.
	 */
	public void n2sort()
	{

		for (int i = 0; i < getRowCount(); i++)
		{

			for (int j = i + 1; j < getRowCount(); j++)
			{

				if (compare(this.indexes[i], this.indexes[j]) == -1) // If element i is less than element j by current sort criteria
				{
					swap(i, j); // Swaps the view indices, effectively reordering rows
				}

			}

		}

	}


	/**
	 * Reinitializes the internal {@code indexes} array to match the current row count
	 * of the underlying model. Each index is initially set to its own value (identity mapping),
	 * representing an unsorted state.
	 */
	public void reallocateIndexes()
	{
		int rowCount = this.model.getRowCount();

		// Set up a new array of indexes with the right number of elements
		// for the new data model.
		this.indexes = new int[rowCount];

		// Initialise with the identity mapping.
		for (int row = 0; row < rowCount; row++)
		{
			this.indexes[row] = row;
		}

	}


	/**
	 * {@inheritDoc}
	 * Sets the underlying {@link TableModel} for this sorter.
	 * After setting the model, it calls {@link #reallocateIndexes()} to reset the sort order.
	 * @param  model  The new {@link TableModel} to use.
	 */
	@Override public void setModel(TableModel model)
	{
		super.setModel(model);
		reallocateIndexes();
	}


	/**
	 * {@inheritDoc}
	 * Sets the value in the cell at {@code aRow}, {@code aColumn} in the underlying model,
	 * using the mapped row index from the sorted view.
	 * @param  aValue   The new value.
	 * @param  aRow     The row in the sorted view whose value is to be changed.
	 * @param  aColumn  The column whose value is to be changed.
	 */
	@Override public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		checkModel(); // Potentially validates or updates model state
		this.model.setValueAt(aValue, this.indexes[aRow], aColumn);
	}


	/**
	 * A stable merge sort algorithm used to sort the row indexes.
	 * This implementation sorts the {@code from} array and stores the result in the {@code to} array.
	 * It operates on a sub-array defined by {@code low} (inclusive) and {@code high} (exclusive) indices.
	 * <p>
	 * The original comment mentions:
	 * "This is a home-grown implementation which we have not had time to research
	 *  - it may perform poorly in some circumstances. It requires twice the space
	 *  of an in-place algorithm and makes NlogN assignments shuttling the values
	 *  between the two arrays. The number of compares appears to vary between N-1
	 *  and NlogN depending on the initial order but the main reason for using it
	 *  here is that, unlike qsort, it is stable."
	 * </p>
	 * @param  from  The source array of row indexes to be sorted.
	 * @param  to    The destination array where the sorted row indexes will be placed.
	 * @param  low   The starting index (inclusive) of the sub-array to sort.
	 * @param  high  The ending index (exclusive) of the sub-array to sort.
	 */
	public void shuttlesort(int from[], int to[], int low, int high)
	{

		if (high - low < 2)
		{
			return;
		}

		int middle = (low + high) / 2;
		shuttlesort(to, from, low, middle);
		shuttlesort(to, from, middle, high);

		int p = low;
		int q = middle;

		/* This is an optional short-cut; at each recursive call, check to see if the
		 * elements in this subset are already ordered. If so, no further comparisons
		 * are needed; the sub-array can just be copied. The array must be copied rather
		 * than assigned otherwise sister calls in the recursion might get out of sinc.
		 * When the number of elements is three they are partitioned so that the first
		 * set, [low, mid), has one element and and the second, [mid, high), has two. We
		 * skip the optimisation when the number of elements is three or less as the
		 * first compare in the normal merge will produce the same sequence of steps.
		 * This optimisation seems to be worthwhile for partially ordered lists but some
		 * analysis is needed to find out how the performance drops to Nlog(N) as the
		 * initial order diminishes - it may drop very quickly. */
		if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0)
		{

			for (int i = low; i < high; i++)
			{
				to[i] = from[i];
			}

			return;
		}

		// A normal merge.

		for (int i = low; i < high; i++)
		{

			if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
			{
				to[i] = from[p++];
			}
			else
			{
				to[i] = from[q++];
			}

		}

	}


	/**
	 * Performs the sort operation on the current model's data.
	 * It uses the {@link #shuttlesort} algorithm on the {@code indexes} array.
	 * The {@code sender} parameter is not used in the current implementation.
	 *
	 * @param  sender  The object that initiated the sort (not currently used).
	 */
	public void sort(Object sender)
	{
		checkModel(); // Potentially validates or updates model state

		this.compares = 0;
		// n2sort(); // Alternative N^2 sort
		// qsort(0, indexes.length-1); // Placeholder for qsort
		shuttlesort(this.indexes.clone(), this.indexes, 0, this.indexes.length);
		// SystemLog.ProblemPrintln("Compares: "+compares);
	}


	/**
	 * Sorts the table by a single specified column in ascending order.
	 *
	 * @param  column  The index of the column to sort by.
	 */
	public void sortByColumn(int column)
	{
		sortByColumn(column, true);
	}


	/**
	 * Sorts the table by a single specified column in the given order.
	 * Clears any previous sorting columns and sets the new one.
	 * After sorting, it fires a {@link TableModelEvent} to notify the JTable to refresh.
	 *
	 * @param  column     The index of the column to sort by.
	 * @param  ascending1  True for ascending order, false for descending order.
	 */
	public void sortByColumn(int column, boolean ascending1)
	{
		this.ascending = ascending1;
		this.sortingColumns.removeAllElements();
		this.sortingColumns.addElement(column);
		sort(this); // 'this' is passed as sender
		super.tableChanged(new TableModelEvent(this)); // Notify listeners (e.g., JTable)
	}


	/**
	 * Swaps two elements in the internal {@code indexes} array.
	 * This is a helper method used by sorting algorithms.
	 *
	 * @param  i  The index of the first element to swap.
	 * @param  j  The index of the second element to swap.
	 */
	public void swap(int i, int j)
	{
		int tmp = this.indexes[i];
		this.indexes[i] = this.indexes[j];
		this.indexes[j] = tmp;
	}


	/**
	 * {@inheritDoc}
	 * Called when the underlying table model has changed.
	 * This implementation first calls {@link #reallocateIndexes()} to reset the sort
	 * to an identity mapping, and then forwards the event to its own listeners.
	 * @param  e  The {@link TableModelEvent} from the underlying model.
	 */
	@Override public void tableChanged(TableModelEvent e)
	{
		reallocateIndexes();
		super.tableChanged(e);
	}

}
