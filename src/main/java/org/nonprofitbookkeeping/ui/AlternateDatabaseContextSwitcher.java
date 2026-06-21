package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;

/** Encapsulates DB context switching and open+repair flow for alternate shell entry points. */
class AlternateDatabaseContextSwitcher
{
    void openDatabase(Path basePath) throws Exception
    {
        DatabaseOpenService.openDatabase(basePath);
    }
}
