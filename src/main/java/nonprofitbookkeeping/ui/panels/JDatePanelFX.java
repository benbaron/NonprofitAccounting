
package nonprofitbookkeeping.ui.panels;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;

/**
 * Lightweight JavaFX replacement for the old Swing {@code JDatePanelImpl} that
 * was based on JDatePicker.org.  Wraps a single {@link DatePicker} and exposes
 * a {@link #valueProperty()} so legacy calling code can bind / listen just like
 * before (where it used the model).
 */
public class JDatePanelFX extends BorderPane
{
	
	private final DatePicker picker = new DatePicker(LocalDate.now());
	private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(LocalDate.now());
	
	public JDatePanelFX()
	{
		setPadding(new Insets(4));
		setCenter(this.picker);
		this.picker.valueProperty().addListener((obs, o, n) -> this.value.set(n));
	}
	
	/**
	 * Returns the selected date property for binding.
	 */
	public ObjectProperty<LocalDate> valueProperty()
	{
		return this.value;
	}
	
	/** Convenience getter. */
	public LocalDate getDate()
	{
		return this.value.get();
	}
	
	/** Convenience setter. */
	public void setDate(LocalDate d)
	{
		this.picker.setValue(d);
	}
	
}
