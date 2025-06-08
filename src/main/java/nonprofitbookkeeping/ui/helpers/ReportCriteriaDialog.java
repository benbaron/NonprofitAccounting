
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


/**
 * Utility class for displaying a JavaFX dialog to gather criteria for generating reports.
 * The dialog can be configured to show selectors for date ranges (with various modes),
 * funds, and accounts, based on the needs of the specific report.
 * It returns an {@link Optional} of {@link ReportCriteria} allowing the caller to
 * handle cases where the user cancels the dialog.
 * The dialog also supports pre-populating fields from an existing {@link ReportConfiguration}
 * and prompting the user to save the chosen criteria as a new configuration.
 */
public class ReportCriteriaDialog
{
	
	/**
     * Inner class to wrap a {@link Fund} object for display in UI controls like ListView.
     * Overrides {@code toString()} to display the fund's name.
     * Implements {@code equals()} and {@code hashCode()} based on the fund's ID for proper functioning in collections.
     */
	private static class FundItem
	{
		/** The underlying Fund object. */
		private final Fund fund;
		
		/**
         * Constructs a new FundItem.
         * @param fund The {@link Fund} to wrap. Must not be null.
         * @throws NullPointerException if fund is null.
         */
		public FundItem(Fund fund)
		{
			this.fund = Objects.requireNonNull(fund, "Fund cannot be null in FundItem");
		}
		
		/**
         * Gets the underlying {@link Fund} object.
         * @return The fund.
         */
		public Fund getFund()
		{
			return this.fund;
		}
		
		/**
         * Gets the ID of the underlying fund.
         * @return The fund ID from {@link Fund#getFundId()}.
         */
		public String getId()
		{
			return this.fund.getFundId();
		}
		
		/**
         * Returns the name of the fund for display purposes.
         * @return The fund name from {@link Fund#getName()}.
         */
		@Override public String toString()
		{
			return this.fund.getName();
		}
		
		/**
         * Compares this FundItem to another object for equality.
         * Two FundItems are equal if their underlying fund IDs are equal.
         * @param o The object to compare with.
         * @return True if the objects are equal, false otherwise.
         */
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			FundItem fundItem = (FundItem) o;
			return Objects.equals(getId(), fundItem.getId());
		}
		
		/**
         * Generates a hash code for this FundItem based on its fund ID.
         * @return The hash code.
         */
		@Override public int hashCode()
		{
			return Objects.hash(getId());
		}
		
	}
	
	/**
     * Inner class to wrap an {@link Account} object for display in UI controls like ListView.
     * Overrides {@code toString()} to display the account's name and number.
     * Implements {@code equals()} and {@code hashCode()} based on the account's number for proper functioning in collections.
     */
	private static class AccountItem
	{
		/** The underlying Account object. */
		private final Account account;
		
		/**
         * Constructs a new AccountItem.
         * @param account The {@link Account} to wrap. Must not be null.
         * @throws NullPointerException if account is null.
         */
		public AccountItem(Account account)
		{
			this.account = Objects.requireNonNull(account, "Account cannot be null in AccountItem");
		}
		
		/**
         * Gets the underlying {@link Account} object.
         * @return The account.
         */
		public Account getAccount()
		{
			return this.account;
		}
		
		/**
         * Gets the account number of the underlying account.
         * @return The account number from {@link Account#getAccountNumber()}.
         */
		public String getId()
		{
			return this.account.getAccountNumber();
		}
		
		/**
         * Returns a string representation of the account, typically "Account Name (AccountNumber)".
         * @return The formatted string for display.
         */
		@Override public String toString()
		{
			return this.account.getName() + " (" + this.account.getAccountNumber() + ")";
		}
		
		/**
         * Compares this AccountItem to another object for equality.
         * Two AccountItems are equal if their underlying account numbers are equal.
         * @param o The object to compare with.
         * @return True if the objects are equal, false otherwise.
         */
		@Override public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AccountItem that = (AccountItem) o;
			return Objects.equals(this.account.getAccountNumber(), that.account.getAccountNumber());
		}
		
		/**
         * Generates a hash code for this AccountItem based on its account number.
         * @return The hash code.
         */
		@Override public int hashCode()
		{
			return Objects.hash(this.account.getAccountNumber());
		}
		
	}
	

	/**
     * Shows the report criteria dialog with options for date selection and fund selection.
     * Account selection is disabled. This is a convenience overload for
     * {@link #showDialog(Window, String, List, ChartOfAccounts, DateSelectionMode, boolean, boolean, ReportConfiguration)}.
     *
     * @param parentWindow The parent {@link Window} for the dialog.
     * @param title The title of the dialog window.
     * @param availableFunds A list of {@link Fund}s available for selection. Can be null or empty.
     * @param dateMode The {@link DateSelectionMode} to configure the date pickers.
     * @param showFundSelector If true, the fund selection UI will be displayed.
     * @return An {@link Optional} containing the {@link ReportCriteria} if the user confirms,
     *         or an empty Optional if the user cancels.
     */
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow,
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector)
	{
		return showDialog(parentWindow, title, availableFunds, null, dateMode, showFundSelector,
			false, null); // showAccountSelector is false, initialConfig is null
	}
	
	/**
     * Shows the report criteria dialog with options for date selection, fund selection,
     * and pre-populating with an initial configuration. Account selection is disabled.
     * This is a convenience overload for
     * {@link #showDialog(Window, String, List, ChartOfAccounts, DateSelectionMode, boolean, boolean, ReportConfiguration)}.
     *
     * @param parentWindow The parent {@link Window} for the dialog.
     * @param title The title of the dialog window.
     * @param availableFunds A list of {@link Fund}s available for selection. Can be null or empty.
     * @param dateMode The {@link DateSelectionMode} to configure the date pickers.
     * @param showFundSelector If true, the fund selection UI will be displayed.
     * @param initialConfig A {@link ReportConfiguration} to pre-populate dialog fields. Can be null.
     * @return An {@link Optional} containing the {@link ReportCriteria} if the user confirms,
     *         or an empty Optional if the user cancels.
     */
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow,
														String title,
														List<Fund> availableFunds,
														DateSelectionMode dateMode,
														boolean showFundSelector,
														ReportConfiguration initialConfig)
	{
		return showDialog(parentWindow, title, availableFunds, null, dateMode, showFundSelector,
			false, initialConfig); // showAccountSelector is false
	}
	
	
	/**
     * Displays a dialog for the user to input report criteria.
     * The dialog includes fields for date selection (start/end dates based on {@code dateMode}),
     * optionally a fund selector, and optionally an account selector.
     * It can be pre-populated with an {@code initialConfig}.
     * The user can run the report or save the configuration and run.
     *
     * @param parentWindow The parent {@link Window} for the dialog, used for modality.
     * @param dialogTitle The title for the dialog window.
     * @param availableFunds A list of {@link Fund}s to populate the fund selector. Can be null or empty if {@code showFundSelector} is false.
     * @param chartOfAccounts The {@link ChartOfAccounts} used to populate the account selector. Can be null if {@code showAccountSelector} is false.
     * @param dateMode The {@link DateSelectionMode} that dictates how date pickers are shown and validated.
     * @param showFundSelector If true, a multi-select list view for funds is included.
     * @param showAccountSelector If true, a multi-select list view for accounts is included.
     * @param initialConfig An optional {@link ReportConfiguration} to pre-fill the dialog's fields. Can be null.
     * @return An {@link Optional} containing the {@link ReportCriteria} gathered from the user if they confirm
     *         (by clicking "Run Report" or "Save Configuration & Run"). Returns an empty Optional if the
     *         user cancels the dialog or if name input for saving configuration is cancelled.
     *         Returns null from the result converter if validation fails (e.g., required dates missing).
     */
	public static Optional<ReportCriteria> showDialog(
														Window parentWindow,
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
     * Overloaded version of {@code showDialog}, potentially intended for use with a
     * {@link ManageReportConfigurationsDialog} as a parent or context.
     * Note: This method is currently a stub and returns null. It needs to be implemented
     * or removed if redundant. The parameter {@code manageReportConfigurationsDialog} is not a standard
     * JavaFX parent type like {@link Window}. If it's a custom dialog/component, its role as a parent
     * needs to be handled appropriately for modality and positioning if this method is implemented.
     *
     * @param manageReportConfigurationsDialog A custom dialog/component, its role as parent is unclear.
     * @param title The title of the dialog window.
     * @param availableFunds A list of {@link Fund}s available for selection.
     * @param dateSelectionMode The {@link DateSelectionMode} to configure the date pickers.
     * @param showFundSelector If true, the fund selection UI will be displayed.
     * @param selectedConfig A {@link ReportConfiguration} to pre-populate dialog fields.
     * @return Currently returns null (stub). Should return an {@link Optional<ReportCriteria>}.
     */
	public static
			Optional<ReportCriteria>
			showDialog(	ManageReportConfigurationsDialog manageReportConfigurationsDialog, // This is not a standard JavaFX parent
						String title, List<Fund> availableFunds,
						DateSelectionMode dateSelectionMode, boolean showFundSelector,
						ReportConfiguration selectedConfig)
	{
		// TODO Auto-generated method stub: Implement dialog logic or remove if this overload is not needed.
		// If this is to be used, `manageReportConfigurationsDialog` would likely need to provide a Window instance
		// to act as the parent for the new Dialog<ReportCriteria>.
		// Example: showDialog(manageReportConfigurationsDialog.getScene().getWindow(), title, ... , selectedConfig);
		return null;
	}
	
}
