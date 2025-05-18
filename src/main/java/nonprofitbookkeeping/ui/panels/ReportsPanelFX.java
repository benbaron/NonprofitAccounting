
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
	
	private final ReportService reportService;
	private final ObservableList<ReportRow> rows = FXCollections.observableArrayList();
	private final TableView<ReportRow> table = new TableView<>();
	
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
	
	private static TableColumn<ReportRow, String> col(String t, String p)
	{
		TableColumn<ReportRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	private void refresh()
	{
		this.rows.clear();
		List<ReportMetadata> list = this.reportService.listGeneratedReports();
		list.forEach(r -> this.rows.add(new ReportRow(r)));
	}
	
	/* ------------------------------------------------------------------ */
	public static class ReportRow
	{
		final String name, date, path;
		
		ReportRow(ReportMetadata m)
		{
			this.name = m.getReportName();
			this.date = m.getCreated();
			this.path = m.getFilePath();
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public String getDate()
		{
			return this.date;
		}
		
		public String getPath()
		{
			return this.path;
		}
		
	}
	
}
