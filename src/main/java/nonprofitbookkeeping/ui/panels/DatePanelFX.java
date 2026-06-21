
package nonprofitbookkeeping.ui.panels;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;

/**
 * Lightweight JavaFX replacement for the old Swing {@code JDatePanelImpl} that
 * was based on JDatePicker.org.  Wraps a single {@link DatePicker} and exposes
 * a {@link #valueProperty()} so legacy calling code can bind / listen just like
 * before (where it used the model).
 */
public class DatePanelFX extends BorderPane
{
	
	/** The underlying JavaFX {@link DatePicker} component used for date selection. */
	private final DatePicker picker = new DatePicker(LocalDate.now());
	/** The exposed {@link ObjectProperty} that holds the selected date. This allows external binding and listening. */
	private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(LocalDate.now());
	
	/**
	 * Constructs a new {@code DatePanelFX}.
	 * Initializes the panel with a {@link DatePicker} centered within it.
	 * The current date is set as the initial value for the picker and the exposed {@link #value} property.
	 * A listener is set up to synchronize the {@link #value} property with changes in the DatePicker.
	 */
	public DatePanelFX()
	{
		setPadding(PanelChrome.PANEL_PADDING);
		setCenter(this.picker);
		this.picker.valueProperty().addListener((obs, o, n) -> this.value.set(n));
	}
	
	/**
	 * Returns the {@link ObjectProperty} representing the selected date in this panel.
	 * This property can be used for binding with other JavaFX properties or for adding change listeners.
	 *
	 * @return The {@link ObjectProperty} for the selected {@link LocalDate}.
	 */
	public ObjectProperty<LocalDate> valueProperty()
	{
		return this.value;
	}
	
	/**
	 * Convenience getter for the currently selected date.
	 *
	 * @return The selected {@link LocalDate}, or null if no date is selected (though DatePicker usually forces a value).
	 */
	public LocalDate getDate()
	{
		return this.value.get();
	}
	
	/**
	 * Convenience setter for the selected date.
	 * Updates both the internal {@link DatePicker} and the exposed {@link #value} property.
	 *
	 * @param d The {@link LocalDate} to set as the selected date. Can be null to clear the selection.
	 */
	public void setDate(LocalDate d)
	{
		this.picker.setValue(d);
	}
	
}
