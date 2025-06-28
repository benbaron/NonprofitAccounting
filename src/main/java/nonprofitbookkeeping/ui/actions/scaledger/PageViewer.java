/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * PageViewer.java
 * PageViewer
 */
package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.table.DefaultTableModel;

/**
 * Placeholder class related to page viewing, potentially intended to manage or provide
 * data models for a UI component like {@link nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel}.
 * Currently, it only contains a stub method.
 */
public class PageViewer
{

        /**
         * Creates a simple {@link DefaultTableModel} containing page data.
         * <p>
         * In lieu of a backing data store, this method generates a small table
         * model with example content. Callers can replace the model data with
         * actual values via {@link nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel#loadData(DefaultTableModel)}.
         * </p>
         *
         * @return A {@link DefaultTableModel} with two columns: "Page" and "Content".
         */
        public static DefaultTableModel getTableModel()
        {
                String[] cols = { "Page", "Content" };
                Object[][] data = { { "1", "Sample" } };
                return new DefaultTableModel(data, cols);
        }
	
}
