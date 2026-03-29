package org.nonprofitbookkeeping.ui;

import javafx.stage.Stage;

/**
 * Adapter for company lifecycle operations used by the B-shell.
 */
interface CompanyActionAdapter
{
    void openCompany(Stage owner, Runnable onCompanyOpened);

    void createOrEditCompany(Stage owner);

    void saveCompany(Stage owner);

    boolean closeCompany(Stage owner);
}
