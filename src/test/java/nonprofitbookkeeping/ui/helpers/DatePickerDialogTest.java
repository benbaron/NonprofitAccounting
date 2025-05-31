package nonprofitbookkeeping.ui.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfHeadless; // To potentially skip in headless environments

import java.awt.HeadlessException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic sanity checks for {@link DatePickerDialog}.
 * These tests primarily ensure that dialog construction does not immediately
 * throw errors. They do not test UI interaction or actual date selection.
 * Tests involving Swing UI might behave differently or fail in headless environments.
 */
class DatePickerDialogTest {

    /**
     * Attempts to call showSingleDateDialog.
     * This is a basic smoke test to catch immediate issues during dialog setup.
     * It may throw HeadlessException in environments without a display.
     */
    @Test
    @DisplayName("showSingleDateDialog: Basic call sanity check")
    @DisabledIfHeadless // This annotation helps skip the test automatically in headless environments
    void testShowSingleDateDialog_basicCall_doesNotThrowUnexpectedError() {
        // We expect this might not "complete" in a unit test (as it waits for UI interaction)
        // or might throw HeadlessException if in a truly headless environment.
        // The main purpose here is a very basic sanity check of dialog construction code.
        // The dialog is modal, so it would block if it could show.
        // In a test, JOptionPane often returns immediately with a default value or throws HeadlessException.

        // For this test, we're primarily concerned with unexpected runtime errors during setup,
        // not the return value or dialog interaction.
        try {
            assertDoesNotThrow(() -> {
                // Pass null for parentComponent as it's often acceptable for default screen centering
                // or if no parent frame is critical for this basic test.
                DatePickerDialog.showSingleDateDialog(null, "Test Single Date", "Select Date:");
            }, "Calling showSingleDateDialog should not throw unexpected exceptions during its setup phase.");
        } catch (HeadlessException e) {
            System.out.println("Skipping DatePickerDialog test in headless environment: " + e.getMessage());
            // Test considered passed or inconclusive if HeadlessException is thrown in such an environment.
        }
    }

    /**
     * Attempts to call showDateRangeDialog.
     * This is a basic smoke test to catch immediate issues during dialog setup.
     * It may throw HeadlessException in environments without a display.
     */
    @Test
    @DisplayName("showDateRangeDialog: Basic call sanity check")
    @DisabledIfHeadless // This annotation helps skip the test automatically in headless environments
    void testShowDateRangeDialog_basicCall_doesNotThrowUnexpectedError() {
        // Similar to the single date dialog, this is a basic sanity check.
        try {
            assertDoesNotThrow(() -> {
                DatePickerDialog.showDateRangeDialog(null, "Test Date Range", "Start Date:", "End Date:");
            }, "Calling showDateRangeDialog should not throw unexpected exceptions during its setup phase.");
        } catch (HeadlessException e) {
            System.out.println("Skipping DatePickerDialog test in headless environment: " + e.getMessage());
            // Test considered passed or inconclusive if HeadlessException is thrown in such an environment.
        }
    }
}
