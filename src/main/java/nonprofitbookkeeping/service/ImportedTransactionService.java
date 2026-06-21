package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.ImportedTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for imported transaction operations.
 */
public class ImportedTransactionService
{
    private final List<ImportedTransaction> transactions = new ArrayList<>();

    public void add(ImportedTransaction transaction)
    {
        synchronized (this.transactions)
        {
            this.transactions.add(transaction);
        }
    }

    public List<ImportedTransaction> listAll()
    {
        synchronized (this.transactions)
        {
            return List.copyOf(this.transactions);
        }
    }

    public boolean delete(ImportedTransaction transaction)
    {
        synchronized (this.transactions)
        {
            return this.transactions.remove(transaction);
        }
    }
}
