
package nonprofitbookkeeping.ui.helpers;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.reports.ReportCriteria;
import nonprofitbookkeeping.ui.panels.ManageReportConfigurationsDialog;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors; // Added
import javafx.scene.control.TextInputDialog; // Added
import javafx.scene.control.ButtonBar; // Added
// Swing imports to be removed
// import javax.swing.*;
// import org.jdatepicker.UtilDateModel;
// import java.awt.*;
// import java.awt.event.ItemEvent;
// import java.time.ZoneId;
// import java.util.Calendar;
// import java.util.Properties;


public class ReportCriteriaDialog
{
	
	// FundItem and AccountItem classes remain mostly the same,
	// ensure they are compatible with JavaFX ListView if needed for custom cell
	// factories.
	// For now, assuming standard ListView<String> or ListView<FundItem/AccountItem>
	// with toString()
	private static class FundItem
	{
		private final Fund fund;
		
		public FundItem(Fund fund)
		{
			this.fund = fund;
		}
		
		public Fund getFund()
		{
			return this.fund;
		}
		
		public String getId()
		{
			return this.fund.getFundId();
		}
		
		@Override public String toString()
		{
			return this.fund.getName();
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
	
	private static class AccountItem
	{
		private final Account account;
		
		public AccountItem(Account account)
		{
			this.account = account;
		}
		
		public Account getAccount()
		{
			return this.account;
		}
		
		public String getId()
		{
			return this.account.getAccountNumber();
		}
		
		@Override public String toString()
		{
			return this.account.getName() + " (" + this.account.getAccountNumber() + ")";
		}
		
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(this.account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		@Override public int hashCode()
		{
			return Objects.hash(this.account.getAccountNumber());
		}
		
	}
	

	// Overloaded public methods to maintain API compatibility for now
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow, // Changed from
																				// Component to
																				// Window
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector)
	{
		return showDialog(parentWindow, title, availableFunds, null, dateMode, showFundSelector,
			false, null);
	}
	
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow, // Changed from
																				// Component to
																				// Window
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector,
														ReportConfiguration initialConfig)
	{
		return showDialog(parentWindow, title, availableFunds, null, dateMode, showFundSelector,
			false, initialConfig);
	}
	
	
	// Main dialog logic
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow, // Changed from
																				// Component to
																				// Window
														String dialogTitle,
														List<Fund> availableFunds,
														ChartOfAccounts chartOfAccounts,
														DateSelectionMode dateMode,
														boolean showFundSelector,
														boolean showAccountSelector,
														ReportConfiguration initialConfig)
	{
		
		Dialog<ReportCriteria> dialog = new Dialog<>();
		dialog.initOwner(parentWindow);
		dialog.setTitle(dialogTitle);
		
		DialogPane dialogPane = dialog.getDialogPane();
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		final DateSelectionMode actualDateMode =
			(initialConfig != null && initialConfig.getDateSelectionMode() != null) ?
				initialConfig.getDateSelectionMode() : dateMode;
		
		DatePicker startDatePicker = new DatePicker();
		DatePicker endDatePicker = new DatePicker();
		
		if (initialConfig != null && initialConfig.getSpecificStartDate() != null)
		{
			startDatePicker.setValue(initialConfig.getSpecificStartDate());
		}
		
		if (initialConfig != null && initialConfig.getSpecificEndDate() != null)
		{
			endDatePicker.setValue(initialConfig.getSpecificEndDate());
		}
		
		int rowIndex = 0;
		
		if (actualDateMode == DateSelectionMode.DATE_RANGE_MANDATORY_START ||
			actualDateMode == DateSelectionMode.DATE_RANGE_OPTIONAL_START)
		{
			grid.add(new Label("Start Date:"), 0, rowIndex);
			grid.add(startDatePicker, 1, rowIndex++);
		}
		
		String endDateLabelText =
			(actualDateMode == DateSelectionMode.SINGLE_DATE) ? "Date:" : "End Date:";
		grid.add(new Label(endDateLabelText), 0, rowIndex);
		grid.add(endDatePicker, 1, rowIndex++);
		
		
		// --- Fund Selector (Placeholder for now) ---
		if (showFundSelector)
		{
			grid.add(new Separator(), 0, rowIndex++, 2, 1);
			grid.add(new Label("Fund Selection:"), 0, rowIndex++, 2, 1);
			
			CheckBox selectAllFundsCheckbox = new CheckBox("Select All Funds");
			ListView<FundItem> fundListView = new ListView<>();
			fundListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			
			boolean initiallySelectAllFunds = true;
			List<String> initialFundIds = null;
			
			if (initialConfig != null && initialConfig.getFundIds() != null &&
				!initialConfig.getFundIds().isEmpty())
			{
				initiallySelectAllFunds = false;
				initialFundIds = initialConfig.getFundIds();
			}
			
			selectAllFundsCheckbox.setSelected(initiallySelectAllFunds);
			fundListView.setDisable(initiallySelectAllFunds);
			
			if (availableFunds != null)
			{
				
				for (Fund fund : availableFunds)
				{
					fundListView.getItems().add(new FundItem(fund));
				}
				
			}
			
			if (!initiallySelectAllFunds && initialFundIds != null)
			{
				
				for (FundItem item : fundListView.getItems())
				{
					
					if (initialFundIds.contains(item.getId()))
					{
						fundListView.getSelectionModel().select(item);
					}
					
				}
				
			}
			
			selectAllFundsCheckbox.selectedProperty()
				.addListener((obs, wasSelected, isNowSelected) ->
				{
					fundListView.setDisable(isNowSelected);
					
					if (isNowSelected)
					{
						fundListView.getSelectionModel().clearSelection();
					}
					
				});
				
			ScrollPane fundScrollPane = new ScrollPane(fundListView);
			fundScrollPane.setFitToWidth(true);
			fundScrollPane.setPrefHeight(100); // Similar to Swing's preferred size
			
			grid.add(selectAllFundsCheckbox, 0, rowIndex++, 2, 1);
			grid.add(fundScrollPane, 0, rowIndex++, 2, 1);
		}
		
		// --- Account Selector ---
		if (showAccountSelector)
		{
			grid.add(new Separator(), 0, rowIndex++, 2, 1);
			grid.add(new Label("Select Accounts (for Detail Reports):"), 0, rowIndex++, 2, 1);
			
			ListView<AccountItem> accountListView = new ListView<>();
			accountListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			
			if (chartOfAccounts != null && chartOfAccounts.getAccounts() != null)
			{
				List<Account> accounts = new ArrayList<>(chartOfAccounts.getAccounts());
				accounts.sort(java.util.Comparator.comparing(Account::getName)); // Sort by name
				
				for (Account account : accounts)
				{
					accountListView.getItems().add(new AccountItem(account));
				}
				
			}
			
			if (initialConfig != null && initialConfig.getAccountIdsForDetailReport() != null &&
				!initialConfig.getAccountIdsForDetailReport().isEmpty())
			{
				List<String> initialAccountIds = initialConfig.getAccountIdsForDetailReport();
				
				for (AccountItem item : accountListView.getItems())
				{
					
					if (initialAccountIds.contains(item.getId()))
					{
						accountListView.getSelectionModel().select(item);
					}
					
				}
				
			}
			
			ScrollPane accountScrollPane = new ScrollPane(accountListView);
			accountScrollPane.setFitToWidth(true);
			accountScrollPane.setPrefHeight(150); // Set preferred height for account list
			
			grid.add(accountScrollPane, 0, rowIndex++, 2, 1);
		}
		
		dialogPane.setContent(grid);
		
		// --- Buttons ---
		ButtonType runButtonType = new ButtonType("Run Report", ButtonBar.ButtonData.OK_DONE);
		ButtonType saveRunButtonType =
			new ButtonType("Save Configuration & Run", ButtonBar.ButtonData.OTHER);
		dialogPane.getButtonTypes().addAll(runButtonType, saveRunButtonType, ButtonType.CANCEL);
		
		// Store references to controls that will be needed in the result converter
		final DatePicker finalStartDatePicker = startDatePicker;
		final DatePicker finalEndDatePicker = endDatePicker;
		
		// Note: Retrieving controls from the grid like this can be fragile.
		// If the layout changes significantly, these retrievals might break.
		// For more complex dialogs, consider a dedicated controller class or builder
		// pattern.
		final CheckBox finalSelectAllFundsCheckbox =
			showFundSelector ? (CheckBox) grid.getChildren().stream()
				.filter(node -> node instanceof CheckBox &&
					"Select All Funds".equals(((CheckBox) node).getText()))
				.findFirst().orElse(null) : null;
		final ListView<FundItem> finalFundListView =
			showFundSelector ? (ListView<FundItem>) ((ScrollPane) grid.getChildren().stream()
				.filter(node -> node instanceof ScrollPane).findFirst()
				.orElse(new ScrollPane(new ListView<>()))).getContent() : null;
		final ListView<AccountItem> finalAccountListView =
			showAccountSelector ? (ListView<AccountItem>) ((ScrollPane) grid.getChildren().stream()
				.filter(node -> node instanceof ScrollPane).skip(showFundSelector ? 1 : 0)
				.findFirst().orElse(new ScrollPane(new ListView<>()))).getContent() : null;
		
		
		dialog.setResultConverter(dialogButton -> {
			
			if (dialogButton == runButtonType || dialogButton == saveRunButtonType)
			{
				LocalDate startDate = null;
				
				if (finalStartDatePicker.isVisible())
				{
					startDate = finalStartDatePicker.getValue();
				}
				
				LocalDate endDate = finalEndDatePicker.getValue();
				
				// Date Validation
				if (actualDateMode == DateSelectionMode.SINGLE_DATE && endDate == null)
				{
					AlertBox.showError(parentWindow, "Date is required.");
					return null; // Prevents dialog from closing / or re-triggers if validation
									// logic is more complex
				}
				
				if (actualDateMode == DateSelectionMode.DATE_RANGE_MANDATORY_START &&
					(startDate == null || endDate == null))
				{
					AlertBox.showError(parentWindow, "Both Start Date and End Date are required.");
					return null;
				}
				
				if (actualDateMode == DateSelectionMode.DATE_RANGE_OPTIONAL_START &&
					endDate == null)
				{ // Only end date is strictly mandatory here
					AlertBox.showError(parentWindow, "End Date is required.");
					return null;
				}
				
				if (startDate != null && endDate != null && endDate.isBefore(startDate))
				{
					AlertBox.showError(parentWindow, "End Date cannot be before Start Date.");
					return null;
				}
				
				List<String> selectedFundIds = new ArrayList<>();
				
				if (showFundSelector && finalSelectAllFundsCheckbox != null &&
					!finalSelectAllFundsCheckbox.isSelected() && finalFundListView != null)
				{
					selectedFundIds =
						finalFundListView.getSelectionModel().getSelectedItems().stream()
							.map(FundItem::getId)
							.collect(Collectors.toList());
				}
				
				List<String> selectedAccountIds = new ArrayList<>();
				
				if (showAccountSelector && finalAccountListView != null)
				{
					selectedAccountIds =
						finalAccountListView.getSelectionModel().getSelectedItems().stream()
							.map(AccountItem::getId)
							.collect(Collectors.toList());
				}
				
				ReportCriteria criteria = new ReportCriteria(startDate, endDate, selectedFundIds,
					actualDateMode, selectedAccountIds);
				
				if (dialogButton == saveRunButtonType)
				{
					TextInputDialog nameDialog = new TextInputDialog(
						initialConfig != null ? initialConfig.getUserGivenName() : "");
					nameDialog.initOwner(parentWindow);
					nameDialog.setTitle("Save Configuration");
					nameDialog.setHeaderText("Enter a name for this report configuration:");
					nameDialog.setContentText("Name:");
					Optional<String> nameResult = nameDialog.showAndWait();
					
					if (nameResult.isPresent() && !nameResult.get().trim().isEmpty())
					{
						criteria.setNameForSaving(nameResult.get().trim());
					}
					else if (!nameResult.isPresent())
					{ // User cancelled the name input
						return null; // Cancel the whole operation
					}
					
				}
				
				return criteria;
			}
			
			return null; // Cancel or close button
		});
		
		return dialog.showAndWait();
	}
	
	/**
	 * @param manageReportConfigurationsDialog
	 * @param title
	 * @param availableFunds
	 * @param dateSelectionMode
	 * @param showFundSelector
	 * @param selectedConfig
	 * @return
	 */
	public static
			Optional<ReportCriteria>
			showDialog(	ManageReportConfigurationsDialog manageReportConfigurationsDialog,
						String title, List<Fund> availableFunds,
						DateSelectionMode dateSelectionMode, boolean showFundSelector,
						ReportConfiguration selectedConfig)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
