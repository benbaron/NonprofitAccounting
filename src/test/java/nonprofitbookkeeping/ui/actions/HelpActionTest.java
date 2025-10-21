
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
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> new HelpAction(null));
                assertEquals("Owner stage cannot be null.", exception.getMessage(),
                        "Exception message for null stage is not as expected.");
        }

        @Test
        @DisplayName("handle: Headless environments skip launching the help window")
                void testHandle_headlessEnvironmentIsGraceful()
        {
                HelpAction action = new TestableHelpAction(this.mockStage, true);

                assertDoesNotThrow(() -> action.handle(null),
                        "Calling handle(null) should not throw when running headless.");
        }

        private static final class TestableHelpAction extends HelpAction
        {
                private final boolean headless;

                private TestableHelpAction(Stage ownerStage, boolean headless)
                {
                        super(ownerStage);
                        this.headless = headless;
                }

                @Override protected boolean isHeadlessEnvironment()
                {
                        return this.headless;
                }
        }

}
