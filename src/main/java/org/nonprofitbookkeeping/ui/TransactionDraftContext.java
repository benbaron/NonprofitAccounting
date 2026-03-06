package org.nonprofitbookkeeping.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Shared in-memory context used to hand off selected transaction drafts between panels.
 */
public final class TransactionDraftContext
{
    private static final ObjectProperty<LedgerRegisterPanel.Row> selectedRow =
        new SimpleObjectProperty<>();

    private TransactionDraftContext() {}

    public static ObjectProperty<LedgerRegisterPanel.Row> selectedRowProperty()
    {
        return selectedRow;
    }

    public static void setSelectedRow(LedgerRegisterPanel.Row row)
    {
        selectedRow.set(row);
    }

    public static LedgerRegisterPanel.Row getSelectedRow()
    {
        return selectedRow.get();
    }
}
