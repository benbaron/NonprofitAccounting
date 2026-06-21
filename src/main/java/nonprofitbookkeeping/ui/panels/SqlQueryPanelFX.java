
package nonprofitbookkeeping.ui.panels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * The Class SqlQueryPanelFX.
 */
public class SqlQueryPanelFX extends BorderPane
{
	
	/** The Constant OPERATORS. */
	private static final List<String> OPERATORS = List.of(
		"=",
		"!=",
		">",
		">=",
		"<",
		"<=",
		"LIKE",
		"IS NULL",
		"IS NOT NULL"
	);
	
	/** The table box. */
	private final ComboBox<String> tableBox = new ComboBox<>();
	
	/** The field box. */
	private final ComboBox<String> fieldBox = new ComboBox<>();
	
	/** The operator box. */
	private final ComboBox<String> operatorBox = new ComboBox<>();
	
	/** The value field. */
	private final TextField valueField = new TextField();
	
	/** The query preview. */
	private final TextArea queryPreview = new TextArea();
	
	/** The run button. */
	private final Button runButton = new Button("Run Query");
	
	/** The refresh button. */
	private final Button refreshButton = new Button("Refresh Schema");
	
	/** The progress indicator. */
	private final ProgressIndicator progressIndicator = new ProgressIndicator();
	
	/** The column types. */
	private final Map<String, Integer> columnTypes = new HashMap<>();
	
	/** The suppress insert. */
	private boolean suppressInsert = false;
	
	/**
	 * Instantiates a new sql query panel FX.
	 */
	public SqlQueryPanelFX()
	{
		setPadding(new Insets(12));
		this.progressIndicator.setMaxSize(24, 24);
		this.progressIndicator.setVisible(false);
		this.queryPreview.setEditable(true);
		this.queryPreview.setPrefRowCount(3);
		this.queryPreview.setText("SELECT *\nFROM ");
		this.operatorBox.setItems(FXCollections.observableArrayList(OPERATORS));
		this.operatorBox.getSelectionModel().selectFirst();
		
		Button insertValueButton = new Button("Insert Value");
		insertValueButton.setOnAction(e -> insertValue());
		Button insertDateButton = new Button("Insert Date...");
		insertDateButton.setOnAction(e -> showDatePicker());
		HBox valueControls = new HBox(8, this.valueField, insertValueButton,
			insertDateButton);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.add(new Label("Table"), 0, 0);
		grid.add(this.tableBox, 1, 0);
		grid.add(this.refreshButton, 2, 0);
		grid.add(new Label("Field"), 0, 1);
		grid.add(this.fieldBox, 1, 1);
		grid.add(new Label("Operator"), 0, 2);
		grid.add(this.operatorBox, 1, 2);
		grid.add(new Label("Value"), 0, 3);
		grid.add(valueControls, 1, 3);
		
		HBox actions = new HBox(10, this.runButton, this.progressIndicator);
		actions.setPadding(new Insets(10, 0, 0, 0));
		
		VBox form = new VBox(12, grid, new Label("Query Preview"),
			this.queryPreview, actions);
		VBox.setVgrow(this.queryPreview, Priority.NEVER);
		setCenter(form);
		
		this.tableBox.setOnAction(e -> {
			loadColumns();
			insertTable();
		});
		this.valueField.setOnAction(e -> insertValue());
		this.fieldBox.setOnAction(e -> insertField());
		this.operatorBox.setOnAction(e -> {
			updateOperatorState();
			insertOperator();
		});
		this.refreshButton.setOnAction(e -> loadTables());
		this.runButton.setOnAction(e -> runQuery());
		
		loadTables();
		updateOperatorState();
		
	}
	
	/**
	 * Load tables.
	 */
	private void loadTables()
	{
		this.tableBox.getItems().clear();
		this.fieldBox.getItems().clear();
		this.columnTypes.clear();
		
		if (!Database.isInitialized())
		{
			setDisabledState(true);
			this.queryPreview.setText("Database not initialized.");
			return;
		}
		
		try (Connection connection = Database.get().getConnection())
		{
			DatabaseMetaData meta = connection.getMetaData();
			
			try (ResultSet rs = meta.getTables(null, null, "%",
				new String[]
				{ "TABLE" }))
			{
				List<String> tables = new ArrayList<>();
				
				while (rs.next())
				{
					String schema = rs.getString("TABLE_SCHEM");
					
					if (schema != null && !"PUBLIC".equalsIgnoreCase(schema))
					{
						continue;
					}
					
					String name = rs.getString("TABLE_NAME");
					
					if (name != null)
					{
						tables.add(name);
					}
					
				}
				
				tables.sort(Comparator.naturalOrder());
				this.tableBox.getItems().setAll(tables);
				
				if (!tables.isEmpty())
				{
					this.suppressInsert = true;
					this.tableBox.getSelectionModel().selectFirst();
					this.suppressInsert = false;
				}
				
			}
			
			setDisabledState(false);
		}
		catch (SQLException ex)
		{
			setDisabledState(true);
			AlertBox.showError(getOwnerWindow(),
				"Unable to load database tables.");
		}
		
	}
	
	/**
	 * Load columns.
	 */
	private void loadColumns()
	{
		this.fieldBox.getItems().clear();
		this.columnTypes.clear();
		String table = this.tableBox.getValue();
		
		if (table == null || table.isBlank())
		{
			return;
		}
		
		try (Connection connection = Database.get().getConnection())
		{
			DatabaseMetaData meta = connection.getMetaData();
			
			try (ResultSet rs = meta.getColumns(null, null, table, "%"))
			{
				List<String> fields = new ArrayList<>();
				
				while (rs.next())
				{
					String name = rs.getString("COLUMN_NAME");
					int type = rs.getInt("DATA_TYPE");
					
					if (name != null)
					{
						fields.add(name);
						this.columnTypes.put(name, type);
					}
					
				}
				
				fields.sort(Comparator.naturalOrder());
				this.fieldBox.getItems().setAll(fields);
				
				if (!fields.isEmpty())
				{
					this.suppressInsert = true;
					this.fieldBox.getSelectionModel().selectFirst();
					this.suppressInsert = false;
				}
				
			}
			
		}
		catch (SQLException ex)
		{
			AlertBox.showError(getOwnerWindow(),
				"Unable to load fields for table: " + table);
		}
		
	}
	
	/**
	 * Insert table.
	 */
	private void insertTable()
	{
		insertToken(this.tableBox.getValue());
		
	}
	
	/**
	 * Insert field.
	 */
	private void insertField()
	{
		insertToken(this.fieldBox.getValue());
		
	}
	
	/**
	 * Insert operator.
	 */
	private void insertOperator()
	{
		String operator = this.operatorBox.getValue();
		
		if (operator == null || operator.isBlank())
		{
			return;
		}
		
		insertToken(" " + operator + " ");
		
	}
	
	/**
	 * Insert value.
	 */
	private void insertValue()
	{
		String value = this.valueField.getText();
		
		if (value == null || value.isBlank())
		{
			return;
		}
		
		insertToken(value);
		
	}
	
	/**
	 * Insert token.
	 *
	 * @param token the token
	 */
	private void insertToken(String token)
	{
		
		if (this.suppressInsert || token == null || token.isBlank())
		{
			return;
		}
		
		int caret = this.queryPreview.getCaretPosition();
		this.queryPreview.insertText(caret, token);
		this.queryPreview.requestFocus();
		this.queryPreview.positionCaret(caret + token.length());
		
	}
	
	/**
	 * Update operator state.
	 */
	private void updateOperatorState()
	{
		String operator = this.operatorBox.getValue();
		boolean needsValue = operatorNeedsValue(operator);
		this.valueField.setDisable(!needsValue);
		
		if (!needsValue)
		{
			this.valueField.clear();
		}
		
	}
	
	/**
	 * Run query.
	 */
	private void runQuery()
	{
		
		if (!Database.isInitialized())
		{
			AlertBox.showWarning(getOwnerWindow(), "Database not initialized.");
			return;
		}
		
		String rawSql = this.queryPreview.getText();
		SqlSelection selection = buildSelectionSql(rawSql);
		
		if (selection == null)
		{
			return;
		}
		
		Task<QueryResult> task = new Task<>()
		{
			@Override
			protected QueryResult call() throws Exception
			{
				
				try (Connection connection = Database.get().getConnection();
					PreparedStatement ps =
						connection.prepareStatement(selection.sql()))
				{
					
					if (selection.hasParameter())
					{
						setParameter(ps, 1, selection.parameterType(),
							selection.parameterValue());
					}
					
					try (ResultSet rs = ps.executeQuery())
					{
						return toQueryResult(rs);
					}
					
				}
				
			}
			
		};
		
		this.runButton.setDisable(true);
		this.progressIndicator.setVisible(true);
		task.setOnSucceeded(e -> {
			this.runButton.setDisable(false);
			this.progressIndicator.setVisible(false);
			QueryResult result = task.getValue();
			showResults(result, selection.sql());
		});
		task.setOnFailed(e -> {
			this.runButton.setDisable(false);
			this.progressIndicator.setVisible(false);
			Throwable ex = task.getException();
			AlertBox.showError(getOwnerWindow(),
				"Query failed: " +
					(ex != null ? ex.getMessage() : "Unknown error"));
		});
		
		Thread thread = new Thread(task, "sql-query-task");
		thread.setDaemon(true);
		thread.start();
		
	}
	
	/**
	 * Builds the selection sql.
	 *
	 * @param rawSql the raw sql
	 * @return the sql selection
	 */
	private SqlSelection buildSelectionSql(String rawSql)
	{
		
		if (rawSql != null && !rawSql.isBlank())
		{
			return new SqlSelection(rawSql.trim(), false, null, null);
		}
		
		String table = this.tableBox.getValue();
		
		if (table == null || table.isBlank())
		{
			AlertBox.showWarning(getOwnerWindow(), "Select a table to query.");
			return null;
		}
		
		String field = this.fieldBox.getValue();
		String operator = this.operatorBox.getValue();
		boolean includeWhere = field != null && !field.isBlank() &&
			operator != null && !operator.isBlank();
		boolean needsValue = includeWhere && operatorNeedsValue(operator);
		String value = this.valueField.getText();
		
		if (needsValue && (value == null || value.isBlank()))
		{
			AlertBox.showWarning(getOwnerWindow(),
				"Enter a value for the filter.");
			return null;
		}
		
		StringBuilder sql = new StringBuilder("SELECT * FROM ");
		sql.append(table);
		Integer paramType = null;
		
		if (includeWhere)
		{
			sql.append(" WHERE ").append(field).append(" ").append(operator);
			
			if (needsValue)
			{
				sql.append(" ?");
				paramType = this.columnTypes.getOrDefault(field, Types.VARCHAR);
			}
			
		}
		
		return new SqlSelection(sql.toString(), needsValue, paramType, value);
		
	}
	
	/**
	 * Show date picker.
	 */
	private void showDatePicker()
	{
		Dialog<LocalDate> dialog = new Dialog<>();
		dialog.setTitle("Select Date");
		DatePicker picker = new DatePicker(LocalDate.now());
		DialogPane pane = dialog.getDialogPane();
		pane.setContent(picker);
		pane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.setResultConverter(
			button -> button == ButtonType.OK ? picker.getValue() : null);
		
		dialog.showAndWait().ifPresent(date -> {
			String formatted = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
			insertToken("DATE '" + formatted + "'");
		});
		
	}
	
	/**
	 * Show results.
	 *
	 * @param result the result
	 * @param sql the sql
	 */
	private void showResults(QueryResult result, String sql)
	{
		TableView<Map<String, Object>> tableView = new TableView<>();
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		for (String column : result.columns)
		{
			TableColumn<Map<String, Object>, String> col =
				new TableColumn<>(column);
			col.setCellValueFactory(cell -> {
				Object value = cell.getValue().get(column);
				return new SimpleStringProperty(value == null ? "" :
					Objects.toString(value));
			});
			tableView.getColumns().add(col);
		}
		
		tableView.setItems(FXCollections.observableArrayList(result.rows));
		
		Label sqlLabel = new Label("SQL: " + sql);
		Button exportCsv = new Button("Export CSV");
		Button exportTxt = new Button("Export TXT");
		Button exportXlsx = new Button("Export XLSX");
		exportCsv.setOnAction(e -> exportResults(result, ExportFormat.CSV));
		exportTxt.setOnAction(e -> exportResults(result, ExportFormat.TXT));
		exportXlsx.setOnAction(e -> exportResults(result, ExportFormat.XLSX));
		
		HBox exportBar = new HBox(10, exportCsv, exportTxt, exportXlsx);
		exportBar.setPadding(new Insets(10));
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox header = new HBox(10, sqlLabel, spacer);
		header.setPadding(new Insets(10, 10, 0, 10));
		
		BorderPane root = new BorderPane(tableView);
		root.setTop(header);
		root.setBottom(exportBar);
		
		Stage stage = new Stage();
		stage.setTitle("Query Results");
		stage.initOwner(getOwnerWindow());
		Scene scene = new Scene(root, 900, 600);
		stage.setScene(scene);
		stage.show();
		
	}
	
	/**
	 * Export results.
	 *
	 * @param result the result
	 * @param format the format
	 */
	private void exportResults(QueryResult result, ExportFormat format)
	{
		Window owner = getOwnerWindow();
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export Results");
		FileChooser.ExtensionFilter filter = switch(format)
		{
			case CSV -> new FileChooser.ExtensionFilter("CSV files (*.csv)",
				"*.csv");
			case TXT -> new FileChooser.ExtensionFilter("Text files (*.txt)",
				"*.txt");
			case XLSX -> new FileChooser.ExtensionFilter("Excel files (*.xlsx)",
				"*.xlsx");
		};
		chooser.getExtensionFilters().add(filter);
		chooser.setSelectedExtensionFilter(filter);
		String baseName = "query_results." + format.extension;
		chooser.setInitialFileName(baseName);
		File target = chooser.showSaveDialog(owner);
		
		if (target == null)
		{
			return;
		}
		
		File output = ensureExtension(target, format.extension);
		
		try
		{
			
			switch(format)
			{
				case CSV -> writeDelimited(output, result, ",");
				case TXT -> writeDelimited(output, result, "\t");
				case XLSX -> writeXlsx(output, result);
			}
			
			AlertBox.showInfo(owner, "Exported to " + output.getAbsolutePath());
		}
		catch (IOException ex)
		{
			AlertBox.showError(owner, "Export failed: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Write delimited.
	 *
	 * @param file the file
	 * @param result the result
	 * @param delimiter the delimiter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeDelimited(File file, QueryResult result, String delimiter)
		throws IOException
	{
		
		try (BufferedWriter writer = Files.newBufferedWriter(file.toPath()))
		{
			writer.write(String.join(delimiter,
				result.columns.stream()
					.map(value -> escapeValue(value, delimiter))
					.toList()));
			writer.newLine();
			
			for (Map<String, Object> row : result.rows)
			{
				List<String> cells = new ArrayList<>();
				
				for (String column : result.columns)
				{
					Object value = row.get(column);
					cells.add(escapeValue(value == null ? "" : value.toString(),
						delimiter));
				}
				
				writer.write(String.join(delimiter, cells));
				writer.newLine();
			}
			
		}
		
	}
	
	/**
	 * Write xlsx.
	 *
	 * @param file the file
	 * @param result the result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXlsx(File file, QueryResult result) throws IOException
	{
		
		try (Workbook wb = new XSSFWorkbook())
		{
			Sheet sheet = wb.createSheet("Results");
			Row header = sheet.createRow(0);
			
			for (int i = 0; i < result.columns.size(); i++)
			{
				header.createCell(i).setCellValue(result.columns.get(i));
			}
			
			int rowIndex = 1;
			
			for (Map<String, Object> row : result.rows)
			{
				Row excelRow = sheet.createRow(rowIndex++);
				
				for (int i = 0; i < result.columns.size(); i++)
				{
					Object value = row.get(result.columns.get(i));
					excelRow.createCell(i)
						.setCellValue(value == null ? "" : value.toString());
				}
				
			}
			
			for (int i = 0; i < result.columns.size(); i++)
			{
				sheet.autoSizeColumn(i);
			}
			
			try (var out = Files.newOutputStream(file.toPath()))
			{
				wb.write(out);
			}
			
		}
		
	}
	
	/**
	 * Escape value.
	 *
	 * @param value the value
	 * @param delimiter the delimiter
	 * @return the string
	 */
	private String escapeValue(String value, String delimiter)
	{
		String trimmed = value == null ? "" : value;
		boolean needsQuotes = trimmed.contains(delimiter) ||
			trimmed.contains("\n") || trimmed.contains("\r") ||
			trimmed.contains("\"");
		
		if (needsQuotes)
		{
			String escaped = trimmed.replace("\"", "\"\"");
			return "\"" + escaped + "\"";
		}
		
		return trimmed;
		
	}
	
	/**
	 * Ensure extension.
	 *
	 * @param file the file
	 * @param extension the extension
	 * @return the file
	 */
	private File ensureExtension(File file, String extension)
	{
		String name = file.getName().toLowerCase(Locale.ROOT);
		
		if (name.endsWith("." + extension))
		{
			return file;
		}
		
		return new File(file.getParentFile(), file.getName() + "." + extension);
		
	}
	
	/**
	 * Operator needs value.
	 *
	 * @param operator the operator
	 * @return true, if successful
	 */
	private boolean operatorNeedsValue(String operator)
	{
		return operator != null && !operator.equalsIgnoreCase("IS NULL") &&
			!operator.equalsIgnoreCase("IS NOT NULL");
		
	}
	
	/**
	 * Sets the parameter.
	 *
	 * @param ps the ps
	 * @param index the index
	 * @param sqlType the sql type
	 * @param value the value
	 * @throws SQLException the SQL exception
	 */
	private void setParameter(PreparedStatement ps, int index, int sqlType,
		String value) throws SQLException
	{
		
		if (value == null || value.isBlank())
		{
			ps.setNull(index, sqlType);
			return;
		}
		
		try
		{
			
			switch(sqlType)
			{
				case Types.INTEGER, Types.SMALLINT, Types.TINYINT ->
					ps.setInt(index, Integer.parseInt(value));
				case Types.BIGINT -> ps.setLong(index, Long.parseLong(value));
				case Types.DECIMAL, Types.NUMERIC ->
					ps.setBigDecimal(index, new BigDecimal(value));
				case Types.BOOLEAN, Types.BIT ->
					ps.setBoolean(index, Boolean.parseBoolean(value));
				case Types.DATE ->
					ps.setDate(index,
						java.sql.Date.valueOf(LocalDate.parse(value)));
				default -> ps.setString(index, value);
			}
			
		}
		catch (RuntimeException ex)
		{
			ps.setString(index, value);
		}
		
	}
	
	/**
	 * To query result.
	 *
	 * @param rs the rs
	 * @return the query result
	 * @throws SQLException the SQL exception
	 */
	private QueryResult toQueryResult(ResultSet rs) throws SQLException
	{
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();
		List<String> columns = new ArrayList<>();
		
		for (int i = 1; i <= columnCount; i++)
		{
			columns.add(meta.getColumnLabel(i));
		}
		
		List<Map<String, Object>> rows = new ArrayList<>();
		
		while (rs.next())
		{
			Map<String, Object> row = new HashMap<>();
			
			for (int i = 1; i <= columnCount; i++)
			{
				row.put(columns.get(i - 1), rs.getObject(i));
			}
			
			rows.add(row);
		}
		
		return new QueryResult(columns, rows);
		
	}
	
	/**
	 * Gets the owner window.
	 *
	 * @return the owner window
	 */
	private Window getOwnerWindow()
	{
		return getScene() != null ? getScene().getWindow() : null;
		
	}
	
	/**
	 * Sets the disabled state.
	 *
	 * @param disabled the new disabled state
	 */
	private void setDisabledState(boolean disabled)
	{
		this.tableBox.setDisable(disabled);
		this.fieldBox.setDisable(disabled);
		this.operatorBox.setDisable(disabled);
		this.valueField.setDisable(disabled || !operatorNeedsValue(
			this.operatorBox.getValue()));
		this.runButton.setDisable(disabled);
		this.refreshButton.setDisable(disabled);
		
	}
	
	/**
	 * The Enum ExportFormat.
	 */
	private enum ExportFormat
	{
		
		/** The csv. */
		CSV("csv"),
		
		/** The txt. */
		TXT("txt"),
		
		/** The xlsx. */
		XLSX("xlsx");
		
		/** The extension. */
		private final String extension;
		
		/**
		 * Instantiates a new export format.
		 *
		 * @param extension the extension
		 */
		ExportFormat(String extension)
		{
			this.extension = extension;
			
		}
		
	}
	
	/**
	 * The Record QueryResult.
	 *
	 * @param columns the columns
	 * @param rows the rows
	 */
	private record QueryResult(List<String> columns,
		List<Map<String, Object>> rows)
	{
	}
	
	/**
	 * The Record SqlSelection.
	 *
	 * @param sql the sql
	 * @param hasParameter the has parameter
	 * @param parameterType the parameter type
	 * @param parameterValue the parameter value
	 */
	private record SqlSelection(String sql, boolean hasParameter,
		Integer parameterType, String parameterValue)
	{
	}
	
}
