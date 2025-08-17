package nonprofitbookkeeping.ui.panels;

/**
 * Simple contract for UI panels that collect data for a report row bean.
 * Implementations should be JavaFX nodes (e.g. extend VBox) and provide
 * a way to build the corresponding bean used during report generation.
 */
public interface ReportRowPanel
{
    /**
     * Build and return the bean populated with data from the UI controls.
     *
     * @return bean instance ready to be consumed by {@code ReportService}.
     */
    Object buildBean();
}

