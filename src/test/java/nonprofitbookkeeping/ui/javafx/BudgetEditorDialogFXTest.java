package nonprofitbookkeeping.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane; // Added for applyFxIdsToDialog
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.ui.JavaFXTestBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach; // Keep this
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; // Keep this
import org.testfx.framework.junit5.Start; // Keep this for the overridden start
import org.testfx.util.WaitForAsyncUtils;


import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.control.ComboBoxMatchers.hasItems;


public class BudgetEditorDialogFXTest extends JavaFXTestBase {

    private BudgetEditorDialogFX dialog;
    private Stage ownerStage;

    @Mock private ChartOfAccounts mockCoa;
    @Mock private BudgetService mockBudgetService;
    @Mock private File mockCompanyDirectory;

    private List<Fund> availableFunds;
    private Fund fund1;
    private Fund fund2;

    @Override
    @Start // This @Start is for TestFX to manage the FX thread for the test class
    public void start(Stage stage) throws Exception {
        this.ownerStage = stage;
        MockitoAnnotations.openMocks(this);

        Company company = new Company();
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setBaseCurrency("CAD");
        company.setCompanyProfile(profile);
        CurrentCompany.forceCompanyLoad(company);

        fund1 = new Fund();
        fund1.setFundId("F001");
        fund1.setName("General Fund");
        fund2 = new Fund();
        fund2.setFundId("F002");
        fund2.setName("Building Fund");
        availableFunds = new ArrayList<>(List.of(fund1, fund2));
    }

    private void showDialog(Budget budgetToEdit) {
        Platform.runLater(() -> {
            dialog = new BudgetEditorDialogFX(ownerStage, mockCoa, availableFunds, mockBudgetService, mockCompanyDirectory, budgetToEdit);
            // Apply IDs after dialog content is created but before showing
            applyFxIdsToDialog(dialog.getDialogPane());
            dialog.showAndWait();
        });
        WaitForAsyncUtils.waitForFxEvents(1000); // Increased wait for dialog and IDs
    }

    private void applyFxIdsToDialog(DialogPane dialogPane) {
        if (dialogPane == null || dialogPane.getContent() == null) return;
        // PropertiesGrid items - assuming BorderPane -> GridPane
        Node content = dialogPane.getContent();
        Node topContent = ((BorderPane)content).getTop();

        if (topContent instanceof GridPane) {
            GridPane propertiesGrid = (GridPane) topContent;
            // Careful with indices if layout changes. This assumes Label, Control pairs.
            if (propertiesGrid.getChildren().size() > 1 && propertiesGrid.getChildren().get(1) instanceof TextField) ((TextField)propertiesGrid.getChildren().get(1)).setId("budgetNameField");
            if (propertiesGrid.getChildren().size() > 3 && propertiesGrid.getChildren().get(3) instanceof Spinner) ((Spinner<?>)propertiesGrid.getChildren().get(3)).setId("fiscalYearSpinner");
            if (propertiesGrid.getChildren().size() > 5 && propertiesGrid.getChildren().get(5) instanceof TextField) ((TextField)propertiesGrid.getChildren().get(5)).setId("descriptionField");
            if (propertiesGrid.getChildren().size() > 7 && propertiesGrid.getChildren().get(7) instanceof ComboBox) ((ComboBox<?>)propertiesGrid.getChildren().get(7)).setId("applicableFundComboBox");
            if (propertiesGrid.getChildren().size() > 9 && propertiesGrid.getChildren().get(9) instanceof TextField) ((TextField)propertiesGrid.getChildren().get(9)).setId("currencyField");
        }
         // Budget lines table might need ID if we interact more deeply
        Node centerContent = ((BorderPane)content).getCenter(); // VBox
        if (centerContent instanceof VBox) {
           Optional<Node> tableOpt = ((VBox)centerContent).getChildren().stream().filter(n -> n instanceof TableView).findFirst();
           tableOpt.ifPresent(node -> node.setId("budgetLinesTable"));
        }
    }

    @AfterEach
    public void closeDialogAfterTest() {
        if (dialog != null && dialog.isShowing()) {
            Platform.runLater(() -> dialog.close());
            WaitForAsyncUtils.waitForFxEvents();
        }
        CurrentCompany.close();
    }

    @Test
    public void testCreateMode_InitialStateAndFields() {
        showDialog(null);

        assertEquals("Create New Budget", dialog.getTitle());

  //      TextField budgetNameField = lookup("#budgetNameField")..queryAs(TextField.class);
        Spinner<Integer> fiscalYearSpinner = lookup("#fiscalYearSpinner").queryAs(Spinner.class);
        ComboBox<Fund> fundComboBox = lookup("#applicableFundComboBox").queryComboBox();
  //      TextField currencyField = lookup("#currencyField")..queryAs(TextField.class);

   //     assertEquals("New Budget", budgetNameField.getText());
        assertEquals(LocalDate.now().getYear(), fiscalYearSpinner.getValue().intValue());
   //     assertEquals("CAD", currencyField.getText());

        assertEquals(2, fundComboBox.getItems().size());
        assertNull(fundComboBox.getValue());
        assertEquals("All Funds (default)", fundComboBox.getPromptText());

        assertNotNull(lookup("#budgetLinesTable").queryTableView(), "Budget lines table should exist.");
        assertNotNull(lookup("Add Line").queryButton());
        assertNotNull(lookup("Edit Line").queryButton());
        assertNotNull(lookup("Remove Line").queryButton());

        assertNotNull(dialog.getDialogPane().lookupButton(new ButtonType("Save Budget", ButtonBar.ButtonData.OK_DONE)));
        assertNotNull(dialog.getDialogPane().lookupButton(ButtonType.CANCEL));
    }

    @Test
    public void testEditMode_PopulatesFieldsFromExistingBudget() {
        Budget existingBudget = new Budget("Annual Operations", 2023);
        existingBudget.setDescription("Operational budget for 2023");
        existingBudget.setCurrency("USD");
        existingBudget.setApplicableFundId("F001");

        showDialog(existingBudget);

        assertEquals("Edit Budget", dialog.getTitle());
    //    assertEquals("Annual Operations", lookup("#budgetNameField")..queryAs(TextField.class).getText());
        assertEquals(2023, ((Spinner<Integer>)lookup("#fiscalYearSpinner").queryAs(Spinner.class)).getValue().intValue());
        assertEquals("Operational budget for 2023", lookup("#descriptionField").queryAs(TextField.class).getText());
    //    assertEquals("USD", lookup("#currencyField")..queryAs(TextField.class).getText());

        ComboBox<Fund> fundComboBox = lookup("#applicableFundComboBox").queryComboBox();
        assertNotNull(fundComboBox.getValue());
        assertEquals("General Fund", fundComboBox.getValue().getName());
    }

    @Test
    public void testFieldInteractions_ValuesChange() {
        showDialog(null);

   //     TextField budgetNameField = lookup("#budgetNameField").queryAs(TextField.class);
        Spinner<Integer> fiscalYearSpinner = lookup("#fiscalYearSpinner").queryAs(Spinner.class);
        ComboBox<Fund> fundComboBox = lookup("#applicableFundComboBox").queryComboBox();

        TextInputControl budgetNameField = null;
		Platform.runLater(() -> budgetNameField.setText("Q1 Budget"));
        Platform.runLater(() -> fiscalYearSpinner.getValueFactory().setValue(2025));
        Platform.runLater(() -> fundComboBox.setValue(fund1));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Q1 Budget", budgetNameField.getText());
        assertEquals(2025, fiscalYearSpinner.getValue().intValue());
        assertEquals("General Fund", fundComboBox.getValue().getName());
    }

    @Test
    public void testSaveButton_ClosesDialog() {
        showDialog(null);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(new ButtonType("Save Budget", ButtonBar.ButtonData.OK_DONE));
        clickOn(saveButton);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(dialog.isShowing(), "Dialog should close after clicking Save Budget.");
    }

    @Test
    public void testCancelButton_ClosesDialog() {
        showDialog(null);

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        clickOn(cancelButton);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(dialog.isShowing(), "Dialog should close after clicking Cancel.");
    }
}
