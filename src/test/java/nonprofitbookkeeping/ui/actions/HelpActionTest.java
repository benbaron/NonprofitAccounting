package nonprofitbookkeeping.ui.actions;

import javafx.stage.Stage;
import javafx.event.ActionEvent; // Though we pass null for ActionEvent

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HelpActionTest {

    @Mock
    private Stage mockStage; // Owner stage for dialogs

    @Test
    @DisplayName("Constructor: Valid Stage should create instance successfully")
    void testConstructor_validStage_succeeds() {
        HelpAction action = new HelpAction(this.mockStage);
        assertNotNull(action, "Instance should be created with a valid Stage.");
    }

    @Test
    @DisplayName("Constructor: Null Stage should throw IllegalArgumentException")
    void testConstructor_nullStage_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HelpAction(null);
        });
        assertEquals("Owner stage cannot be null.", exception.getMessage(),
                     "Exception message for null stage is not as expected.");
    }

    @Test
    @DisplayName("handle: Basic call should attempt to show UI (Alert for missing resource)")
    void testHandle_basicCall_showsAlertWhenResourceMissing() {
        HelpAction action = new HelpAction(this.mockStage);

        // In a standard unit test environment, getResource("/help/help.html") will likely return null
        // because the resources might not be on the test classpath or accessible in the same way.
        // The HelpAction.handle() method is designed to show an Alert if the resource is null.
        // This test verifies that calling handle() doesn't throw an NPE in this common scenario
        // and proceeds to the point where it would try to show an Alert.
        // Full UI testing of the Alert display would require TestFX.

        assertDoesNotThrow(() -> action.handle(null), // Pass null for ActionEvent as it's not used
            "Calling handle(null) should not throw unexpected exceptions, even if help resource is missing.");

        // We can't easily verify the Alert is shown without TestFX, but we know the code path
        // for a null resource URL leads to creating and trying to show an Alert.
        // The main point here is no NPE due to missing resource.
    }
}
