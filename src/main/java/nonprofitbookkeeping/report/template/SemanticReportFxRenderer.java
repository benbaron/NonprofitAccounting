package nonprofitbookkeeping.report.template;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Renders compact semantic report templates into JavaFX form/table previews. */
public class SemanticReportFxRenderer
{
	private static final double MIN_CHAR_WIDTH = 10 * 7.5;

	public Node render(JsonNode template, SemanticReportValueSet values)
	{
		Node content = "tableReport".equals(template.path("type").asText("sectionReport"))
			? renderTable(template, values)
			: renderSections(template, values);
		ScrollPane pane = new ScrollPane(content);
		pane.setFitToWidth(false);
		pane.setFitToHeight(false);
		pane.setPannable(true);
		pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		return pane;
	}

	private Node renderSections(JsonNode template, SemanticReportValueSet values)
	{
		VBox root = new VBox(14);
		root.setPadding(new Insets(18));
		root.setStyle("-fx-background-color: white;");
		Label title = label(template.path("title").asText(template.path("templateId").asText()),
			"-fx-font-size: 18px; -fx-font-weight: bold;");
		root.getChildren().add(title);
		if (template.hasNonNull("subtitle"))
		{
			root.getChildren().add(label(template.path("subtitle").asText(),
				"-fx-font-size: 12px; -fx-text-fill: #555;"));
		}
		for (JsonNode section : template.path("sections"))
		{
			root.getChildren().add(sectionGrid(section, values));
		}
		return root;
	}

	private Node sectionGrid(JsonNode section, SemanticReportValueSet values)
	{
		VBox box = new VBox(4);
		box.getChildren().add(label(section.path("title").asText(),
			"-fx-font-weight: bold; -fx-background-color: #d9eaf7; -fx-padding: 4 6 4 6;"));
		GridPane grid = new GridPane();
		grid.setHgap(0);
		grid.setVgap(0);
		int r = 0;
		grid.add(header("Line", 80), 0, r);
		grid.add(header("Description", 360), 1, r);
		grid.add(header("Amount", 120), 2, r);
		grid.add(header("Notes", 260), 3, r++);
		for (JsonNode row : section.path("rows"))
		{
			if ("spacer".equals(row.path("type").asText()))
			{
				r++;
				continue;
			}
			String value = "";
			if (row.hasNonNull("valueKey"))
			{
				value = format(values.get(row.path("valueKey").asText()),
					row.path("format").asText("text"));
			}
			grid.add(cell(row.path("line").asText(""), 80, Pos.CENTER_LEFT, false), 0, r);
			grid.add(cell(row.path("label").asText(""), 360, Pos.CENTER_LEFT, false), 1, r);
			grid.add(cell(value, 120, Pos.CENTER_RIGHT, row.path("emphasis").asBoolean(false)), 2, r);
			grid.add(cell(row.path("note").asText(""), 260, Pos.CENTER_LEFT, false), 3, r++);
		}
		box.getChildren().add(grid);
		return box;
	}

	private Node renderTable(JsonNode template, SemanticReportValueSet values)
	{
		VBox root = new VBox(10);
		root.setPadding(new Insets(18));
		root.setStyle("-fx-background-color: white;");
		root.getChildren().add(label(template.path("title").asText(template.path("templateId").asText()),
			"-fx-font-size: 18px; -fx-font-weight: bold;"));
		if (template.hasNonNull("subtitle"))
		{
			root.getChildren().add(label(template.path("subtitle").asText(),
				"-fx-font-size: 12px; -fx-text-fill: #555;"));
		}
		GridPane grid = new GridPane();
		JsonNode columns = template.path("columns");
		for (int c = 0; c < columns.size(); c++)
		{
			JsonNode col = columns.get(c);
			grid.add(header(col.path("label").asText(), width(col)), c, 0);
		}
		List<Map<String, Object>> rows = values.table(template.path("tableKey").asText());
		int r = 1;
		for (Map<String, Object> row : rows)
		{
			for (int c = 0; c < columns.size(); c++)
			{
				JsonNode col = columns.get(c);
				String display = format(row.get(col.path("field").asText()),
					col.path("format").asText("text"));
				Pos align = "currency".equals(col.path("format").asText("text")) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
				grid.add(cell(display, width(col), align, false), c, r);
			}
			r++;
		}
		if (rows.isEmpty())
		{
			grid.add(cell("No rows for the selected reporting period.", 360, Pos.CENTER_LEFT, false), 0, 1, Math.max(1, columns.size()), 1);
		}
		root.getChildren().add(grid);
		return root;
	}

	private Label header(String text, double width)
	{
		Label label = cell(text, width, Pos.CENTER_LEFT, true);
		label.setStyle(label.getStyle() + " -fx-background-color: #e7eef7; -fx-font-weight: bold;");
		return label;
	}

	private Label cell(String text, double width, Pos alignment, boolean emphasis)
	{
		Label label = label(text, "-fx-padding: 3 6 3 6; -fx-border-color: #b8b8b8; -fx-border-width: 0.5;");
		label.setMinWidth(Math.max(MIN_CHAR_WIDTH, width));
		label.setPrefWidth(Math.max(MIN_CHAR_WIDTH, width));
		label.setMinHeight(24);
		label.setAlignment(alignment);
		label.setWrapText(true);
		if (emphasis)
		{
			label.setStyle(label.getStyle() + " -fx-font-weight: bold; -fx-background-color: #f7f7f7;");
		}
		GridPane.setHgrow(label, Priority.NEVER);
		return label;
	}

	private Label label(String text, String style)
	{
		Label label = new Label(text == null ? "" : text);
		label.setStyle(style);
		return label;
	}

	private double width(JsonNode column)
	{
		return Math.max(MIN_CHAR_WIDTH, column.path("width").asDouble(120));
	}

	private static String format(Object value, String format)
	{
		if (value == null)
		{
			return "currency".equals(format) ? "-" : "";
		}
		if ("currency".equals(format) && value instanceof BigDecimal amount)
		{
			return amount.signum() == 0 ? "-" : amount.toPlainString();
		}
		if ("date".equals(format) && value instanceof LocalDate date)
		{
			return date.toString();
		}
		return String.valueOf(value);
	}
}
