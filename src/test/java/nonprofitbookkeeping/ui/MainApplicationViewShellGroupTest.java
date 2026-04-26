package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainApplicationViewShellGroupTest extends JavaFXTestBase
{
	private MainApplicationView view;

	@Start
	public void start(Stage stage)
	{
		this.view = new MainApplicationView();
		this.view.showWorkspaceTabs();
		stage.setScene(new Scene(this.view, 1100, 700));
		stage.show();
	}

	@Test
	public void selectingReportingTab_highlightsReportingLegend() throws Exception
	{
		TabPane tabPane = (TabPane) getPrivateField("tabPane");
		Label reviewLabel = (Label) getPrivateField("reviewGroupLabel");
		Label workflowLabel = (Label) getPrivateField("workflowGroupLabel");
		Label reportingLabel = (Label) getPrivateField("reportingGroupLabel");

		Platform.runLater(() -> tabPane.getSelectionModel()
			.select(findTab(tabPane, "Reports")));
		WaitForAsyncUtils.waitForFxEvents();

		assertTrue(reportingLabel.getStyleClass().contains("shell-group-active"));
		assertFalse(reviewLabel.getStyleClass().contains("shell-group-active"));
		assertFalse(workflowLabel.getStyleClass().contains("shell-group-active"));
	}

	@Test
	public void unmappedTab_defaultsToWorkflowGroup() throws Exception
	{
		Label reviewLabel = (Label) getPrivateField("reviewGroupLabel");
		Label workflowLabel = (Label) getPrivateField("workflowGroupLabel");
		Label reportingLabel = (Label) getPrivateField("reportingGroupLabel");
		Method updateMethod = MainApplicationView.class
			.getDeclaredMethod("updateShellGroupHighlight", Tab.class);
		updateMethod.setAccessible(true);

		Platform.runLater(() -> {
			try
			{
				updateMethod.invoke(this.view, new Tab("Ad Hoc"));
			}
			catch (Exception ex)
			{
				throw new RuntimeException(ex);
			}
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertTrue(workflowLabel.getStyleClass().contains("shell-group-active"));
		assertFalse(reviewLabel.getStyleClass().contains("shell-group-active"));
		assertFalse(reportingLabel.getStyleClass().contains("shell-group-active"));
	}

	private Object getPrivateField(String name) throws Exception
	{
		Field field = MainApplicationView.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(this.view);
	}

	private static Tab findTab(TabPane tabPane, String text)
	{
		for (Tab tab : tabPane.getTabs())
		{
			if (text.equals(tab.getText()))
			{
				return tab;
			}
		}
		throw new IllegalArgumentException("Missing tab: " + text);
	}
}
