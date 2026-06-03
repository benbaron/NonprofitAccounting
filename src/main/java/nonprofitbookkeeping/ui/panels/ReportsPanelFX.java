
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;
// Added for listener
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import javafx.scene.Node; // For iterating over toolbar items
import nonprofitbookkeeping.ui.UiSpacing;


/**
 * JavaFX rewrite of {@code ReportsPanel}. Lets the user generate a new report
 * (opens {@link GenerateReportPanelFX}) and shows previously generated reports
 * in a table with an **Open** button that launches the default OS application.
 */
public class ReportsPanelFX extends BorderPane
{
	
	/** Service layer for report generation and listing. */
	private final ReportService reportService;
	/** ObservableList to hold {@link ReportRow} objects for display in the table of generated reports. */
	private final ObservableList<ReportRow> rows =
		FXCollections.observableArrayList();
	/** TableView to display metadata of previously generated reports. */
	private final TableView<ReportRow> table = new TableView<>();
	
	/** The company listener. */
	private ReportsPanelCompanyListener companyListener;
	
	/** The generator tool bar. */
	private ToolBar generatorToolBar;
	
	/**
	 * Constructs a new {@code ReportsPanelFX}.
	 * Initializes the panel with a {@link ReportService} instance, a bar for generating new reports,
	 * and a table to display previously generated reports. The list of reports is refreshed upon initialization.
	 */
	public ReportsPanelFX()
	{
		this.reportService = new ReportService();
		setPadding(PanelChrome.PANEL_PADDING);
		buildTable();
		setCenter(this.table);
		
		this.generatorToolBar = buildGeneratorBarInternal(); // Call internal
																// method
		setTop(PanelChrome.topSection("Reports", this.generatorToolBar));
		
		this.companyListener = new ReportsPanelCompanyListener(this);
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
		
		handleCompanyChange(CurrentCompany.isOpen());
		
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and returns a {@link ToolBar} for initiating the generation of new reports.
	 * The toolbar includes a {@link ComboBox} to select the report type,
	 * {@link DatePicker} controls for selecting "From" and "To" dates (though these are not directly used
	 * by the Generate button's current action, which opens a new panel), and a "Generate" button.
	 * Clicking "Generate" opens a new {@link GenerateReportPanelFX} in a dialog window.
	 *
	 * @return A configured {@link ToolBar} for report generation controls.
	 */
	private ToolBar buildGeneratorBarInternal()
	{
		ComboBox<String> typeBox = new ComboBox<>();
		typeBox.getItems().addAll("Income Statement",
			"Balance Sheet",
			"Cash Flow",
			"Donor Summary",
			"Fund Activity Report");
		
		DatePicker from = new DatePicker(LocalDate.now().withDayOfYear(1));
		DatePicker to = new DatePicker(LocalDate.now());
		Button gen = new Button("Generate");
		gen.setOnAction(e -> {
			// This action itself should ideally be disabled if no company is
			// open.
			// The GenerateReportPanelFX might also need its own company checks.
			Stage dlg = new Stage();
			dlg.setTitle("Generating Report");
			// Ensure GenerateReportPanelFX handles cases where no company is
			// open if it's
			// possible to reach here.
			dlg.setScene(new Scene(
				new GenerateReportPanelFX(this.reportService), 600, 400));
			dlg.showAndWait();
			
			if (CurrentCompany.isOpen())
			{ // Only refresh if a company is still open
				refresh();
			}
			
		});
		Button workbookReports = new Button("Workbook Reports");
		workbookReports.setOnAction(e -> openWorkbookReportsDialog());
		return new ToolBar(new Label("Type:"), typeBox, new Label("From:"),
			from, new Label("To:"),
			to, gen, new Separator(), workbookReports);
		
	}
	
	/**
	 * Opens the semantic JSON workbook-report preview panel.
	 */
	private void openWorkbookReportsDialog()
	{
		Stage dlg = new Stage();
		dlg.setTitle("Workbook Reports");
		dlg.setScene(new Scene(new SemanticReportPreviewPanelFX(), 1100, 760));
		dlg.showAndWait();
	}
	
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying metadata of generated reports.
	 * It defines columns for Report Name, Creation Date, File Path, and an "Open" button column.
	 * The "Open" button in each row attempts to open the report file using the system's default application.
	 * The table is bound to the {@link #rows} observable list.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * (used in the {@code col} helper) uses reflection and can lead to type safety warnings, and for generic
	 * types with {@code TableColumn}.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	private void buildTable()
	{
		TableColumn<ReportRow, String> nameCol = col("Report", "name");
		TableColumn<ReportRow, String> dateCol = col("Created", "date");
		TableColumn<ReportRow, String> pathCol = col("File", "path");
		TableColumn<ReportRow, Void> openCol = new TableColumn<>("Open");
		openCol.setCellFactory(tc -> new TableCell<>()
		{
			private final Button btn = new Button("Open");
			{
				this.btn.setOnAction(e -> {
					ReportRow r = getTableView().getItems().get(getIndex());
					
					try
					{
						java.awt.Desktop.getDesktop().open(new File(r.path));
					}
					catch (Exception ex)
					{
						new Alert(Alert.AlertType.ERROR,
							"Cannot open file: " + ex.getMessage())
							.showAndWait();
					}
					
				});
			}
			
			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : this.btn);
				
			}
			
		});
		this.table.getColumns().addAll(nameCol, dateCol, pathCol, openCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
		
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for displaying String properties in the reports table.
	 *
	 * @param t The title of the column for the table header.
	 * @param p The name of the property in {@link ReportRow} to bind this column to (e.g., "name" for getName()).
	 * @return A configured {@link TableColumn} for displaying String data from a {@link ReportRow}.
	 */
	private static TableColumn<ReportRow, String> col(String t, String p)
	{
		TableColumn<ReportRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
		
	}
	
	/**
	 * Refreshes the data displayed in the reports {@link #table}.
	 * It clears any existing rows, fetches the current list of generated report metadata
	 * from the {@link #reportService}, converts each {@link ReportMetadata} into a {@link ReportRow},
	 * and adds them to the {@link #rows} observable list, which updates the table view.
	 */
	private void refresh()
	{
		this.rows.clear();
		
		// Assuming reportService.listGeneratedReports() is company-aware
		// or returns empty list if no company.
		if (CurrentCompany.isOpen())
		{
			List<ReportMetadata> list = ReportService.listGeneratedReports();
			
			if (list != null)
			{ // Guard against null list from service
				list.forEach(r -> this.rows.add(new ReportRow(r)));
			}
			
		}
		
	}
	
	/**
	 * Handle company change.
	 *
	 * @param isOpen the is open
	 */
	private void handleCompanyChange(boolean isOpen)
	{
		
		// Disable/Enable all items in the generatorToolBar
		if (this.generatorToolBar != null)
		{
			
			for (Node node : this.generatorToolBar.getItems())
			{
				// Check for specific types if needed, e.g., ComboBox,
				// DatePicker, Button
				node.setDisable(!isOpen);
			}
			
		}
		
		// Specifically handle ComboBox selection placeholder if needed
		ComboBox<String> typeBox = null;
		
		if (this.generatorToolBar != null &&
			this.generatorToolBar.getItems().size() > 1 &&
			this.generatorToolBar.getItems().get(1) instanceof ComboBox)
		{
			@SuppressWarnings("unchecked")
			ComboBox<String> tempCb =
				(ComboBox<String>) this.generatorToolBar.getItems().get(1);
			typeBox = tempCb;
		}
		
		if (isOpen)
		{
			
			if (typeBox != null && !typeBox.getItems().isEmpty())
			{
				typeBox.getSelectionModel().selectFirst();
			}
			
			refresh();
		}
		else
		{
			this.rows.clear();
			
			if (typeBox != null)
			{
				typeBox.getSelectionModel().clearSelection();
				// typeBox.setPlaceholder(new Label("No company")); //
				// Placeholder if desired
			}
			
		}
		
	}
	
	/**
	 * The listener interface for receiving reportsPanelCompany events.
	 * The class that is interested in processing a reportsPanelCompany
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addReportsPanelCompanyListener</code> method. When
	 * the reportsPanelCompany event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ReportsPanelCompanyEvent
	 */
	private class ReportsPanelCompanyListener implements CompanyChangeListener
	{
		
		/** The panel. */
		private ReportsPanelFX panel;
		
		/**
		 * Instantiates a new reports panel company listener.
		 *
		 * @param panel the panel
		 */
		public ReportsPanelCompanyListener(ReportsPanelFX panel)
		{
			this.panel = panel;
			
		}
		
		/**
		 * Override @see nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener#companyChange(boolean) 
		 */
		@Override
		public void companyChange(boolean isOpen)
		{
			this.panel.handleCompanyChange(isOpen);
			
		}
		
	}
	
	/** 
	 * It wraps {@link ReportMetadata} for easy display with {@link PropertyValueFactory}.
	 */
	public static class ReportRow
	{
		
		/** The name. */
		final String name;
		
		/** The date. */
		final String date;
		
		/** The path. */
		final String path;
		
		/**
		 * Instantiates a new report row.
		 *
		 * @param m the m
		 */
		ReportRow(ReportMetadata m)
		{
			this.name = m.getReportName();
			this.date = m.getCreated();
			this.path = m.getFilePath();
			
		}
		
		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName()
		{
			return this.name;
			
		}
		
		/**
		 * Gets the date.
		 *
		 * @return the date
		 */
		public String getDate()
		{
			return this.date;
			
		}
		
		/**
		 * Gets the path.
		 *
		 * @return the path
		 */
		public String getPath()
		{
			return this.path;
			
		}
		
	}
	
}
