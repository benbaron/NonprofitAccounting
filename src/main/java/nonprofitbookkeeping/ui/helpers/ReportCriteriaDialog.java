
package nonprofitbookkeeping.ui.helpers;

import nonprofitbookkeeping.model.Account; // Added
import nonprofitbookkeeping.model.ChartOfAccounts; // Added
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.reports.ReportConfiguration; // Added
import nonprofitbookkeeping.reports.ReportCriteria;
import javax.swing.*;

import org.jdatepicker.UtilDateModel;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class ReportCriteriaDialog
{
	
	// DateSelectionMode enum has been moved to its own file: DateSelectionMode.java
	
	private static Component fundListScrollPane;


	private static class FundItem
	{
		private final Fund fund;
		
		public FundItem(Fund fund)
		{
			this.fund = fund;
		}
		
		public Fund getFund()
		{
			return fund;
		}
		
		public String getId()
		{
			// Assuming Fund has getId() or similar unique identifier method.
			// If it's getFundId(), adjust accordingly.
			return fund.getFundId(); // Updated to getFundId as per BudgetPanel example
		}
		
		@Override public String toString()
		{
			// Assuming Fund has getName().
			return fund.getName();
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			FundItem fundItem = (FundItem) o;
			return Objects.equals(getId(), fundItem.getId());
		}
		
		@Override public int hashCode()
		{
			return Objects.hash(getId());
		}
		
	}
	
	public static Optional<ReportCriteria> showDialog(
														Component parentComponent,
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector)
	{
		// Call the main overloaded method with null for initialConfig, chartOfAccounts,
		// and false for showAccountSelector
		return showDialog(parentComponent, title, availableFunds, null, dateMode, showFundSelector,
			false, null);
	}
	
	// Overloaded method that takes initialConfig but not
	// chartOfAccounts/showAccountSelector
	public static Optional<ReportCriteria> showDialog(
														Component parentComponent,
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector,
														ReportConfiguration initialConfig)
	{
		return showDialog(parentComponent, title, availableFunds, null, dateMode, showFundSelector,
			false, initialConfig);
	}
	
	// New Inner Class AccountItem
	private static class AccountItem
	{
		private final Account account;
		
		public AccountItem(Account account)
		{
			this.account = account;
		}
		
		public Account getAccount()
		{
			return account;
		}
		
		public String getId()
		{
			return account.getAccountNumber();
		} // Using AccountNumber as ID
		
		@Override public String toString()
		{
			return account.getName() + " (" + account.getAccountNumber() + ")";
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		@Override public int hashCode()
		{
			return Objects.hash(account.getAccountNumber());
		}
		
	}
	
	
	// Main overloaded method with all parameters
	public static Optional<ReportCriteria> showDialog(
														Component parentComponent,
														String title,
														List<Fund> availableFunds,
														ChartOfAccounts chartOfAccounts, // Added
														DateSelectionMode dateMode,
														boolean showFundSelector,
														boolean showAccountSelector, // Added
														ReportConfiguration initialConfig)
	{
		
		final DateSelectionMode actualDateMode =
			(initialConfig != null && initialConfig.getDateSelectionMode() != null) ?
				initialConfig.getDateSelectionMode() : dateMode;
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 4, 4, 4);
		
		Properties datePickerProps = new Properties();
		datePickerProps.put("text.today", "Today");
		datePickerProps.put("text.month", "Month");
		datePickerProps.put("text.year", "Year");
		
		JDatePickerImpl startDatePicker = null;
		JDatePickerImpl endDatePicker = null;
		UtilDateModel startDateModel = new UtilDateModel();
		UtilDateModel endDateModel = new UtilDateModel();
		
		if (initialConfig != null && initialConfig.getSpecificStartDate() != null)
		{
			LocalDate sd = initialConfig.getSpecificStartDate();
			startDateModel.setDate(sd.getYear(), sd.getMonthValue() - 1, sd.getDayOfMonth());
			startDateModel.setSelected(true);
		}
		
		if (initialConfig != null && initialConfig.getSpecificEndDate() != null)
		{
			LocalDate ed = initialConfig.getSpecificEndDate();
			endDateModel.setDate(ed.getYear(), ed.getMonthValue() - 1, ed.getDayOfMonth());
			endDateModel.setSelected(true);
		}
		
		if (actualDateMode == DateSelectionMode.DATE_RANGE_MANDATORY_START ||
			actualDateMode == DateSelectionMode.DATE_RANGE_OPTIONAL_START)
		{
			panel.add(new JLabel("Start Date:"), gbc);
			gbc.gridx++;
			JDatePanelImpl startDatePanel = new JDatePanelImpl(startDateModel, datePickerProps);
			startDatePicker = new JDatePickerImpl(startDatePanel, null);
				//new JDatePickerImpl.JFormattedTextFieldFactory());
			// FIXME
			// panel.add(startDatePicker, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
		}
		
		String endDateLabelText =
			(actualDateMode == DateSelectionMode.SINGLE_DATE) ? "Date:" : "End Date:";
		panel.add(new JLabel(endDateLabelText), gbc);
		gbc.gridx++;
		JDatePanelImpl endDatePanel = new JDatePanelImpl(endDateModel, datePickerProps);
		endDatePicker =
			new JDatePickerImpl(endDatePanel, null);
		//new JDatePickerImpl.JFormattedTextFieldFactory());
		// FIXME panel.add(endDatePicker, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		
		JList<FundItem> fundList = null;
		JCheckBox selectAllFundsCheckbox = null;
		
		if (showFundSelector)
		{
			// ... (existing fund selector UI code remains here, unchanged) ...
			// For brevity, this part is not repeated in the diff if it's identical.
			// Ensure it's the same as before.
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			panel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
			gbc.gridy++;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridwidth = 1;
			
			boolean initiallySelectAllFunds = true;
			List<String> initialFundIds = null;
			
			if (initialConfig != null && initialConfig.getFundIds() != null &&
				!initialConfig.getFundIds().isEmpty())
			{
				initiallySelectAllFunds = false;
				initialFundIds = initialConfig.getFundIds();
			}
			
			selectAllFundsCheckbox = new JCheckBox("Select All Funds", initiallySelectAllFunds);
			panel.add(selectAllFundsCheckbox, gbc);
			gbc.gridy++;
			List<Fund> funds = availableFunds != null ? availableFunds : new ArrayList<>();
			FundItem[] fundItems = funds.stream().map(FundItem::new).toArray(FundItem[]::new);
			fundList = new JList<>(fundItems);
			fundList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			fundList.setEnabled(!selectAllFundsCheckbox.isSelected());
			
			if (!initiallySelectAllFunds && initialFundIds != null)
			{
				List<Integer> indicesToSelect = new ArrayList<>();
				
				for (int i = 0; i < fundItems.length; i++)
				{
					if (initialFundIds.contains(fundItems[i].getId()))
						indicesToSelect.add(i);
				}
				
				fundList.setSelectedIndices(
					indicesToSelect.stream().mapToInt(Integer::intValue).toArray());
			}
			
			JScrollPane fundListScrollPane = new JScrollPane(fundList);
			fundListScrollPane.setPreferredSize(new Dimension(250, 100));
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			panel.add(fundListScrollPane, gbc);
			gbc.gridy++;
			final JList<FundItem> finalFundList = fundList;
			
			if (selectAllFundsCheckbox != null)
			{ // Check if checkbox was created
				selectAllFundsCheckbox.addItemListener(e -> {
					
					if (finalFundList != null)
					{
						finalFundList.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
						if (e.getStateChange() == ItemEvent.SELECTED)
							finalFundList.clearSelection();
					}
					
				});
			}
			
		}
		
		JList<AccountItem> accountListJList = null; // Renamed to avoid conflict with java.awt.List
		
		if (showAccountSelector)
		{
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			panel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
			gbc.gridy++;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridwidth = 1;
			panel.add(new JLabel("Select Accounts (for Detail Reports):"), gbc);
			gbc.gridy++;
			
			List<Account> accounts =
				(chartOfAccounts != null && chartOfAccounts.getAccounts() != null) ?
					chartOfAccounts.getAccounts() : new ArrayList<>();
			AccountItem[] accountItems = accounts.stream()
				.sorted(java.util.Comparator.comparing(Account::getName)) // Sort by name
				.map(AccountItem::new)
				.toArray(AccountItem[]::new);
			accountListJList = new JList<>(accountItems);
			accountListJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			if (initialConfig != null && initialConfig.getAccountIdsForDetailReport() != null &&
				!initialConfig.getAccountIdsForDetailReport().isEmpty())
			{
				List<String> initialAccountIds = 
					initialConfig.getAccountIdsForDetailReport();
				List<Integer> indicesToSelect = new ArrayList<>();
				
				for (int i = 0; i < accountItems.length; i++)
				{
					
					if (initialAccountIds.contains(accountItems[i].getId()))
					{
						indicesToSelect.add(i);
					}
					
				}
				
				accountListJList.setSelectedIndices(
					indicesToSelect.stream().mapToInt(Integer::intValue).toArray());
			}
			
			JScrollPane accountListScrollPane = new JScrollPane(accountListJList);
			fundListScrollPane.setPreferredSize(new Dimension(250, 100)); // Set preferred size for
																			// scroll pane
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			panel.add(fundListScrollPane, gbc);
			gbc.gridy++;
			
			final JList<FundItem> finalFundList = fundList; // For use in lambda
			selectAllFundsCheckbox.addItemListener(e -> {
				
				if (finalFundList != null)
				{
					finalFundList.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
					
					if (e.getStateChange() == ItemEvent.SELECTED)
					{
						finalFundList.clearSelection();
					}
					
				}
				
			});
		}
		
		// Custom buttons for the dialog
		Object[] options =
		{ "Run Report", "Save Configuration...", "Cancel" };
		int result = JOptionPane.showOptionDialog(parentComponent, panel, title,
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE,
			null, // do not use a custom Icon
			options, // the titles of buttons
			options[0]); // default button title
		
		if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION)
		{ // "Run Report" or "Save Configuration..."
			LocalDate startDate = null;
			
			if (startDatePicker != null)
			{
				Calendar selectedStartCal = null;
				// FIXME (Calendar) startDatePicker.getModel().getValue();
				
//				if (selectedStartCal != null)
//				{
//					startDate =
//						selectedStartCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//				}
				
			}
			
			LocalDate endDate = null;
			Calendar selectedEndCal = null;
			// FIXME (Calendar) endDatePicker.getModel().getValue();
			
			if (selectedEndCal != null)
			{
				endDate = selectedEndCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
			
			List<String> selectedFundIds = new ArrayList<>();
			
			if (showFundSelector && selectAllFundsCheckbox != null &&
				!selectAllFundsCheckbox.isSelected() && fundList != null)
			{
				selectedFundIds = fundList.getSelectedValuesList().stream()
					.map(FundItem::getId)
					.collect(Collectors.toList());
			}
			// If "Select All Funds" is checked, selectedFundIds remains empty, signifying
			// no specific fund filter.
			
			// Validation for dates (as before)
			if (actualDateMode == DateSelectionMode.SINGLE_DATE && endDate == null)
			{
				JOptionPane.showMessageDialog(panel, "Date is required.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
				return showDialog(parentComponent, title, availableFunds, chartOfAccounts,
					actualDateMode, showFundSelector, showAccountSelector, initialConfig);
			}
			
			if (actualDateMode == DateSelectionMode.DATE_RANGE_MANDATORY_START &&
				(startDate == null || endDate == null))
			{
				JOptionPane.showMessageDialog(panel, "Both Start Date and End Date are required.",
					"Input Error", JOptionPane.ERROR_MESSAGE);
				return showDialog(parentComponent, title, availableFunds, chartOfAccounts,
					actualDateMode, showFundSelector, showAccountSelector, initialConfig);
			}
			
			if (actualDateMode == DateSelectionMode.DATE_RANGE_OPTIONAL_START && endDate == null)
			{
				JOptionPane.showMessageDialog(panel, "End Date is required.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
				return showDialog(parentComponent, title, availableFunds, chartOfAccounts,
					actualDateMode, showFundSelector, showAccountSelector, initialConfig);
			}
			
			if (startDate != null && endDate != null && endDate.isBefore(startDate))
			{
				JOptionPane.showMessageDialog(panel, "End Date cannot be before Start Date.",
					"Input Error", JOptionPane.ERROR_MESSAGE);
				return showDialog(parentComponent, title, availableFunds, chartOfAccounts,
					actualDateMode, showFundSelector, showAccountSelector, initialConfig);
			}
			
			List<String> selectedAccountIds = new ArrayList<>();
			
			if (showAccountSelector && accountListJList != null &&
				accountListJList.getSelectedValuesList() != null)
			{
				selectedAccountIds = accountListJList.getSelectedValuesList().stream()
					.map(AccountItem::getId)
					.collect(Collectors.toList());
			}
			
			ReportCriteria criteria = new ReportCriteria(startDate, endDate, selectedFundIds,
				actualDateMode, selectedAccountIds);
			
			if (result == JOptionPane.NO_OPTION)
			{ // "Save Configuration..."
				String initialName =
					(initialConfig != null && initialConfig.getUserGivenName() != null) ?
						initialConfig.getUserGivenName() : "";
				String configName = JOptionPane.showInputDialog(panel,
					"Enter a name for this report configuration:", initialName);
				
				if (configName != null && !configName.trim().isEmpty())
				{
					criteria.setNameForSaving(configName.trim());
				}
				else if (configName == null)
				{
					// User cancelled name input, effectively cancelling "Save Configuration"
					// Revert to "Run Report" behavior or re-prompt main dialog?
					// For simplicity, if name input is cancelled, we still run the report without
					// saving name.
					// If configName is empty string, it's also run without saving name.
					// To force a name or cancel entirely, more complex loop needed.
					// Current: If name is null (cancel) or empty, nameForSaving remains null.
				}
				
			}
			
			// For both YES_OPTION ("Run Report") and NO_OPTION ("Save Config" -> then run)
			return Optional.of(criteria);
		}
		
		// CANCEL_OPTION or dialog closed
		return Optional.empty();
	}
	
}
