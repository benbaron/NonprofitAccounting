
package nonprofitbookkeeping.ui.actions;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) class HelpActionTest
{
	
	@Mock private Stage mockStage; // Owner stage for dialogs
	
	@Test
	@DisplayName("Constructor: Valid Stage should create instance successfully")
		void testConstructor_validStage_succeeds()
	{
		HelpAction action = new HelpAction(this.mockStage);
		assertNotNull(action, "Instance should be created with a valid Stage.");
	}
	
	@Test
	@DisplayName("Constructor: Null Stage should throw IllegalArgumentException")
		void testConstructor_nullStage_throwsIllegalArgumentException()
	{
                Exception exception = assertThrows(IllegalArgumentException.class, () -> new HelpAction(null));
		assertEquals("Owner stage cannot be null.", exception.getMessage(),
			"Exception message for null stage is not as expected.");
	}
	
        @Test
        @DisplayName("handle: Basic call should display help panel without exceptions")
                void testHandle_basicCall_doesNotThrow()
        {
                HelpAction action = new HelpAction(this.mockStage);

                assertDoesNotThrow(() -> action.handle(null),
                        "Calling handle(null) should not throw unexpected exceptions, even if help resources are unavailable.");
        }
	
}
