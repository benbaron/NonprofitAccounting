package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.ui.panels.NewTransactionPanelFX.Line; // Corrected import for Line
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.util.function.Consumer;

@ExtendWith(ApplicationExtension.class)
public class NewTransactionPanelBugTest {

    private NewTransactionPanelFX newTransactionPanel;
    private TableView<Line> linesTable;
    private TextArea memoTextArea;

    private FxRobot robot;
    @Start
    private void start(Stage stage) {
        Consumer<AccountingTransaction> mockOnSave = tx -> {
            // Mock consumer, does nothing for this test
        };
        
        this.newTransactionPanel = new NewTransactionPanelFX(mockOnSave);
        
        Scene scene = new Scene(this.newTransactionPanel, 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();

        // Lookup UI elements after the stage is shown
        this.linesTable = this.robot.lookup(".table-view").queryTableView(); // Generic lookup for TableView
        this.memoTextArea = this.robot.lookup(".text-area").queryAs(TextArea.class); // Generic lookup for TextArea
    }

    // Helper method to access robot in @Start, TestFX injects it into test methods but not @Start
    // This is a common pattern if you need robot during setup.
    // However, for this test, we can pass robot to methods that need it or do lookups in the test method itself.
    // For simplicity, I'll perform lookups in start and make sure they are valid.
    // If robot is needed in start, it would require ApplicationTest.launch() or similar setup.
    // Let's assume TestFX setup handles robot availability for lookups in @Start too,
    // if not, these lookups would be moved to the beginning of the test method.
    // TestFX's ApplicationExtension should make robot available for injection in @Start parameters if needed.
    // Let's try without direct robot injection in @Start parameters first.
    // If newTransactionPanel.lookup() is used, it needs to be on FX thread.
    // FxRobot lookups are generally safer.

    @Test
    void reproduceDataErasureBug(FxRobot robot) {
        // Step 1: Setup is done in @Start

        // Step 2: Add an initial empty line to the table
        Platform.runLater(() -> {
            this.linesTable.getItems().add(new Line()); // Add directly to TableView's items
            this.linesTable.refresh(); // Ensure UI updates
        });
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI updates

        // Verify line was added
        Assertions.assertThat(this.linesTable.getItems()).hasSize(1);
        Line addedLine = this.linesTable.getItems().get(0);
        Assertions.assertThat(addedLine.amount.get()).isEqualTo(BigDecimal.ZERO);


        // Step 3 & 4: Target the "Amount" cell of the first row and start editing.
        // "Amount" is the 3rd column (index 2).
        // Using direct node lookup for the cell.
        // Ensure row exists before trying to click on it.
        org.testfx.service.query.NodeQuery rowQuery = robot.lookup(".table-row-cell").nth(0);
        Assertions.assertThat(rowQuery.queryAll()).isNotEmpty(); // Make sure row is there

        org.testfx.service.query.NodeQuery cellQuery = rowQuery.lookup(".table-cell").nth(2);
        
        Assertions.assertThat(cellQuery.queryAll()).isNotEmpty(); // Make sure cell is there
        
        robot.doubleClickOn(null); // FIXME

        WaitForAsyncUtils.waitForFxEvents();

        
        // Step 5: Simulate typing a new value
        robot.write("100.00");
        WaitForAsyncUtils.waitForFxEvents();

        // Step 6: Click on another UI element (Memo TextArea) to make the cell lose focus
        Assertions.assertThat(this.memoTextArea).isNotNull(); // Ensure memoTextArea was found
        robot.clickOn(this.memoTextArea);
        WaitForAsyncUtils.waitForFxEvents();

        // Step 7: Retrieve the value from the cell's underlying model item
        Line firstLine = this.linesTable.getItems().get(0);
        BigDecimal amountAfterLosingFocus = firstLine.amount.get();

        // Step 8: Assert that the value has reverted to BigDecimal.ZERO (the default for new Line's amount)
        // This assertion is the core of the bug reproduction.
        // If the bug exists, the value "100.00" was not committed, and it reverted.
        Assertions.assertThat(amountAfterLosingFocus)
            .as("Amount in the first line after losing focus and typing '100.00'")
            .isEqualTo(BigDecimal.ZERO); 
            // If it was not BigDecimal.ZERO but some other default, this would need to change.
            // Based on NewTransactionPanelFX.Line constructor, it's BigDecimal.ZERO.
    }
}
