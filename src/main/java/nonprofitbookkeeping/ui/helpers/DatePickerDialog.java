package nonprofitbookkeeping.ui.helpers;

import javax.swing.*;

import org.jdatepicker.UtilDateModel;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;
import java.util.Properties;

public class DatePickerDialog {
	// FIXME - lots of stuff broken
	
    /**
     * Shows a dialog prompting the user to select a date range.
     *
     * @param parentComponent The parent component for the dialog.
     * @param title           The title of the dialog.
     * @param startDateLabel  The label for the start date picker.
     * @param endDateLabel    The label for the end date picker.
     * @return An Optional containing an array of two LocalDates (start and end).
     *         Returns Optional.empty() if the user cancels.
     *         The LocalDates in the array can be null if not selected in the picker.
     */
    public static Optional<LocalDate[]> showDateRangeDialog(Component parentComponent, String title, String startDateLabel, String endDateLabel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 2, 2, 2);

        // Start Date Picker
        panel.add(new JLabel(startDateLabel), gbc);
        gbc.gridx++;
        UtilDateModel startDateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl startDatePanel = new JDatePanelImpl(startDateModel, p);
        JDatePickerImpl startDatePicker = 
        	new JDatePickerImpl(startDatePanel, null);
        		//new JDatePickerImpl.JFormattedTextFieldFactory());
       // panel.add(startDatePicker, gbc);

        // End Date Picker
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel(endDateLabel), gbc);
        gbc.gridx++;
        UtilDateModel endDateModel = new UtilDateModel();
        JDatePanelImpl endDatePanel = new JDatePanelImpl(endDateModel, p); // Reuse properties
        JDatePickerImpl endDatePicker = new JDatePickerImpl(endDatePanel, null);
        	//new JDatePickerImpl.JFormattedTextFieldFactory());
       // panel.add(endDatePicker, gbc);

        int result = JOptionPane.showConfirmDialog(parentComponent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            LocalDate startDate = null;
            Calendar selectedStartCal = null;
            	//(Calendar) startDatePicker.getModel().getValue();
            if (selectedStartCal != null) {
                startDate = selectedStartCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            LocalDate endDate = null;
            Calendar selectedEndCal = null;
            //(Calendar) endDatePicker.getModel().getValue();
            if (selectedEndCal != null) {
                endDate = selectedEndCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            return Optional.of(new LocalDate[]{startDate, endDate});
        }
        return Optional.empty();
    }

    /**
     * Shows a dialog prompting the user to select a single date.
     *
     * @param parentComponent The parent component for the dialog.
     * @param title           The title of the dialog.
     * @param dateLabel       The label for the date picker.
     * @return An Optional containing the selected LocalDate.
     *         Returns Optional.empty() if the user cancels or if no date is selected.
     */
    public static Optional<LocalDate> showSingleDateDialog(Component parentComponent,
                                                           String title, String dateLabel) {
        JPanel panel = new JPanel(new FlowLayout()); // Simpler layout for one date

        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, null);
        	//new JDatePickerImpl.JFormattedTextFieldFactory());

        panel.add(new JLabel(dateLabel));
        //panel.add(datePicker);

        int result = JOptionPane.showConfirmDialog(parentComponent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Calendar selectedCal = null; //(Calendar) datePicker.getModel().getValue();
            if (selectedCal != null) {
                return Optional.of(selectedCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }
        return Optional.empty();
    }
}
