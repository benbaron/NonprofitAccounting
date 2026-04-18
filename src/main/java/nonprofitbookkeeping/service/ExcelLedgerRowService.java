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
        synchronized (rows)
        {
            rows.add(row);
        }
    }

    public List<ExcelLedgerRow> listAll()
    {
        synchronized (rows)
        {
            return List.copyOf(rows);
        }
    }

    public boolean delete(ExcelLedgerRow row)
    {
        synchronized (rows)
        {
            return rows.remove(row);
        }
    }
}
