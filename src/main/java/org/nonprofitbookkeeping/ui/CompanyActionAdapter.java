package org.nonprofitbookkeeping.ui;

/**
 * Adapter for company lifecycle operations used by the B-shell.
 */
interface CompanyActionAdapter
{
    void openCompany(Runnable onCompanyOpened);

    void createOrEditCompany();

    void saveCompany();

    boolean closeCompany();
}
