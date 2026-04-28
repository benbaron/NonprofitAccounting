package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Labeled;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.ui.JavaFXTestBase;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

public class SettingsPanelFXTest extends JavaFXTestBase
{
        private SettingsPanelFX panel;
        private TabPane tabPane;
        private SettingsService service;
        private SettingsModel model;

        @Start
        @Override public void start(Stage stage) throws Exception
        {
                this.service = new SettingsService();
                this.model = this.service.getSettings();
                seedSettingsModel();

                this.panel = new SettingsPanelFX(stage, this.service);
                Scene scene = new Scene(this.panel, 800, 600);
                stage.setScene(scene);
                stage.show();

                this.tabPane = lookup(".tab-pane").queryAs(TabPane.class);
        }

        private void seedSettingsModel()
        {
                this.model.setOrganizationName("Helping Hands");
                this.model.setFiscalYearStart("03-01");
                this.model.setDefaultCurrency("EUR");
                this.model.setDefaultIncomeAccount("Donations");
                this.model.setDefaultExpenseAccount("Supplies");
                this.model.setDonationEditPostingPolicy("REVERSE_AND_REPOST");
                this.model.setAutosaveEnabled(false);
                this.model.setAutosaveIntervalMinutes(15);
                this.model.setDefaultCompanyDirectory("/tmp/nonprofit");
                this.model.setLastUsedCompanyFile("/tmp/nonprofit/company.npbk");
                this.model.setDefaultReportPeriod(ReportPeriodPreset.LAST_MONTH.name());
                this.model.setEnableYearToDateOption(true);
                this.model.setEnableFullYearOption(false);
                this.model.setEnableLastMonthOption(true);
                this.model.setTheme("Dark");
                this.model.setLanguage("es-ES");
                this.model.setCurrencyFormat("¤#,##0.00");
        }

        private void selectTab(String tabName)
        {
                Optional<Tab> tabOptional = this.tabPane.getTabs().stream()
                        .filter(t -> tabName.equals(t.getText()))
                        .findFirst();
                assertTrue(tabOptional.isPresent(), "Tab '" + tabName + "' not found.");
                Platform.runLater(() -> this.tabPane.getSelectionModel().select(tabOptional.get()));
                WaitForAsyncUtils.waitForFxEvents();
        }

        @Test public void testCompanyInfoTab_displaysSeededValues()
        {
                selectTab("Company Info");

                verifyThat(lookupTextFieldByLabel("Organization Name:"), hasTextInField("Helping Hands"));
                verifyThat(lookupTextFieldByLabel("Fiscal Year Start:"), hasTextInField("03-01"));
                verifyThat(lookupComboBoxByLabel("Default Currency:"), hasComboBoxValue("EUR"));

                lookupTextFieldByLabel("Organization Name:").setText("River Valley");
                verifyThat(lookupTextFieldByLabel("Organization Name:"), hasTextInField("River Valley"));

                Platform.runLater(() -> castComboBox(lookupComboBoxByLabel("Default Currency:")).setValue("USD"));
                WaitForAsyncUtils.waitForFxEvents();
                verifyThat(lookupComboBoxByLabel("Default Currency:"), hasComboBoxValue("USD"));
        }

        @Test public void testAccountingTab_allowsSelectingDefaultAccounts()
        {
                selectTab("Accounting");

                ComboBox<String> incomeBox = castComboBox(lookupComboBoxByLabel("Default Income Account:"));
                ComboBox<String> expenseBox = castComboBox(lookupComboBoxByLabel("Default Expense Account:"));
                ComboBox<String> donationPolicyBox = castComboBox(lookupComboBoxByLabel("Donation Edit Posting:"));

                verifyThat(incomeBox, hasComboBoxValue("Donations"));
                verifyThat(expenseBox, hasComboBoxValue("Supplies"));
                verifyThat(donationPolicyBox, hasComboBoxValue("REVERSE_AND_REPOST"));

                Platform.runLater(() -> {
                        incomeBox.getItems().setAll("Donations", "Memberships");
                        expenseBox.getItems().setAll("Supplies", "Utilities");
                        incomeBox.setValue("Memberships");
                        expenseBox.setValue("Utilities");
                        donationPolicyBox.setValue("UPDATE_IN_PLACE");
                });
                WaitForAsyncUtils.waitForFxEvents();

                verifyThat(incomeBox, hasComboBoxValue("Memberships"));
                verifyThat(expenseBox, hasComboBoxValue("Utilities"));
                verifyThat(donationPolicyBox, hasComboBoxValue("UPDATE_IN_PLACE"));
        }

        @Test public void testApplicationTab_reflectsAutosaveAndReportPreferences()
        {
                selectTab("Application");

                HBox autosaveControls = lookupControlByLabel("Autosave:", HBox.class);
                Optional<CheckBox> autosaveCheck = autosaveControls.getChildren().stream()
                        .filter(node -> node instanceof CheckBox)
                        .map(node -> (CheckBox) node)
                        .findFirst();
                @SuppressWarnings("unchecked") Optional<Spinner<Integer>> autosaveSpinner = autosaveControls.getChildren().stream()
                        .filter(node -> node instanceof Spinner)
                        .map(node -> (Spinner<Integer>) node)
                        .findFirst();

                assertTrue(autosaveCheck.isPresent());
                assertTrue(autosaveSpinner.isPresent());
                assertFalse(autosaveCheck.get().isSelected());
                assertEquals(15, autosaveSpinner.get().getValue());

                HBox directoryBox = lookupControlByLabel("Default Directory:", HBox.class);
                TextField directoryField = (TextField) directoryBox.getChildren().stream()
                        .filter(node -> node instanceof TextField)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Default directory field not found"));
                assertEquals("/tmp/nonprofit", directoryField.getText());

                HBox fileBox = lookupControlByLabel("Last Used File:", HBox.class);
                TextField fileField = (TextField) fileBox.getChildren().stream()
                        .filter(node -> node instanceof TextField)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Last used file field not found"));
                assertEquals("/tmp/nonprofit/company.npbk", fileField.getText());

                ComboBox<ReportPeriodPreset> presetCombo = castComboBox(
                        lookupComboBoxByLabel("Default Report Period:"));
                verifyThat(presetCombo, hasComboBoxValue(ReportPeriodPreset.LAST_MONTH));

                VBox reportOptions = lookupControlByLabel("Enable Report Filters:", VBox.class);
                CheckBox ytd = (CheckBox) reportOptions.getChildren().get(0);
                CheckBox fullYear = (CheckBox) reportOptions.getChildren().get(1);
                CheckBox lastMonth = (CheckBox) reportOptions.getChildren().get(2);

                assertTrue(ytd.isSelected());
                assertFalse(fullYear.isSelected());
                assertTrue(lastMonth.isSelected());

                Platform.runLater(() -> {
                        autosaveCheck.get().setSelected(true);
                        autosaveSpinner.get().getValueFactory().setValue(10);
                        fullYear.setSelected(true);
                        lastMonth.setSelected(false);
                });
                WaitForAsyncUtils.waitForFxEvents();

                assertTrue(autosaveCheck.get().isSelected());
                assertEquals(10, autosaveSpinner.get().getValue());
                assertTrue(fullYear.isSelected());
                assertFalse(lastMonth.isSelected());
        }

        @Test public void testBackupTab_buttonsInvokeFileChoosersWithoutAlerts()
        {
                selectTab("Backup");

                clickOn("Create Backup");
                WaitForAsyncUtils.waitForFxEvents();
                DialogPane backupAlert = getTopModalDialogPane();
                assertNull(backupAlert, "Backup export should rely on a file chooser without showing alerts when cancelled.");

                clickOn("Restore Backup");
                WaitForAsyncUtils.waitForFxEvents();
                DialogPane restoreAlert = getTopModalDialogPane();
                assertNull(restoreAlert,
                        "Restore import should rely on a file chooser without showing alerts when cancelled.");
        }

        @Test public void testUiPreferencesTab_reflectsThemeLanguageAndCurrencyFormat()
        {
                selectTab("UI Preferences");

                ComboBox<String> themeCombo = castComboBox(lookupComboBoxByLabel("Theme:"));
                verifyThat(themeCombo, hasComboBoxValue("Dark"));

                ComboBox<Locale> languageCombo = castComboBox(lookupComboBoxByLabel("Language:"));
                verifyThat(languageCombo, hasComboBoxValue(Locale.forLanguageTag("es-ES")));

                verifyThat(lookupTextFieldByLabel("Currency Format:"), hasTextInField("¤#,##0.00"));

                Platform.runLater(() -> {
                        themeCombo.setValue("Light");
                        languageCombo.setValue(Locale.forLanguageTag("fr-FR"));
                });
                WaitForAsyncUtils.waitForFxEvents();

                verifyThat(themeCombo, hasComboBoxValue("Light"));
                verifyThat(languageCombo, hasComboBoxValue(Locale.forLanguageTag("fr-FR")));
        }

        private Matcher<TextField> hasTextInField(String expectedText)
        {
                return new TypeSafeMatcher<>()
                {
                        @Override public void describeTo(Description description)
                        {
                                description.appendText("TextField with text ")
                                        .appendValue(expectedText);
                        }

                        @Override protected boolean matchesSafely(TextField textField)
                        {
                                String text = textField == null ? null : textField.getText();
                                return Objects.equals(expectedText, text);
                        }

                        @Override protected void describeMismatchSafely(TextField textField,
                                Description mismatchDescription)
                        {
                                mismatchDescription.appendText("was ")
                                        .appendValue(textField == null ? null : textField.getText());
                        }
                };
        }

        private Matcher<ComboBox<?>> hasComboBoxValue(Object expectedValue)
        {
                return new TypeSafeMatcher<>()
                {
                        @Override public void describeTo(Description description)
                        {
                                description.appendText("ComboBox with value ")
                                        .appendValue(expectedValue);
                        }

                        @Override protected boolean matchesSafely(ComboBox<?> comboBox)
                        {
                                Object value = comboBox == null ? null : comboBox.getValue();
                                return Objects.equals(expectedValue, value);
                        }

                        @Override protected void describeMismatchSafely(ComboBox<?> comboBox,
                                Description mismatchDescription)
                        {
                                Object value = comboBox == null ? null : comboBox.getValue();
                                mismatchDescription.appendText("was ").appendValue(value);
                        }
                };
        }

        private <T extends Node> T lookupControlByLabel(String labelText, Class<T> type)
        {
                Labeled label = lookup(labelText).queryLabeled();
                Node parent = label.getParent();

                if (parent instanceof GridPane)
                {
                        GridPane grid = (GridPane) parent;
                        int labelRowIndex = Optional.ofNullable(GridPane.getRowIndex(label)).orElse(0);
                        int labelColIndex = Optional.ofNullable(GridPane.getColumnIndex(label)).orElse(0);
                        Optional<Node> match = grid.getChildren().stream()
                                .filter(node -> type.isInstance(node))
                                .filter(node -> Optional.ofNullable(GridPane.getRowIndex(node)).orElse(0) == labelRowIndex)
                                .filter(node -> Optional.ofNullable(GridPane.getColumnIndex(node)).orElse(0) == labelColIndex + 1)
                                .findFirst();
                        assertTrue(match.isPresent(), type.getSimpleName() + " next to label '" + labelText + "' not found.");
                        return type.cast(match.get());
                }

                fail("Label '" + labelText + "' is not in a GridPane or expected control not found next to it.");
                return null;
        }

        private TextField lookupTextFieldByLabel(String labelText)
        {
                return lookupControlByLabel(labelText, TextField.class);
        }

        private ComboBox<?> lookupComboBoxByLabel(String labelText)
        {
                return lookupControlByLabel(labelText, ComboBox.class);
        }

        private DialogPane getTopModalDialogPane()
        {
                Optional<Node> dialogPaneOpt = lookup((Node n) -> n instanceof DialogPane &&
                        n.getScene() != null && n.getScene().getWindow() instanceof Stage &&
                        ((Stage) n.getScene().getWindow()).isShowing()).tryQuery();
                return (DialogPane) dialogPaneOpt.orElse(null);
        }

        @SuppressWarnings("unchecked") private static <T> ComboBox<T> castComboBox(ComboBox<?> comboBox)
        {
                return (ComboBox<T>) comboBox;
        }
}
