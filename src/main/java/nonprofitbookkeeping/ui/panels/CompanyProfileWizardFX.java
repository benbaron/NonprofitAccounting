package nonprofitbookkeeping.ui.panels;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.service.CompanyManagementService;

/** Multi-page wizard shared by Create Company and Edit Company. */
public class CompanyProfileWizardFX extends BorderPane
{
    private final Node[] steps = new Node[5];
    private final Button back = new Button("Back");
    private final Button next = new Button("Next");
    private final Button finish = new Button("Save Company");
    private final Label validation = new Label();
    private final Consumer<CompanyProfileModel> onSave;

    private final TextField name = new TextField();
    private final ComboBox<String> legal = new ComboBox<>();
    private final TextField taxId = new TextField();
    private final TextField address = new TextField();
    private final TextField phone = new TextField();
    private final TextField email = new TextField();
    private final TextField fiscalStart = new TextField("01-01");
    private final ComboBox<String> currency = new ComboBox<>();
    private final DatePicker startingDate = new DatePicker(LocalDate.now());
    private final ComboBox<String> chartTemplate = new ComboBox<>();
    private final ComboBox<String> defaultBank = new ComboBox<>();
    private final CheckBox fundAccounting =
        new CheckBox("Enable Fund Accounting");
    private final CheckBox inventory =
        new CheckBox("Enable Inventory Tracking");
    private final CheckBox multiCurrency =
        new CheckBox("Enable Multi-Currency");
    private final TextArea review = new TextArea();
    private int step;

    public CompanyProfileWizardFX(Company company,
        Consumer<CompanyProfileModel> onSave)
    {
        this.onSave = onSave;
        buildChoices(company);
        prefill(company);
        buildSteps();
        setPadding(PanelChrome.PANEL_PADDING);
        setCenter(this.steps[0]);
        setBottom(buildButtons());
        updateStep();
    }

    private void buildChoices(Company company)
    {
        this.legal.getItems().addAll("501(c)(3)", "Non-Profit",
            "Corporation", "LLC", "Other");
        this.legal.setValue("Non-Profit");

        List<String> currencies = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .sorted()
            .toList();
        this.currency.getItems().addAll(currencies);
        this.currency.setValue("USD");

        this.chartTemplate.getItems().addAll(
            CompanyManagementService.chartTemplates());
        this.chartTemplate.setValue(CompanyManagementService.TEMPLATE_SCA);

        this.defaultBank.setEditable(false);
        this.defaultBank.getItems().add("1000");
        if (company != null && company.getChartOfAccounts() != null)
        {
            company.getChartOfAccounts().getAccounts().stream()
                .filter(account -> account != null &&
                    account.getAccountNumber() != null &&
                    isBankType(account.getAccountType()))
                .map(account -> account.getAccountNumber())
                .distinct()
                .sorted()
                .forEach(this.defaultBank.getItems()::add);
        }
        this.defaultBank.getItems().sort(Comparator.naturalOrder());
        this.defaultBank.setValue("1000");
        this.chartTemplate.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (CompanyManagementService.TEMPLATE_EMPTY.equals(newValue))
            {
                this.defaultBank.setValue(null);
            }
            else if (this.defaultBank.getValue() == null)
            {
                this.defaultBank.setValue("1000");
            }
        });
    }

    private static boolean isBankType(AccountType type)
    {
        return type == AccountType.BANK || type == AccountType.CASH ||
            type == AccountType.CHECKING || type == AccountType.MONEYMKRT;
    }

    private void prefill(Company company)
    {
        CompanyProfileModel profile = company == null ? null :
            company.getCompanyProfileModel();
        if (profile == null)
        {
            this.fundAccounting.setSelected(true);
            return;
        }
        this.name.setText(profile.getCompanyName());
        this.legal.setValue(nonblank(profile.getLegalStructure(),
            "Non-Profit"));
        this.taxId.setText(profile.getTaxId());
        this.address.setText(profile.getAddress());
        this.phone.setText(profile.getPhone());
        this.email.setText(profile.getEmail());
        this.fiscalStart.setText(normalizeFiscalStart(
            profile.getFiscalYearStart()));
        this.currency.setValue(nonblank(profile.getBaseCurrency(), "USD"));
        try
        {
            this.startingDate.setValue(LocalDate.parse(
                profile.getStartingBalanceDate()));
        }
        catch (RuntimeException ignored)
        {
            this.startingDate.setValue(LocalDate.now());
        }
        this.chartTemplate.setValue(nonblank(profile.getChartOfAccountsType(),
            CompanyManagementService.TEMPLATE_SCA));
        String configuredBank = profile.getDefaultBankAccount();
        this.defaultBank.setValue(this.defaultBank.getItems().contains(
            configuredBank) ? configuredBank : null);
        this.fundAccounting.setSelected(profile.isEnableFundAccounting());
        this.inventory.setSelected(profile.isEnableInventory());
        this.multiCurrency.setSelected(profile.isEnableMultiCurrency());
    }

    private void buildSteps()
    {
        GridPane identity = grid();
        identity.addRow(0, new Label("Company Name:"), this.name);
        identity.addRow(1, new Label("Legal Structure:"), this.legal);
        identity.addRow(2, new Label("Tax ID:"), this.taxId);
        identity.addRow(3, new Label("Address:"), this.address);
        identity.addRow(4, new Label("Phone:"), this.phone);
        identity.addRow(5, new Label("Email:"), this.email);
        this.steps[0] = titled("Identity", identity);

        GridPane fiscal = grid();
        fiscal.addRow(0, new Label("Fiscal Year Start (MM-DD):"),
            this.fiscalStart);
        fiscal.addRow(1, new Label("Base Currency:"), this.currency);
        fiscal.addRow(2, new Label("Starting Balance Date:"),
            this.startingDate);
        this.steps[1] = titled("Fiscal and Currency Settings", fiscal);

        GridPane chart = grid();
        chart.addRow(0, new Label("Chart Template:"), this.chartTemplate);
        chart.addRow(1, new Label("Default Bank Account:"),
            this.defaultBank);
        this.steps[2] = titled("Chart of Accounts", chart);

        GridPane features = grid();
        features.add(this.fundAccounting, 0, 0, 2, 1);
        features.add(this.inventory, 0, 1, 2, 1);
        features.add(this.multiCurrency, 0, 2, 2, 1);
        this.steps[3] = titled("Features", features);

        this.review.setEditable(false);
        this.review.setWrapText(true);
        this.steps[4] = titled("Review", this.review);
    }

    private HBox buildButtons()
    {
        this.back.setOnAction(event -> {
            this.step--;
            updateStep();
        });
        this.next.setOnAction(event -> {
            if (!validateCurrentStep())
            {
                return;
            }
            this.step++;
            updateStep();
        });
        this.finish.setOnAction(event -> save());
        this.validation.setStyle("-fx-text-fill: #b00020;");
        HBox box = new HBox(10, this.back, this.next, this.finish,
            this.validation);
        box.setPadding(new Insets(12));
        return box;
    }

    private void updateStep()
    {
        if (this.step == this.steps.length - 1)
        {
            this.review.setText(reviewText());
        }
        setCenter(this.steps[this.step]);
        this.back.setDisable(this.step == 0);
        this.next.setVisible(this.step < this.steps.length - 1);
        this.next.setManaged(this.next.isVisible());
        this.finish.setVisible(this.step == this.steps.length - 1);
        this.finish.setManaged(this.finish.isVisible());
        this.validation.setText("");
    }

    private boolean validateCurrentStep()
    {
        if (this.step == 0 && this.name.getText().isBlank())
        {
            this.validation.setText("Company name is required.");
            return false;
        }
        if (this.step == 1 &&
            !this.fiscalStart.getText().matches("\\d{2}-\\d{2}"))
        {
            this.validation.setText("Fiscal year start must use MM-DD.");
            return false;
        }
        return true;
    }

    private void save()
    {
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName(this.name.getText().trim());
        profile.setLegalStructure(this.legal.getValue());
        profile.setTaxId(this.taxId.getText());
        profile.setAddress(this.address.getText());
        profile.setPhone(this.phone.getText());
        profile.setEmail(this.email.getText());
        profile.setFiscalYearStart(this.fiscalStart.getText().trim());
        profile.setBaseCurrency(this.currency.getValue());
        profile.setStartingBalanceDate(this.startingDate.getValue().toString());
        profile.setChartOfAccountsType(this.chartTemplate.getValue());
        profile.setDefaultBankAccount(this.defaultBank.getValue());
        profile.setEnableFundAccounting(this.fundAccounting.isSelected());
        profile.setEnableInventory(this.inventory.isSelected());
        profile.setEnableMultiCurrency(this.multiCurrency.isSelected());
        this.onSave.accept(profile);
    }

    private String reviewText()
    {
        return "Company: " + this.name.getText() + "\n" +
            "Legal structure: " + this.legal.getValue() + "\n" +
            "Fiscal year starts: " + this.fiscalStart.getText() + "\n" +
            "Base currency: " + this.currency.getValue() + "\n" +
            "Starting balance date: " + this.startingDate.getValue() + "\n" +
            "Chart template: " + this.chartTemplate.getValue() + "\n" +
            "Default bank account: " + nonblank(
                this.defaultBank.getValue(), "None") + "\n" +
            "Fund accounting: " + this.fundAccounting.isSelected() + "\n" +
            "Inventory: " + this.inventory.isSelected() + "\n" +
            "Multi-currency: " + this.multiCurrency.isSelected();
    }

    private static GridPane grid()
    {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        ColumnConstraints labels = new ColumnConstraints();
        labels.setPercentWidth(35);
        ColumnConstraints controls = new ColumnConstraints();
        controls.setPercentWidth(65);
        grid.getColumnConstraints().addAll(labels, controls);
        return grid;
    }

    private static TitledPane titled(String title, Node content)
    {
        TitledPane pane = new TitledPane(title, content);
        pane.setCollapsible(false);
        return pane;
    }

    private static String nonblank(String value, String fallback)
    {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String normalizeFiscalStart(String value)
    {
        if (value == null || value.isBlank())
        {
            return "01-01";
        }
        if (value.matches("\\d{4}-\\d{2}-\\d{2}"))
        {
            return value.substring(5);
        }
        return value;
    }
}
