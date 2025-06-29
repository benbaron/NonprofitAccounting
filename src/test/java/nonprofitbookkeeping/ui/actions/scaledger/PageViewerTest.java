package nonprofitbookkeeping.ui.actions.scaledger;

import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.*;

public class PageViewerTest {
    @Test
    public void testGetTableModelReturnsSampleData() {
        DefaultTableModel model = PageViewer.getTableModel();
        assertEquals(2, model.getColumnCount());
        assertEquals("Page", model.getColumnName(0));
        assertEquals("Content", model.getColumnName(1));
        assertEquals(1, model.getRowCount());
        assertEquals("1", model.getValueAt(0, 0));
        assertEquals("Sample", model.getValueAt(0, 1));
    }
}
