package nonprofitbookkeeping.service;

import java.sql.SQLException;

public interface PostingFacade
{
    PostingReference post(PostingCommand command) throws SQLException;

    PostingReference reverse(int journalTxnId, String reason) throws SQLException;

    PostingReference amend(int oldJournalTxnId, PostingCommand newCommand,
        String reason) throws SQLException;
}
