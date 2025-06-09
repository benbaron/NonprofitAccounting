
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;

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
	private final ObservableList<ReportRow> rows = FXCollections.observableArrayList();
	/** TableView to display metadata of previously generated reports. */
	private final TableView<ReportRow> table = new TableView<>();
	
	/**
	 * Constructs a new {@code ReportsPanelFX}.
	 * Initializes the panel with a {@link ReportService} instance, a bar for generating new reports,
	 * and a table to display previously generated reports. The list of reports is refreshed upon initialization.
	 */
	public ReportsPanelFX()
	{
		this.reportService = new ReportService();
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setTop(buildGeneratorBar());
		refresh();
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
	private ToolBar buildGeneratorBar()
	{
		ComboBox<String> typeBox = new ComboBox<>();
		typeBox.getItems().addAll("Income Statement", "Balance Sheet", "Cash Flow", "Donor Summary",
			"Fund Activity Report");
		typeBox.getSelectionModel().selectFirst();
		DatePicker from = new DatePicker(LocalDate.now().withDayOfYear(1));
		DatePicker to = new DatePicker(LocalDate.now());
		Button gen = new Button("Generate");
		gen.setOnAction(e -> {
			Stage dlg = new Stage();
			dlg.setTitle("Generating Report");
			dlg.setScene(new Scene(new GenerateReportPanelFX(this.reportService), 600, 400));
			dlg.showAndWait();
			refresh();
		});
		return new ToolBar(new Label("Type:"), typeBox, new Label("From:"), from, new Label("To:"),
			to, gen);
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
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
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
						new Alert(Alert.AlertType.ERROR, "Cannot open file: " + ex.getMessage())
							.showAndWait();
					}
					
				});
			}
			
			@Override protected void updateItem(Void item, boolean empty)
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
		List<ReportMetadata> list = this.reportService.listGeneratedReports();
		list.forEach(r -> this.rows.add(new ReportRow(r)));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * A simple data class (POJO) used to represent a row in the generated reports {@link TableView}.
	 * It wraps {@link ReportMetadata} for easy display with {@link PropertyValueFactory}.
	 */
	public static class ReportRow
	{
		/** The name of the report. */
		final String name;
		/** The creation date of the report, as a String. */
		final String date;
		/** The file path where the report is stored. */
		final String path;
		
		/**
		 * Constructs a {@code ReportRow} from {@link ReportMetadata}.
		 *
		 * @param m The {@link ReportMetadata} object containing details of a generated report. Must not be null.
		 */
		ReportRow(ReportMetadata m)
		{
			this.name = m.getReportName();
			this.date = m.getCreated();
			this.path = m.getFilePath();
		}
		
		/**
		 * Gets the name of the report.
		 * @return The report's name.
		 */
		public String getName()
		{
			return this.name;
		}
		
		/**
		 * Gets the creation date of the report.
		 * @return The creation date as a String.
		 */
		public String getDate()
		{
			return this.date;
		}
		
		/**
		 * Gets the file path of the generated report.
		 * @return The file path string.
		 */
		public String getPath()
		{
			return this.path;
		}
		
	}
	
}
