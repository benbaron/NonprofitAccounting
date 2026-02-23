
package nonprofitbookkeeping.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleUITest.
 */
@ExtendWith(ApplicationExtension.class) public class SimpleUITest
{
	
	/** The button. */
	private Button button;
	
	/**
	 * Will be called with {@code @BeforeEach} semantics, i. e. before each test method.
	 *
	 * @param stage - Will be injected by the test runner.
	 */
	@Start private void start(Stage stage)
	{
		this.button = new Button("click me!");
		this.button.setId("myButton");
		this.button.setOnAction(actionEvent -> this.button.setText("clicked!"));
		stage.setScene(new Scene(new StackPane(this.button), 100, 100));
		stage.show();
	}
	
	/**
	 * Should contain button with text.
	 *
	 * @param robot - Will be injected by the test runner.
	 */
	@Test
		void should_contain_button_with_text(FxRobot robot)
	{
		Assertions.assertThat(this.button).hasText("click me!");
		// or (lookup by css id):
		Assertions.assertThat(robot.lookup("#myButton").queryAs(Button.class)).hasText("click me!");
		// or (lookup by css class):
		Assertions.assertThat(robot.lookup(".button").queryAs(Button.class)).hasText("click me!");
	}
	
	/**
	 * When button is clicked text changes.
	 *
	 * @param robot the robot
	 */
	@Test
		void when_button_is_clicked_text_changes(FxRobot robot)
	{
		// when:
		robot.clickOn(".button");
		
		// then:
		Assertions.assertThat(this.button).hasText("clicked!");
		// or (lookup by css id):
		Assertions.assertThat(robot.lookup("#myButton").queryAs(Button.class)).hasText("clicked!");
		// or (lookup by css class):
		Assertions.assertThat(robot.lookup(".button").queryAs(Button.class)).hasText("clicked!");
	}
	
}
