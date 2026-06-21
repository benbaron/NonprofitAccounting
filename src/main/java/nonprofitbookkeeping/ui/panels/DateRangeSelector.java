package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

/**
 * Preset selector + date pickers for CUSTOM.
 */
public class DateRangeSelector extends HBox
{
    private final ComboBox<DateRangePreset> preset = new ComboBox<>();
    private final DatePicker start = new DatePicker();
    private final DatePicker end = new DatePicker();
    private final Label summary = new Label();

    public DateRangeSelector()
    {
        setSpacing(8);
        setPadding(new Insets(0, 0, 0, 6));

        this.preset.getItems().setAll(DateRangePreset.values());
        this.preset.getSelectionModel().select(DateRangePreset.ALL);

        this.start.setPrefWidth(140);
        this.end.setPrefWidth(140);

        getChildren().addAll(new Label("Range:"), this.preset, this.start, new Label("to"), this.end, this.summary);

        this.preset.valueProperty().addListener((o, a, b) -> recompute());
        this.start.valueProperty().addListener((o, a, b) -> recompute());
        this.end.valueProperty().addListener((o, a, b) -> recompute());

        recompute();
    }

    private void recompute()
    {
        DateRangePreset p = this.preset.getValue();
        boolean custom = p == DateRangePreset.CUSTOM;

        this.start.setDisable(!custom);
        this.end.setDisable(!custom);

        LocalDate s = this.start.getValue();
        LocalDate e = this.end.getValue();

        DateRange computed = DateRangeUtil.compute(p, LocalDate.now(), s, e);
        DateRangeContext.set(computed);

        this.summary.setText(computed.toString());
    }

    public ComboBox<DateRangePreset> presetBox() { return this.preset; }
}
