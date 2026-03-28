
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.api.CompanyCreatedCallback;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: Auto-generated Javadoc
/**
 * JavaFX wizard for creating or editing a company profile.
 * Replaces the Swing {@code CreateCompanyPanel}.
 */
public class CreateOrEditCompanyPanelFX extends BorderPane
{
	
	/** Tracks the current step in the wizard (0-indexed). */
	private int step = 0;
	/** Array holding the UI {@link Node} for each step of the wizard. */
	private final Node[] steps = new Node[3];
	
	/** Button to navigate to the previous step in the wizard. */
	private final Button back = new Button("Back");
	/** Button to navigate to the next step in the wizard. */
	private final Button next = new Button("Next");
	/** Button to finalize and save the company profile. */
	private final Button finish = new Button("Save Company");
	
	/* ——— Company Info fields ——— */
	/** TextField for the company's name. */
	private final TextField nameField = new TextField();
	/** ComboBox for selecting the company's legal structure (e.g., 501(c)(3), LLC). */
	private final ComboBox<String> legalCb = new ComboBox<>();
	/** TextField for the company's tax identification number. */
	private final TextField taxIdField = new TextField();
	/** TextField for the company's physical address. */
	private final TextField addressField = new TextField();
	/** TextField for the company's phone number. */
	private final TextField phoneField = new TextField();
	/** TextField for the company's email address. */
	private final TextField emailField = new TextField();
	
	/* ——— Fiscal Settings ——— */
	/** TextField for specifying the fiscal year start date (e.g., "YYYY-MM-DD"). */
	private final TextField fiscalStartField = new TextField("2025-01-01");
	/** ComboBox for selecting the company's base currency (e.g., USD, EUR). */
	private final ComboBox<String> currencyCb = new ComboBox<>();
	/** TextField for specifying the starting balance date for accounting records. */
	private final TextField startBalField = new TextField("2025-01-01");
	/** ComboBox for selecting the type of Chart of Accounts to use (e.g., Standard Nonprofit). */
	private final ComboBox<String> chartCb = new ComboBox<>();
	
	/* ——— Admin / Features ——— */
	/** TextField for the administrator's username. */
	private final TextField adminUserField = new TextField("admin");
	/** PasswordField for the administrator's password. */
	private final PasswordField adminPass = new PasswordField();
	/** TextField for the default bank account name. */
	private final TextField bankAcctField = new TextField("Bank Checking");
	/** CheckBox to enable or disable fund accounting features. */
	private final CheckBox fundBox = new CheckBox("Enable Fund Accounting");
	/** CheckBox to enable or disable inventory tracking features. */
	private final CheckBox inventoryBox =
		new CheckBox("Enable Inventory Tracking");
	/** CheckBox to enable or disable multi-currency support. */
	private final CheckBox multiCurBox = new CheckBox("Enable Multi-Currency");
	/** Allows the user to seed demo data when creating a new company. */
	private final CheckBox demoDataBox = new CheckBox(
		"Populate demo chart of accounts and sample transactions");
	
	/** Callback interface invoked when the company profile is created or successfully edited and saved. */
	private final CompanyCreatedCallback callback;
	
	/**
	 * Constructs a new {@code CreateOrEditCompanyPanelFX} wizard.
	 * This panel guides the user through multiple steps to input company profile information.
	 * If an {@code existing} company is provided, its data is used to pre-fill the wizard fields.
	 * The {@code cb} callback is invoked upon successful completion and saving of the company profile.
	 * 
	 * @param existing The {@link Company} object to edit. If a new company is being created,
	 *                 this should be a new {@code Company} instance (or null, though current code uses `checkNotNull`).
	 *                 Its profile data will be used to pre-populate the fields. Must not be null.
	 * @param cb The {@link CompanyCreatedCallback} to be invoked when the company profile is saved.
	 *           This callback receives the newly created or updated {@link CompanyProfileModel}. Must not be null.
	 * @throws NullPointerException if {@code existing} or {@code cb} is null.
	 */
	public CreateOrEditCompanyPanelFX(Company existing,
		CompanyCreatedCallback cb)
	{
		checkNotNull(existing);
		checkNotNull(cb);
		
		this.callback = cb; // save the callback
		
		setPadding(new Insets(10));
		
		// run the wizard
		buildSteps(existing);
		
		setCenter(this.steps[0]);
		setBottom(buildButtons());
		updateStep();
		
	}
	
	
	/**
	 * Creates the UI panels for each step of the wizard.
	 * If a {@code company} object is provided and contains a profile, its data is used to pre-fill the input fields.
	 * The steps are:
	 * <ol>
	 *   <li>Company Information (name, legal structure, tax ID, address, phone, email).</li>
	 *   <li>Fiscal Settings (fiscal year start, base currency, starting balance date, chart of accounts type).</li>
	 *   <li>Admin & Features (admin user/pass, default bank account, feature toggles for fund accounting, inventory, multi-currency).</li>
	 * </ol>
	 * Each step is a {@link TitledPane} containing a {@link GridPane} with the respective fields.
	 * 
	 * @param company The {@link Company} object whose data may be used to pre-fill the wizard fields.
	 *                If {@code company} or its profile is null, fields will have default or empty values.
	 */
	private void buildSteps(Company company)
	{
		
		/* Prefill if editing */
		if (company != null && company.getCompanyProfile() != null)
		{
			var profile = company.getCompanyProfile();
			
			this.nameField.setText(profile.getCompanyName());
			this.taxIdField.setText(profile.getTaxId());
			this.addressField.setText(profile.getAddress());
			this.phoneField.setText(profile.getPhone());
			this.emailField.setText(profile.getEmail());
			this.legalCb.setValue(profile.getLegalStructure());
			this.fiscalStartField.setText(profile.getFiscalYearStart());
			this.currencyCb.setValue(profile.getBaseCurrency());
			this.startBalField.setText(profile.getStartingBalanceDate());
			this.chartCb.setValue(profile.getChartOfAccountsType());
			this.adminUserField.setText(profile.getAdminUsername());
			this.adminPass.setText(profile.getAdminPassword());
			this.bankAcctField.setText(profile.getDefaultBankAccount());
			this.fundBox.setSelected(profile.isEnableFundAccounting());
			this.inventoryBox.setSelected(profile.isEnableInventory());
			this.multiCurBox.setSelected(profile.isEnableMultiCurrency());
		}
		
		/* Company Info */
		this.legalCb.getItems().addAll("501(c)(3)", "LLC", "Corporation",
			"Other");
		GridPane g1 = grid(6);
		g1.addRow(0, new Label("Company Name:"), this.nameField);
		g1.addRow(1, new Label("Legal Structure:"), this.legalCb);
		g1.addRow(2, new Label("Tax ID:"), this.taxIdField);
		g1.addRow(3, new Label("Address:"), this.addressField);
		g1.addRow(4, new Label("Phone:"), this.phoneField);
		g1.addRow(5, new Label("Email:"), this.emailField);
		this.steps[0] = titled("Company Information", g1);
		
		/* Fiscal Settings */
		this.currencyCb.getItems().addAll("USD", "EUR", "GBP");
		this.chartCb.getItems().addAll("Standard Nonprofit", "Basic",
			"Custom Upload");
		GridPane g2 = grid(4);
		g2.addRow(0, new Label("Fiscal Year Start:"), this.fiscalStartField);
		g2.addRow(1, new Label("Base Currency:"), this.currencyCb);
		g2.addRow(2, new Label("Starting Balance Date:"), this.startBalField);
		g2.addRow(3, new Label("Chart of Accounts:"), this.chartCb);
		this.steps[1] = titled("Fiscal Settings", g2);
		
		/* Admin & Features */
		GridPane g3 = grid(6);
		g3.addRow(0, new Label("Admin Username:"), this.adminUserField);
		g3.addRow(1, new Label("Admin Password:"), this.adminPass);
		g3.addRow(2, new Label("Default Bank Account:"), this.bankAcctField);
		g3.addRow(3, this.fundBox, new Label());
		g3.addRow(4, this.inventoryBox, new Label());
		g3.addRow(5, this.multiCurBox, new Label());
		g3.addRow(6, this.demoDataBox, new Label());
		this.demoDataBox.setSelected(true);
		this.steps[2] = titled("Admin & Features", g3);
		
	}
	
	/**
	 * Builds a {@link GridPane} with a predefined two-column layout (30%/70% width)
	 * and standard padding and gap settings. This is a utility method for constructing
	 * the layout of each wizard step.
	 *
	 * @param rows The number of rows this grid will initially be configured for (though more can be added).
	 *             This parameter is not strictly used to limit rows but might be for initial capacity or context.
	 * @return A configured {@link GridPane} instance.
	 */
	private static GridPane grid(int rows)
	{
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(8);
		g.setPadding(new Insets(10));
		
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(30);
		
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(70);
		
		g.getColumnConstraints().addAll(c1, c2);
		return g;
		
	}
	
	/**
	 * Creates a non-collapsible {@link TitledPane} with the given title and content.
	 * This is a utility method for wrapping each wizard step's content.
	 * 
	 * @param title The title to be displayed on the pane.
	 * @param content The {@link Node} (typically a {@link GridPane}) to be set as the content of the pane.
	 * @return A configured {@link TitledPane} instance.
	 */
	private static TitledPane titled(String title, Node content)
	{
		TitledPane tp = new TitledPane(title, content);
		tp.setCollapsible(false);
		return tp;
		
	}
	
	/**
	 * Builds the {@link HBox} containing the navigation buttons for the wizard (Back, Next, Save Company).
	 * Sets up action handlers for these buttons to control step navigation and final save action.
	 *
	 * @return An {@link HBox} containing the configured navigation buttons.
	 */
	private HBox buildButtons()
	{
		HBox box = new HBox(10, this.back, this.next, this.finish);
		box.setPadding(new Insets(10));
		
		this.back.setOnAction(e -> {
			this.step--;
			updateStep();
		});
		
		this.next.setOnAction(e -> {
			this.step++;
			updateStep();
		});
		
		this.finish.setOnAction(e -> saveAndExit());
		return box;
		
	}
	
	/**
	 * Updates the wizard's UI to reflect the current step.
	 * This involves setting the central content of the {@link BorderPane} to the current step's UI Node,
	 * and adjusting the visibility and enabled state of the navigation buttons (Back, Next, Save Company)
	 * based on whether the current step is the first, last, or an intermediate step.
	 */
	private void updateStep()
	{
		setCenter(this.steps[this.step]);
		this.back.setDisable(this.step == 0);
		this.next.setDisable(this.step == this.steps.length - 1);
		this.next.setVisible(this.step < this.steps.length - 1);
		this.finish.setVisible(this.step == this.steps.length - 1);
		
	}
	
	/**
	 * Collects all data from the wizard's input fields, populates a {@link CompanyProfileModel},
	 * and then invokes the {@link #callback} with this model.
	 * If no callback is configured (which shouldn't happen based on constructor preconditions but is checked),
	 * it shows an informational alert. This method is called when the "Save Company" button is clicked.
	 */
	private void saveAndExit()
	{
		CompanyProfileModel model = new CompanyProfileModel();
		
		model.setCompanyName(this.nameField.getText());
		model.setLegalStructure(this.legalCb.getValue());
		model.setTaxId(this.taxIdField.getText());
		model.setAddress(this.addressField.getText());
		model.setPhone(this.phoneField.getText());
		model.setEmail(this.emailField.getText());
		model.setFiscalYearStart(this.fiscalStartField.getText());
		model.setBaseCurrency(this.currencyCb.getValue());
		model.setStartingBalanceDate(this.startBalField.getText());
		model.setChartOfAccountsType(this.chartCb.getValue());
		model.setAdminUsername(this.adminUserField.getText());
		model.setAdminPassword(this.adminPass.getText());
		model.setDefaultBankAccount(this.bankAcctField.getText());
		model.setEnableFundAccounting(this.fundBox.isSelected());
		model.setEnableInventory(this.inventoryBox.isSelected());
		model.setEnableMultiCurrency(this.multiCurBox.isSelected());
		
		if (this.callback != null)
		{
			boolean seedDemo =
				this.demoDataBox.isSelected() && !this.demoDataBox.isDisable();
			this.callback.onCreatedProfileModel(model, seedDemo);
		}
		else
		{
			new Alert(Alert.AlertType.INFORMATION, "Company saved: " +
				model.getCompanyName()).showAndWait();
		}
		
	}
	
	/**
	 * Enables or disables the demo data seeding option.
	 *
	 * @param available the new demo seeding available
	 */
	public void setDemoSeedingAvailable(boolean available)
	{
		this.demoDataBox.setDisable(!available);
		
		if (!available)
		{
			this.demoDataBox.setSelected(false);
		}
		
	}
	
}
