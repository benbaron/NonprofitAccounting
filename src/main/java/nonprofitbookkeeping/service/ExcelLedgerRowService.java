package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.ExcelLedgerRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Excel ledger row operations.
 */
public class ExcelLedgerRowService
{
    private final List<ExcelLedgerRow> rows = new ArrayList<>();

    public void add(ExcelLedgerRow row)
    {
        synchronized (this.rows)
        {
            this.rows.add(row);
        }
    }

    public List<ExcelLedgerRow> listAll()
    {
        synchronized (this.rows)
        {
            return List.copyOf(this.rows);
        }
    }

    public boolean delete(ExcelLedgerRow row)
    {
        synchronized (this.rows)
        {
            return this.rows.remove(row);
        }
    }
}
