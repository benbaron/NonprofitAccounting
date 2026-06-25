package nonprofitbookkeeping.ui.panels;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import nonprofitbookkeeping.service.CompanyManagementService.ChartTemplate;
import nonprofitbookkeeping.service.CompanyManagementService.CompanyDefinition;

/** Shared multi-page wizard used to create and edit companies. */
public final class CompanySetupWizardFX
{
    private CompanySetupWizardFX()
    {
    }

    public static Optional<CompanyDefinition> show(
        CompanyDefinition existing, boolean editing)
    {
        CompanyDefinition initial = existing == null ? defaults() : existing;

        TextField name = new TextField(initial.companyName());
        TextField legal = new TextField(initial.legalStructure());
        TextField fiscal = new TextField(initial.fiscalYearStart());
        ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList(
            Currency.getAvailableCurrencies().stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .toList()));
        currency.setEditable(true);
        currency.setValue(initial.baseCurrency());
        DatePicker startingDate = new DatePicker(initial.startingBalanceDate());
        ComboBox<ChartTemplate> template = new ComboBox<>(
            FXCollections.observableArrayList(ChartTemplate.values()));
        template.setValue(initial.chartTemplate());
        template.setDisable(editing && initial.chartTemplate() != ChartTemplate.EMPTY);

        CheckBox fund = new CheckBox("Enable fund accounting");
        fund.setSelected(initial.enableFundAccounting());
        CheckBox inventory = new CheckBox("Enable inventory");
        inventory.setSelected(initial.enableInventory());
        CheckBox multiCurrency = new CheckBox("Enable multi-currency");
        multiCurrency.setSelected(initial.enableMultiCurrency());
        TextField defaultBank = new TextField(initial.defaultBankAccount());
        defaultBank.setPromptText("Account number, e.g. 1000");

        template.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!editing && newValue != ChartTemplate.EMPTY &&
                defaultBank.getText().isBlank())
            {
                defaultBank.setText("1000");
            }
            if (newValue == ChartTemplate.EMPTY)
            {
                defaultBank.clear();
            }
        });

        TabPane pages = new TabPane(
            tab("Identity", identityPage(name, legal)),
            tab("Fiscal & Currency", fiscalPage(fiscal, currency)),
            tab("Chart Template", chartPage(template, defaultBank)),
            tab("Features", featurePage(fund, inventory, multiCurrency)),
            tab("Starting Balances", startingPage(startingDate)));
        pages.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        TextArea review = new TextArea();
        review.setEditable(false);
        review.setWrapText(true);
        Tab reviewTab = tab("Review", review);
        reviewTab.setOnSelectionChanged(event -> {
            if (reviewTab.isSelected())
            {
                review.setText(reviewText(name, legal, fiscal, currency,
                    startingDate, template, fund, inventory, multiCurrency,
                    defaultBank));
            }
        });
        pages.getTabs().add(reviewTab);

        Dialog<CompanyDefinition> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit Company" : "Create Company");
        dialog.getDialogPane().setContent(pages);
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(760, 520);
        dialog.setResizable(true);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK)
            {
                return null;
            }
            return new CompanyDefinition(
                name.getText(),
                legal.getText(),
                fiscal.getText(),
                currency.getEditor().getText().isBlank() ?
                    currency.getValue() : currency.getEditor().getText(),
                startingDate.getValue(),
                template.getValue(),
                fund.isSelected(),
                inventory.isSelected(),
                multiCurrency.isSelected(),
                defaultBank.getText());
        });
        return dialog.showAndWait();
    }

    private static CompanyDefinition defaults()
    {
        return new CompanyDefinition("", "Non-Profit", "01-01", "USD",
            LocalDate.now(), ChartTemplate.SCA_BRANCH, true, false, false,
            "1000");
    }

    private static Tab tab(String title, javafx.scene.Node content)
    {
        return new Tab(title, content);
    }

    private static GridPane identityPage(TextField name, TextField legal)
    {
        return form(List.of(
            row("Company name", name),
            row("Legal structure", legal)));
    }

    private static GridPane fiscalPage(TextField fiscal,
        ComboBox<String> currency)
    {
        return form(List.of(
            row("Fiscal year starts (MM-DD)", fiscal),
            row("Base currency", currency)));
    }

    private static GridPane chartPage(ComboBox<ChartTemplate> template,
        TextField defaultBank)
    {
        return form(List.of(
            row("Chart of accounts template", template),
            row("Default bank account", defaultBank),
            row("Note", new Label(
                "The default bank account must exist in the selected chart."))));
    }

    private static GridPane featurePage(CheckBox fund, CheckBox inventory,
        CheckBox multiCurrency)
    {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addColumn(0, fund, inventory, multiCurrency);
        return grid;
    }

    private static GridPane startingPage(DatePicker startingDate)
    {
        return form(List.of(row("Starting balance date", startingDate)));
    }

    private static GridPane form(List<FormRow> rows)
    {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(12);
        grid.setVgap(12);
        int index = 0;
        for (FormRow row : rows)
        {
            grid.add(new Label(row.label()), 0, index);
            grid.add(row.node(), 1, index);
            index++;
        }
        return grid;
    }

    private static FormRow row(String label, javafx.scene.Node node)
    {
        return new FormRow(label, node);
    }

    private static String reviewText(TextField name, TextField legal,
        TextField fiscal, ComboBox<String> currency, DatePicker date,
        ComboBox<ChartTemplate> template, CheckBox fund,
        CheckBox inventory, CheckBox multiCurrency, TextField bank)
    {
        return "Company: " + name.getText() + "\n" +
            "Legal structure: " + legal.getText() + "\n" +
            "Fiscal year start: " + fiscal.getText() + "\n" +
            "Base currency: " + currency.getEditor().getText() + "\n" +
            "Starting balance date: " + date.getValue() + "\n" +
            "Chart template: " + template.getValue() + "\n" +
            "Default bank account: " + bank.getText() + "\n" +
            "Fund accounting: " + fund.isSelected() + "\n" +
            "Inventory: " + inventory.isSelected() + "\n" +
            "Multi-currency: " + multiCurrency.isSelected();
    }

    private record FormRow(String label, javafx.scene.Node node)
    {
    }
}
