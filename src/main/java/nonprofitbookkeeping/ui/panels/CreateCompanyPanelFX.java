
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

/**
 * JavaFX wizard for creating or editing a company profile.
 * Replaces the Swing {@code CreateCompanyPanel}.
 */
public class CreateCompanyPanelFX extends BorderPane
{
	
	private int step = 0;
	private final Node[] steps = new Node[3];
	
	private final Button back = new Button("Back");
	private final Button next = new Button("Next");
	private final Button finish = new Button("Save Company");
	
	/* ——— Company Info fields ——— */
	private final TextField nameField = new TextField();
	private final ComboBox<String> legalCb = new ComboBox<>();
	private final TextField taxIdField = new TextField();
	private final TextField addressField = new TextField();
	private final TextField phoneField = new TextField();
	private final TextField emailField = new TextField();
	
	/* ——— Fiscal Settings ——— */
	private final TextField fiscalStartField = new TextField("2025-01-01");
	private final ComboBox<String> currencyCb = new ComboBox<>();
	private final TextField startBalField = new TextField("2025-01-01");
	private final ComboBox<String> chartCb = new ComboBox<>();
	
	/* ——— Admin / Features ——— */
	private final TextField adminUserField = new TextField("admin");
	private final PasswordField adminPass = new PasswordField();
	private final TextField bankAcctField = new TextField("Bank Checking");
	private final CheckBox fundBox = new CheckBox("Enable Fund Accounting");
	private final CheckBox inventoryBox = new CheckBox("Enable Inventory Tracking");
	private final CheckBox multiCurBox = new CheckBox("Enable Multi-Currency");
	
	private final CompanyCreatedCallback callback;
	
	/**
	 * 
	 * Constructor CreateCompanyPanelFX
	 * @param existing
	 * @param cb
	 */
	public CreateCompanyPanelFX(Company existing, CompanyCreatedCallback cb)
	{
		this.callback = cb;
		setPadding(new Insets(10));
		
		buildSteps(existing);
		
		setCenter(this.steps[0]);
		setBottom(buildButtons());
		updateStep();
	}
	
	/* --------------------------------------------------------------------- */
	private void buildSteps(Company ex)
	{
		
		/* Prefill if editing */
		if (ex != null && ex.getCompanyProfile() != null)
		{
			var p = ex.getCompanyProfile();
			this.nameField.setText(p.getCompanyName());
			this.taxIdField.setText(p.getTaxId());
			this.addressField.setText(p.getAddress());
			this.phoneField.setText(p.getPhone());
			this.emailField.setText(p.getEmail());
			this.legalCb.setValue(p.getLegalStructure());
			this.fiscalStartField.setText(p.getFiscalYearStart());
			this.currencyCb.setValue(p.getBaseCurrency());
			this.startBalField.setText(p.getStartingBalanceDate());
			this.chartCb.setValue(p.getChartOfAccountsType());
			this.adminUserField.setText(p.getAdminUsername());
			this.adminPass.setText(p.getAdminPassword());
			this.bankAcctField.setText(p.getDefaultBankAccount());
			this.fundBox.setSelected(p.isEnableFundAccounting());
			this.inventoryBox.setSelected(p.isEnableInventory());
			this.multiCurBox.setSelected(p.isEnableMultiCurrency());
		}
		
		/* Company Info */
		this.legalCb.getItems().addAll("501(c)(3)", "LLC", "Corporation", "Other");
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
		this.chartCb.getItems().addAll("Standard Nonprofit", "Basic", "Custom Upload");
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
		this.steps[2] = titled("Admin & Features", g3);
	}
	
	/**
	 * 
	 * @param rows
	 * @return
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
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	private static TitledPane titled(String title, Node content)
	{
		TitledPane tp = new TitledPane(title, content);
		tp.setCollapsible(false);
		return tp;
	}
	
	/**
	 * 
	 * @return
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
	 * 
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
	 * 
	 */
	private void saveAndExit()
	{
		CompanyProfileModel m = new CompanyProfileModel();
		m.setCompanyName(this.nameField.getText());
		m.setLegalStructure(this.legalCb.getValue());
		m.setTaxId(this.taxIdField.getText());
		m.setAddress(this.addressField.getText());
		m.setPhone(this.phoneField.getText());
		m.setEmail(this.emailField.getText());
		m.setFiscalYearStart(this.fiscalStartField.getText());
		m.setBaseCurrency(this.currencyCb.getValue());
		m.setStartingBalanceDate(this.startBalField.getText());
		m.setChartOfAccountsType(this.chartCb.getValue());
		m.setAdminUsername(this.adminUserField.getText());
		m.setAdminPassword(this.adminPass.getText());
		m.setDefaultBankAccount(this.bankAcctField.getText());
		m.setEnableFundAccounting(this.fundBox.isSelected());
		m.setEnableInventory(this.inventoryBox.isSelected());
		m.setEnableMultiCurrency(this.multiCurBox.isSelected());
		
		if (this.callback != null)
		{
			this.callback.onCreatedProfileModel(m);
		}
		else
		{
			new Alert(Alert.AlertType.INFORMATION, "Company saved: " + m.getCompanyName()).showAndWait();
		}
	}
	
}
