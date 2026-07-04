package org.nonprofitbookkeeping.ui;

/** Command action callbacks supplied by the alternate shell. */
record AlternateUiCommandActions(Runnable openDatabase,
    Runnable importDatabase,
    Runnable exportDatabase,
    Runnable repairDatabase,
    Runnable createCompany,
    Runnable destroyCompany,
    Runnable populateCompany,
    Runnable createSampleCompany,
    Runnable openCompany,
    Runnable importChartOfAccounts,
    Runnable exportChartOfAccounts,
    Runnable importSclx,
    Runnable exportSclx,
    Runnable openMonthlyClose,
    Runnable openChartOfAccounts,
    Runnable openJournal,
    Runnable openInventory,
    Runnable openReportsWorkspace,
    Runnable newItem,
    Runnable save,
    Runnable delete,
    Runnable cancel,
    Runnable find,
    Runnable scheduleReport,
    Runnable openDonors,
    Runnable openFunds,
    Runnable reconcileAccounts,
    Runnable undepositedFunds,
    Runnable documents,
    Runnable help)
{
    static AlternateUiCommandActions noop()
    {
        Runnable noop = () -> { };
        return new AlternateUiCommandActions(noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop,
            noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop,
            noop, noop, noop);
    }
}
