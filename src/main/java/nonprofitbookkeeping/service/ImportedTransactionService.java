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
        synchronized (transactions)
        {
            transactions.add(transaction);
        }
    }

    public List<ImportedTransaction> listAll()
    {
        synchronized (transactions)
        {
            return List.copyOf(transactions);
        }
    }

    public boolean delete(ImportedTransaction transaction)
    {
        synchronized (transactions)
        {
            return transactions.remove(transaction);
        }
    }
}
