
package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.Semaphore;


public abstract class JavaFXTestBase extends ApplicationTest
{
	
	private Stage stage;
	
	@Override public void start(Stage stage1) throws Exception
	{
		this.stage = stage1;
		// Default scene, can be overridden by subclasses by setting a new root
		Parent emptyRoot = new Parent()
		{
		}; // An empty parent node
		Scene scene = new Scene(emptyRoot, 800, 600);
		stage1.setScene(scene);
		stage1.show();
	}
	
	@BeforeEach public void setupToolkit() throws Exception
	{
		// Set up the toolkit if not already initialized
		// Using SHOW mode to make tests visible, which can be helpful for debugging
		// For headless environments, this might need to be configured differently
		System.setProperty("testfx.toolkit", "glass");
		System.setProperty("testfx.showMode", "SHOW");
		System.setProperty("testfx.robot", "glass");
		System.setProperty("prism.order", "sw"); // Use software rendering pipeline
		System.setProperty("prism.text", "t2k"); // Use T2K font rendering
		System.setProperty("java.awt.headless", "false"); // Ensure AWT is not in headless mode for
															// JavaFX
		
		// This is a workaround for potential issues with toolkit initialization in some
		// environments
		// It ensures that the TestFX toolkit is ready before tests run.
		// ToolkitSetup.registerToolkit();
		// The above line might be needed if ApplicationTest doesn't handle it
		// sufficiently
	}
	
	protected void setSceneRoot(Parent rootNode)
	{
		Platform.runLater(() -> {
			Scene scene = new Scene(rootNode);
			this.stage.setScene(scene);
		});
		// Wait for the UI thread to process the change
		waitForRunLater();
	}
	
	protected Stage getStage()
	{
		return this.stage;
	}
	
	/**
	 * Helper method to wait for Platform.runLater() to complete.
	 * This is crucial for ensuring UI updates are processed before test assertions.
	 */
	protected void waitForRunLater()
	{
		Semaphore semaphore = new Semaphore(0);
		Platform.runLater(semaphore::release);
		
		try
		{
			semaphore.acquire();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt(); // Preserve interrupt status
			throw new RuntimeException("Interrupted while waiting for Platform.runLater()", e);
		}
		
	}
	
	@AfterEach public void cleanupAfterTest() throws Exception
	{
		
		// Hide the stage after each test
		if (this.stage != null)
		{
			Platform.runLater(() -> this.stage.hide());
			waitForRunLater(); // Ensure hide operation completes
		}
		
	}
	
}
