package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.AccountingTransaction;

public record PostingCommand(AccountingTransaction transaction,
                             String module,
                             String domainRecordId,
                             String linkRole,
                             String idempotencyKey) {}
