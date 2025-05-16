package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.api.CompanyCreatedCallback;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CompanyProfileModel;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel used to create or edit a company profile.
 */
public class CreateCompanyPanel extends JPanel
{
    /**
     * serialVersionUID : long
     */
    private static final long serialVersionUID = -1750034274967438043L;
    private CardLayout cardLayout;
    private JPanel wizardPanel;
    private JButton backButton, nextButton, finishButton;
    private int step = 0;

    private final CompanyCreatedCallback callback;
    
    // Fields to collect data
    private JTextField nameField, taxIdField, addressField, phoneField, emailField;
    private JComboBox<String> legalCombo;
    
    private JTextField fiscalStartField, startDateField;
    private JComboBox<String> currencyCombo, chartCombo;
    
    private JTextField adminUserField, bankAccountField;
    private JPasswordField adminPassField;
    private JCheckBox fundBox, inventoryBox, multicurrencyBox;
    
    /**
     * Constructor CreateCompanyPanel (for creating or editing a company)
     * @param existingProfile the company data file containing the existing profile (if editing)
     * @param callback the callback function for when the company is created or updated
     */
    public CreateCompanyPanel(CompanyDataFile existingProfile, CompanyCreatedCallback callback)
    {
        this.callback = callback;
        setLayout(new BorderLayout());
        
        this.cardLayout = new CardLayout();
        this.wizardPanel = new JPanel(this.cardLayout);
        
        // Step 1: Company Info
        JPanel companyInfoPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        companyInfoPanel.setBorder(new TitledBorder("Company Information"));
        this.nameField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getCompanyName() : "");
        this.taxIdField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getTaxId() : "");
        this.addressField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getAddress() : "");
        this.phoneField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getPhone() : "");
        this.emailField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getEmail() : "");
        this.legalCombo = new JComboBox<>(new String[] { "501(c)(3)", "LLC", "Corporation", "Other" });
        if (existingProfile != null && existingProfile.getCompanyProfile() != null) {
            this.legalCombo.setSelectedItem(existingProfile.getCompanyProfile().getLegalStructure());
        }
        
        companyInfoPanel.add(new JLabel("Company Name:"));
        companyInfoPanel.add(this.nameField);
        companyInfoPanel.add(new JLabel("Legal Structure:"));
        companyInfoPanel.add(this.legalCombo);
        companyInfoPanel.add(new JLabel("Tax ID:"));
        companyInfoPanel.add(this.taxIdField);
        companyInfoPanel.add(new JLabel("Address:"));
        companyInfoPanel.add(this.addressField);
        companyInfoPanel.add(new JLabel("Phone:"));
        companyInfoPanel.add(this.phoneField);
        companyInfoPanel.add(new JLabel("Email:"));
        companyInfoPanel.add(this.emailField);
        
        // Step 2: Fiscal Settings
        JPanel fiscalSettingsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        fiscalSettingsPanel.setBorder(new TitledBorder("Fiscal Settings"));
        this.fiscalStartField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getFiscalYearStart() : "2025-01-01");
        this.currencyCombo = new JComboBox<>(new String[] { "USD", "EUR", "GBP" });
        if (existingProfile != null && existingProfile.getCompanyProfile() != null) {
            this.currencyCombo.setSelectedItem(existingProfile.getCompanyProfile().getBaseCurrency());
        }
        this.startDateField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getStartingBalanceDate() : "2025-01-01");
        this.chartCombo = new JComboBox<>(new String[] { "Standard Nonprofit", "Basic", "Custom Upload" });
        if (existingProfile != null && existingProfile.getCompanyProfile() != null) {
            this.chartCombo.setSelectedItem(existingProfile.getCompanyProfile().getChartOfAccountsType());
        }
        
        fiscalSettingsPanel.add(new JLabel("Fiscal Year Start:"));
        fiscalSettingsPanel.add(this.fiscalStartField);
        fiscalSettingsPanel.add(new JLabel("Base Currency:"));
        fiscalSettingsPanel.add(this.currencyCombo);
        fiscalSettingsPanel.add(new JLabel("Starting Balance Date:"));
        fiscalSettingsPanel.add(this.startDateField);
        fiscalSettingsPanel.add(new JLabel("Chart of Accounts:"));
        fiscalSettingsPanel.add(this.chartCombo);
        
        // Step 3: Admin & Features
        JPanel adminPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        adminPanel.setBorder(new TitledBorder("Admin and Features"));
        this.adminUserField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getAdminUsername() : "admin");
        this.adminPassField = new JPasswordField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getAdminPassword() : "");
        this.bankAccountField = new JTextField(existingProfile != null && existingProfile.getCompanyProfile() != null ? existingProfile.getCompanyProfile().getDefaultBankAccount() : "Bank Checking");
        
        this.fundBox = new JCheckBox("Enable Fund Accounting", existingProfile != null && existingProfile.getCompanyProfile() != null && existingProfile.getCompanyProfile().isEnableFundAccounting());
        this.inventoryBox = new JCheckBox("Enable Inventory Tracking", existingProfile != null && existingProfile.getCompanyProfile() != null && existingProfile.getCompanyProfile().isEnableInventory());
        this.multicurrencyBox = new JCheckBox("Enable Multi-Currency", existingProfile != null && existingProfile.getCompanyProfile() != null && existingProfile.getCompanyProfile().isEnableMultiCurrency());
        
        adminPanel.add(new JLabel("Admin Username:"));
        adminPanel.add(this.adminUserField);
        adminPanel.add(new JLabel("Admin Password:"));
        adminPanel.add(this.adminPassField);
        adminPanel.add(new JLabel("Default Bank Account:"));
        adminPanel.add(this.bankAccountField);
        adminPanel.add(this.fundBox);
        adminPanel.add(new JLabel());
        adminPanel.add(this.inventoryBox);
        adminPanel.add(new JLabel());
        adminPanel.add(this.multicurrencyBox);
        adminPanel.add(new JLabel());
        
        this.wizardPanel.add(companyInfoPanel, "Company");
        this.wizardPanel.add(fiscalSettingsPanel, "Fiscal");
        this.wizardPanel.add(adminPanel, "Admin");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.backButton = new JButton("Back");
        this.nextButton = new JButton("Next");
        this.finishButton = new JButton("Save Company");
        
        this.backButton.setEnabled(false);
        this.finishButton.setVisible(true);
        
        this.backButton.addActionListener(e -> {
            if (this.step > 0)
            {
                this.step--;
                updateStep();
            }
        });
        this.nextButton.addActionListener(e -> {
            if (this.step < 2)
            {
                this.step++;
                updateStep();
            }
        });
        this.finishButton.addActionListener(e -> finishWizard());
        
        buttonPanel.add(this.backButton);
        buttonPanel.add(this.nextButton);
        buttonPanel.add(this.finishButton);
        
        add(this.wizardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateStep();
    }

    /**
     * Updates the view based on the current step of the wizard.
     */
    private void updateStep()
    {
        switch(this.step)
        {
            case 0 -> {
                this.cardLayout.show(this.wizardPanel, "Company");
                this.backButton.setEnabled(false);
                this.nextButton.setVisible(true);
                this.finishButton.setVisible(false);
            }
            case 1 -> {
                this.cardLayout.show(this.wizardPanel, "Fiscal");
                this.backButton.setEnabled(true);
                this.nextButton.setVisible(true);
                this.finishButton.setVisible(false);
            }
            case 2 -> {
                this.cardLayout.show(this.wizardPanel, "Admin");
                this.backButton.setEnabled(true);
                this.nextButton.setVisible(false);
                this.finishButton.setVisible(true);
            }
			default -> throw new IllegalArgumentException("Unexpected value: " + this.step);
        }
    }

    /**
     * Finalizes the creation or updating of the company profile.
     */
    private void finishWizard()
    {
        CompanyProfileModel model = new CompanyProfileModel();
        model.setCompanyName(this.nameField.getText());
        model.setLegalStructure((String) this.legalCombo.getSelectedItem());
        model.setTaxId(this.taxIdField.getText());
        model.setAddress(this.addressField.getText());
        model.setPhone(this.phoneField.getText());
        model.setEmail(this.emailField.getText());
        
        model.setFiscalYearStart(this.fiscalStartField.getText());
        model.setBaseCurrency((String) this.currencyCombo.getSelectedItem());
        model.setStartingBalanceDate(this.startDateField.getText());
        model.setChartOfAccountsType((String) this.chartCombo.getSelectedItem());
        
        model.setAdminUsername(this.adminUserField.getText());
        model.setAdminPassword(new String(this.adminPassField.getPassword()));
        model.setDefaultBankAccount(this.bankAccountField.getText());
        
        model.setEnableFundAccounting(this.fundBox.isSelected());
        model.setEnableInventory(this.inventoryBox.isSelected());
        model.setEnableMultiCurrency(this.multicurrencyBox.isSelected());
        
        if (this.callback != null)
        {
            this.callback.onCompanyCreated(model);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Company saved: " + model.getCompanyName());
        }
    }
}
