package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.CompanyManagementService;

class SharedCompanyManagementPanelFXTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void sharedWorkspaceUsesApprovedColumnsActionsAndStartupChoice()
        throws Exception
    {
        runOnFxThread(() -> {
            SharedCompanyManagementPanelFX panel =
                new SharedCompanyManagementPanelFX(
                    new CompanyManagementService(), new TestHost());

            VBox top = assertInstanceOf(VBox.class, panel.getTop());
            ChoiceBox<?> startup = find(top, ChoiceBox.class);
            assertEquals(3, startup.getItems().size());
            CheckBox archived = find(top, CheckBox.class);
            assertEquals("Show archived", archived.getText());

            SplitPane center = assertInstanceOf(SplitPane.class,
                panel.getCenter());
            TableView<?> table = assertInstanceOf(TableView.class,
                center.getItems().get(0));
            assertEquals(List.of("Company Name", "ID", "Last Updated",
                    "Last Opened", "Status"),
                table.getColumns().stream().map(column -> column.getText())
                    .toList());
            assertTrue(table.getOnKeyPressed() != null);
            assertTrue(table.getRowFactory() != null);

            VBox bottom = assertInstanceOf(VBox.class, panel.getBottom());
            HBox actions = assertInstanceOf(HBox.class,
                bottom.getChildren().get(0));
            List<String> labels = actions.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .map(Button::getText)
                .toList();
            assertTrue(labels.contains("Open"));
            assertTrue(labels.contains("Create Company…"));
            assertTrue(labels.contains("Edit Company…"));
            assertTrue(labels.contains("Archive / Restore"));
            assertTrue(labels.contains("Export Backup and Delete…"));
            assertTrue(labels.contains("Developer Tools…"));
            assertFalse(labels.stream().anyMatch(label ->
                label.contains("Demo") || label.contains("Sample")));
        });
    }

    @Test
    void companyWizardHasFiveStepsAndNoDemoSeedingControl()
        throws Exception
    {
        runOnFxThread(() -> {
            CompanyProfileWizardFX wizard = new CompanyProfileWizardFX(
                new Company(), profile -> { });
            VBox buttonArea = assertInstanceOf(VBox.class,
                new VBox(wizard.getBottom()));
            assertFalse(findAll(wizard, CheckBox.class).stream()
                .anyMatch(box -> box.getText() != null &&
                    (box.getText().contains("Demo") ||
                        box.getText().contains("Sample"))));
            assertTrue(findAll(wizard, Button.class).stream()
                .anyMatch(button -> "Save Company".equals(button.getText())));
        });
    }

    private static <T extends Node> T find(Node root, Class<T> type)
    {
        return findAll(root, type).stream().findFirst().orElseThrow();
    }

    private static <T extends Node> List<T> findAll(Node root, Class<T> type)
    {
        java.util.ArrayList<T> result = new java.util.ArrayList<>();
        collect(root, type, result);
        return result;
    }

    private static <T extends Node> void collect(Node node, Class<T> type,
        List<T> result)
    {
        if (type.isInstance(node))
        {
            result.add(type.cast(node));
        }
        if (node instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                collect(child, type, result);
            }
        }
    }

    private static void runOnFxThread(FxRunnable runnable) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];
        Platform.runLater(() -> {
            try
            {
                runnable.run();
            }
            catch (Throwable throwable)
            {
                error[0] = throwable;
            }
            finally
            {
                latch.countDown();
            }
        });
        if (!latch.await(20, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Timed out waiting for FX task");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }

    @FunctionalInterface
    private interface FxRunnable
    {
        void run() throws Exception;
    }

    private static final class TestHost
        implements SharedCompanyManagementPanelFX.Host
    {
        @Override public String activeDatabaseLabel() { return "No database"; }
        @Override public void switchDatabase(javafx.stage.Window owner) { }
        @Override public void openCompany(long id, String label) { }
        @Override public void closeActiveCompany() { }
        @Override public Long activeCompanyId() { return null; }
        @Override public void openDeveloperTools(javafx.stage.Window owner) { }
    }
}
