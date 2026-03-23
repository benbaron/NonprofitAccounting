package nonprofitbookkeeping.importer.sclx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Person;
import nonprofitbookkeeping.model.supplemental.DeferredRevenueLine;
import nonprofitbookkeeping.model.supplemental.OtherAssetsLine;
import nonprofitbookkeeping.model.supplemental.OtherLiabilitiesLine;
import nonprofitbookkeeping.model.supplemental.PayablesLine;
import nonprofitbookkeeping.model.supplemental.PrepaidExpenseLine;
import nonprofitbookkeeping.model.supplemental.ReceivablesLine;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.persistence.PersonRepository;
import nonprofitbookkeeping.persistence.impex.BankStatementRecordRepository;
import nonprofitbookkeeping.persistence.impex.BudgetRecordRepository;
import nonprofitbookkeeping.persistence.impex.BankingItemRecordRepository;
import nonprofitbookkeeping.persistence.impex.OrganizationRecordRepository;
import nonprofitbookkeeping.persistence.impex.ReportingPeriodRecordRepository;
import nonprofitbookkeeping.persistence.impex.FundRecordRepository;
import nonprofitbookkeeping.persistence.impex.EventRecordRepository;
import nonprofitbookkeeping.persistence.impex.DocumentRecordRepository;
import nonprofitbookkeeping.persistence.impex.OutstandingItemRecordRepository;
import nonprofitbookkeeping.persistence.impex.OtherAssetItemRecordRepository;
import nonprofitbookkeeping.persistence.impex.AssetRecordRepository;
import nonprofitbookkeeping.persistence.impex.SupplyRecordRepository;
import nonprofitbookkeeping.service.scaledger.JournalLedgerPersistenceGateway;
import nonprofitbookkeeping.model.impex.BankStatementRecord;
import nonprofitbookkeeping.model.impex.BudgetRecord;
import nonprofitbookkeeping.model.impex.BankingItemRecord;
import nonprofitbookkeeping.model.impex.OrganizationRecord;
import nonprofitbookkeeping.model.impex.ReportingPeriodRecord;
import nonprofitbookkeeping.model.impex.FundRecord;
import nonprofitbookkeeping.model.impex.EventRecord;
import nonprofitbookkeeping.model.impex.DocumentRecord;
import nonprofitbookkeeping.model.impex.OutstandingItemRecord;
import nonprofitbookkeeping.model.impex.OtherAssetItemRecord;
import nonprofitbookkeeping.model.impex.AssetRecord;
import nonprofitbookkeeping.model.impex.SupplyRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Concrete SCLX import target for NonprofitBookkeeping.
 *
 * <p>This target persists the parts of SCLX that map cleanly onto the existing
 * application model:
 * <ul>
 *   <li>accounts -> {@link AccountRepository}</li>
 *   <li>people -> {@link PersonRepository}</li>
 *   <li>transactions -> {@link JournalLedgerPersistenceGateway}</li>
 *   <li>supplementalItems -> attached as transaction supplemental lines when a
 *       related imported transaction can be found</li>
 * </ul>
 *
 * <p>Collections that already have concrete import record support are imported
 * as first-class records. Remaining collections without a stable repository
 * path are still preserved through {@link DocumentRepository}.
 */
public class NonprofitBookkeepingSclxImportTarget implements SclxImportTarget
{
    private static final String DOC_PREFIX = "sclx.import.";

    private final JournalLedgerPersistenceGateway journalGateway;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final DocumentRepository documentRepository;
    private final BankStatementRecordRepository bankStatementRecordRepository;
    private final BudgetRecordRepository budgetRecordRepository;
    private final BankingItemRecordRepository bankingItemRecordRepository;
    private final OrganizationRecordRepository organizationRecordRepository;
    private final ReportingPeriodRecordRepository reportingPeriodRecordRepository;
    private final FundRecordRepository fundRecordRepository;
    private final EventRecordRepository eventRecordRepository;
    private final DocumentRecordRepository documentRecordRepository;
    private final OutstandingItemRecordRepository outstandingItemRecordRepository;
    private final OtherAssetItemRecordRepository otherAssetItemRecordRepository;
    private final AssetRecordRepository assetRecordRepository;
    private final SupplyRecordRepository supplyRecordRepository;
    private final ObjectMapper mapper;

    private SclxDocument document;
    private SclxImportOptions options;

    private final Map<String, String> sclxAccountNameByRef;
    private final Map<String, Account> existingAccountsByRef;
    private final Map<String, Person> existingPeopleByName;
    private final Map<String, Person> importedPeopleBySclxId;
    private final Map<String, String> fundNameById;
    private final Map<String, String> budgetNameById;
    private final Map<String, AccountingTransaction> importedTransactionsBySclxId;
    private final Map<Integer, AccountingTransaction> importedTransactionsByWorkbookRow;
    private final List<SclxDocument.SupplementalItem> unmatchedSupplementalItems;

    public NonprofitBookkeepingSclxImportTarget()
    {
        this(new JournalLedgerPersistenceGateway(),
            new AccountRepository(),
            new PersonRepository(),
            new DocumentRepository(),
            new BankStatementRecordRepository(),
            new BudgetRecordRepository(),
            new BankingItemRecordRepository(),
            new OrganizationRecordRepository(),
            new ReportingPeriodRecordRepository(),
            new FundRecordRepository(),
            new EventRecordRepository(),
            new DocumentRecordRepository(),
            new OutstandingItemRecordRepository(),
            new OtherAssetItemRecordRepository(),
            new AssetRecordRepository(),
            new SupplyRecordRepository());
    }

    public NonprofitBookkeepingSclxImportTarget(
        JournalLedgerPersistenceGateway journalGateway,
        AccountRepository accountRepository,
        PersonRepository personRepository,
        DocumentRepository documentRepository,
        BankStatementRecordRepository bankStatementRecordRepository,
        BudgetRecordRepository budgetRecordRepository,
        BankingItemRecordRepository bankingItemRecordRepository,
        OrganizationRecordRepository organizationRecordRepository,
        ReportingPeriodRecordRepository reportingPeriodRecordRepository,
        FundRecordRepository fundRecordRepository,
        EventRecordRepository eventRecordRepository,
        DocumentRecordRepository documentRecordRepository,
        OutstandingItemRecordRepository outstandingItemRecordRepository,
        OtherAssetItemRecordRepository otherAssetItemRecordRepository,
        AssetRecordRepository assetRecordRepository,
        SupplyRecordRepository supplyRecordRepository)
    {
        this.journalGateway = Objects.requireNonNull(journalGateway, "journalGateway");
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository");
        this.personRepository = Objects.requireNonNull(personRepository, "personRepository");
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository");
        this.bankStatementRecordRepository = Objects.requireNonNull(bankStatementRecordRepository, "bankStatementRecordRepository");
        this.budgetRecordRepository = Objects.requireNonNull(budgetRecordRepository, "budgetRecordRepository");
        this.bankingItemRecordRepository = Objects.requireNonNull(bankingItemRecordRepository, "bankingItemRecordRepository");
        this.organizationRecordRepository = Objects.requireNonNull(organizationRecordRepository, "organizationRecordRepository");
        this.reportingPeriodRecordRepository = Objects.requireNonNull(reportingPeriodRecordRepository, "reportingPeriodRecordRepository");
        this.fundRecordRepository = Objects.requireNonNull(fundRecordRepository, "fundRecordRepository");
        this.eventRecordRepository = Objects.requireNonNull(eventRecordRepository, "eventRecordRepository");
        this.documentRecordRepository = Objects.requireNonNull(documentRecordRepository, "documentRecordRepository");
        this.outstandingItemRecordRepository = Objects.requireNonNull(outstandingItemRecordRepository, "outstandingItemRecordRepository");
        this.otherAssetItemRecordRepository = Objects.requireNonNull(otherAssetItemRecordRepository, "otherAssetItemRecordRepository");
        this.assetRecordRepository = Objects.requireNonNull(assetRecordRepository, "assetRecordRepository");
        this.supplyRecordRepository = Objects.requireNonNull(supplyRecordRepository, "supplyRecordRepository");

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.sclxAccountNameByRef = new LinkedHashMap<>();
        this.existingAccountsByRef = new LinkedHashMap<>();
        this.existingPeopleByName = new LinkedHashMap<>();
        this.importedPeopleBySclxId = new LinkedHashMap<>();
        this.fundNameById = new LinkedHashMap<>();
        this.budgetNameById = new LinkedHashMap<>();
        this.importedTransactionsBySclxId = new LinkedHashMap<>();
        this.importedTransactionsByWorkbookRow = new LinkedHashMap<>();
        this.unmatchedSupplementalItems = new ArrayList<>();
    }

    @Override
    public void beginImport(SclxDocument document, SclxImportOptions options)
    {
        this.document = document;
        this.options = options == null ? SclxImportOptions.defaults() : options;

        this.sclxAccountNameByRef.clear();
        this.existingAccountsByRef.clear();
        this.existingPeopleByName.clear();
        this.importedPeopleBySclxId.clear();
        this.fundNameById.clear();
        this.budgetNameById.clear();
        this.importedTransactionsBySclxId.clear();
        this.importedTransactionsByWorkbookRow.clear();
        this.unmatchedSupplementalItems.clear();

        loadExistingAccounts();
        loadExistingPeople();
    }

    @Override
    public void importOrganization(SclxDocument.Organization organization)
    {
        if (organization == null)
        {
            return;
        }

        try
        {
            this.organizationRecordRepository.upsert(toOrganizationRecord(organization));
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to import organization " + organization.organizationId(), ex);
        }
    }

    @Override
    public void importReportingPeriod(SclxDocument.ReportingPeriod reportingPeriod)
    {
        if (reportingPeriod == null)
        {
            return;
        }

        try
        {
            this.reportingPeriodRecordRepository.upsert(toReportingPeriodRecord(reportingPeriod));
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to import reporting period", ex);
        }
    }

    @Override
    public void importAccounts(List<SclxDocument.Account> accounts)
    {
        if (accounts == null || accounts.isEmpty())
        {
            return;
        }

        for (SclxDocument.Account account : accounts)
        {
            cacheSclxAccount(account);

            if (this.options.accountImportMode() == AccountImportMode.MAPPED)
            {
                continue;
            }

            upsertAccount(account);
        }
    }

    @Override
    public void importFunds(List<SclxDocument.Fund> funds)
    {
        if (funds == null || funds.isEmpty())
        {
            return;
        }

        for (SclxDocument.Fund fund : funds)
        {
            if (fund == null)
            {
                continue;
            }

            if (fund.fundId() != null && !fund.fundId().isBlank())
            {
                this.fundNameById.put(fund.fundId(), firstNonBlank(fund.name(), fund.fundId()));
            }

            try
            {
                this.fundRecordRepository.upsert(toFundRecord(fund));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import fund " + fund.fundId(), ex);
            }
        }
    }

    
@Override
public void importBudgets(List<SclxDocument.Budget> budgets)
{
    if (budgets == null || budgets.isEmpty())
    {
        return;
    }

    for (SclxDocument.Budget budget : budgets)
    {
        if (budget == null)
        {
            continue;
        }

        if (budget.budgetId() != null && !budget.budgetId().isBlank())
        {
            this.budgetNameById.put(budget.budgetId(), firstNonBlank(budget.name(), budget.budgetId()));
        }

        try
        {
            this.budgetRecordRepository.upsert(toBudgetRecord(budget));
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to import budget " + budget.budgetId(), ex);
        }
    }
}

@Override
public void importPeople(List<SclxDocument.Person> people)
    {
        if (people == null || people.isEmpty())
        {
            return;
        }

        for (SclxDocument.Person person : people)
        {
            if (person == null)
            {
                continue;
            }

            Person saved = resolveOrCreatePerson(person.displayName(), person.email(), person.phone());
            if (person.personId() != null && !person.personId().isBlank() && saved != null)
            {
                this.importedPeopleBySclxId.put(person.personId(), saved);
            }
        }
    }

    @Override
    public void importEvents(List<SclxDocument.Event> events)
    {
        if (events == null || events.isEmpty())
        {
            return;
        }

        for (SclxDocument.Event event : events)
        {
            if (event == null)
            {
                continue;
            }

            try
            {
                this.eventRecordRepository.upsert(toEventRecord(event));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import event " + event.eventId(), ex);
            }
        }
    }

    @Override
    public void importDocuments(List<SclxDocument.Document> documents)
    {
        if (documents == null || documents.isEmpty())
        {
            return;
        }

        for (SclxDocument.Document document : documents)
        {
            if (document == null)
            {
                continue;
            }

            try
            {
                this.documentRecordRepository.upsert(toDocumentRecord(document));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import document " + document.documentId(), ex);
            }
        }
    }

    @Override
    public void importTransactions(List<SclxDocument.Transaction> transactions)
    {
        if (transactions == null || transactions.isEmpty())
        {
            return;
        }

        for (SclxDocument.Transaction tx : transactions)
        {
            if (tx == null)
            {
                continue;
            }

            AccountingTransaction imported = convertTransaction(tx);
            if (imported == null)
            {
                continue;
            }

            AccountingTransaction saved = this.journalGateway.saveTransactionWithEntries(imported);
            this.importedTransactionsBySclxId.put(tx.transactionId(), saved);

            Integer workbookRow = tx.workbookLink() == null ? null : tx.workbookLink().ledgerRowIndex();
            if (workbookRow != null)
            {
                this.importedTransactionsByWorkbookRow.put(workbookRow, saved);
            }
        }
    }

    @Override
    public void importOutstandingItems(List<SclxDocument.OutstandingItem> outstandingItems)
    {
        if (outstandingItems == null || outstandingItems.isEmpty())
        {
            return;
        }

        for (SclxDocument.OutstandingItem outstandingItem : outstandingItems)
        {
            if (outstandingItem == null)
            {
                continue;
            }

            try
            {
                this.outstandingItemRecordRepository.upsert(toOutstandingItemRecord(outstandingItem));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import outstanding item " + outstandingItem.outstandingItemId(), ex);
            }
        }
    }

    @Override
    public void importOtherAssetItems(List<SclxDocument.OtherAssetItem> otherAssetItems)
    {
        if (otherAssetItems == null || otherAssetItems.isEmpty())
        {
            return;
        }

        for (SclxDocument.OtherAssetItem otherAssetItem : otherAssetItems)
        {
            if (otherAssetItem == null)
            {
                continue;
            }

            try
            {
                this.otherAssetItemRecordRepository.upsert(toOtherAssetItemRecord(otherAssetItem));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import otherAssetItem " + otherAssetItem.otherAssetItemId(), ex);
            }
        }
    }

    @Override
    public void importSupplementalItems(List<SclxDocument.SupplementalItem> supplementalItems)
    {
        if (supplementalItems == null || supplementalItems.isEmpty())
        {
            return;
        }

        for (SclxDocument.SupplementalItem item : supplementalItems)
        {
            if (item == null)
            {
                continue;
            }

            AccountingTransaction txn = resolveSupplementalTransaction(item);
            if (txn == null)
            {
                this.unmatchedSupplementalItems.add(item);
                continue;
            }

            TxnSupplementalLineBase bean = toSupplementalBean(item);
            txn.getSupplementalLines().add(bean);
            this.journalGateway.saveTransactionWithEntries(txn);
        }
    }

    @Override
    public void importAssets(List<SclxDocument.Asset> assets)
    {
        if (assets == null || assets.isEmpty())
        {
            return;
        }

        for (SclxDocument.Asset asset : assets)
        {
            if (asset == null)
            {
                continue;
            }

            try
            {
                this.assetRecordRepository.upsert(toAssetRecord(asset));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import asset " + asset.assetId(), ex);
            }
        }
    }

    @Override
    public void importSupplies(List<SclxDocument.Supply> supplies)
    {
        if (supplies == null || supplies.isEmpty())
        {
            return;
        }

        for (SclxDocument.Supply supply : supplies)
        {
            if (supply == null)
            {
                continue;
            }

            try
            {
                this.supplyRecordRepository.upsert(toSupplyRecord(supply));
            }
            catch (SQLException ex)
            {
                throw new SclxImportException("Failed to import supply " + supply.supplyId(), ex);
            }
        }
    }

    
@Override
public void importBankingItems(List<SclxDocument.BankingItem> bankingItems)
{
    if (bankingItems == null || bankingItems.isEmpty())
    {
        return;
    }

    for (SclxDocument.BankingItem bankingItem : bankingItems)
    {
        if (bankingItem == null)
        {
            continue;
        }

        try
        {
            this.bankingItemRecordRepository.upsert(toBankingItemRecord(bankingItem));
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to import bankingItem " + bankingItem.bankingItemId(), ex);
        }
    }
}

@Override
public void importBankStatementImports(List<SclxDocument.BankStatementImport> bankStatementImports)
{
    if (bankStatementImports == null || bankStatementImports.isEmpty())
    {
        return;
    }

    for (SclxDocument.BankStatementImport bankStatementImport : bankStatementImports)
    {
        if (bankStatementImport == null)
        {
            continue;
        }

        try
        {
            this.bankStatementRecordRepository.upsert(toBankStatementRecord(bankStatementImport));
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to import bankStatementImport " + bankStatementImport.importId(), ex);
        }
    }
}

    @Override
    public void completeImport(SclxImportResult result)
    {
        persistJsonDocument(DOC_PREFIX + "summary", result);

        if (!this.unmatchedSupplementalItems.isEmpty())
        {
            persistJsonDocument(DOC_PREFIX + "unmatchedSupplementalItems", this.unmatchedSupplementalItems);
        }
    }

    private void loadExistingAccounts()
    {
        try
        {
            for (Account account : this.accountRepository.listAll())
            {
                cacheExistingAccount(account);
            }
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to load existing accounts", ex);
        }
    }

    private void loadExistingPeople()
    {
        try
        {
            for (Person person : this.personRepository.list())
            {
                if (person != null && person.getName() != null)
                {
                    this.existingPeopleByName.put(normalizeKey(person.getName()), person);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to load existing people", ex);
        }
    }

    private void cacheExistingAccount(Account account)
    {
        if (account == null)
        {
            return;
        }

        putIfPresent(this.existingAccountsByRef, account.getAccountNumber(), account);
        putIfPresent(this.existingAccountsByRef, account.getName(), account);
        putIfPresent(this.existingAccountsByRef, account.getAccountCode(), account);
    }

    private void cacheSclxAccount(SclxDocument.Account account)
    {
        if (account == null)
        {
            return;
        }

        putIfPresent(this.sclxAccountNameByRef, account.accountId(), firstNonBlank(account.Name(), account.Number(), account.accountId()));
        putIfPresent(this.sclxAccountNameByRef, account.Number(), firstNonBlank(account.Name(), account.Number(), account.accountId()));
        putIfPresent(this.sclxAccountNameByRef, account.Name(), firstNonBlank(account.Name(), account.Number(), account.accountId()));
        putIfPresent(this.sclxAccountNameByRef, account.code(), firstNonBlank(account.Name(), account.Number(), account.accountId()));
    }

    private void upsertAccount(SclxDocument.Account source)
    {
        if (source == null)
        {
            return;
        }

        String accountNumber = resolveSclxAccountReference(source.accountId(), source.Number(), source.Name());
        if (accountNumber == null || accountNumber.isBlank())
        {
            return;
        }

        Account account = this.existingAccountsByRef.get(normalizeKey(accountNumber));
        if (account == null)
        {
            account = new Account();
            account.setAccountNumber(accountNumber);
        }

        account.setName(firstNonBlank(source.Name(), accountNumber));
        account.setAccountCode(firstNonBlank(source.code(), account.getAccountCode()));
        account.setIncreaseSide(parseAccountSide(source.IncreaseSide(), source.Type()));
        account.setAccountType(parseAccountType(source.Type()));
        account.setParentAccountId(firstNonBlank(source.Parent(), account.getParentAccountId()));
        account.setOpeningBalance(source.OpeningBalance());
        account.setCurrency(this.document != null && this.document.organization() != null
            ? this.document.organization().baseCurrency()
            : account.getCurrency());

        try
        {
            this.accountRepository.upsert(account);
            cacheExistingAccount(account);
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to upsert account " + accountNumber, ex);
        }
    }

    private AccountingTransaction convertTransaction(SclxDocument.Transaction tx)
    {
        if (tx.lines() == null || tx.lines().isEmpty())
        {
            return null;
        }

        AccountingTransaction out = new AccountingTransaction();
        out.setId(0);
        out.setDate(stringDate(firstNonNull(tx.postingDate(), tx.transactionDate())));
        out.setBookingDateTimestamp(epochMillis(firstNonNull(tx.postingDate(), tx.transactionDate())));
        out.setDescription(firstNonBlank(tx.description(), tx.reference(), tx.transactionId()));
        out.setMemo(firstNonBlank(tx.description(), tx.reference(), tx.transactionId()));
        out.setCheckNumber(blankToEmpty(tx.reference()));
        out.setClearBank(blankToEmpty(tx.bankTiming()));
        out.setBank(blankToEmpty(extractWorkbookString(tx.extensions(), "bankAccount")));
        out.setReconciled(false);
        out.setBudgetTracking(blankToEmpty(resolveBudgetName(firstNonBlank(tx.budgetId(), firstLineBudgetId(tx)))));
        out.setAssociatedFundName(blankToEmpty(resolveFundName(firstLineFundId(tx))));
        out.setToFrom(blankToEmpty(resolveTransactionToFrom(tx)));
        out.setInfo(new LinkedHashMap<>());

        storeTransactionInfo(out, tx);

        for (SclxDocument.TransactionLine line : tx.lines())
        {
            AccountingEntry entry = toEntry(line);
            if (entry != null)
            {
                out.addEntry(entry);
                storeLineInfo(out, line);
            }
        }

        if (out.getEntries() == null || out.getEntries().isEmpty())
        {
            return null;
        }

        addBalancingCashLineIfNeeded(out, tx);
        return out;
    }

    private AccountingEntry toEntry(SclxDocument.TransactionLine line)
    {
        if (line == null)
        {
            return null;
        }

        BigDecimal debit = safeAmount(line.debit());
        BigDecimal credit = safeAmount(line.credit());
        if (debit.signum() == 0 && credit.signum() == 0)
        {
            return null;
        }

        AccountSide side = debit.signum() > 0 ? AccountSide.DEBIT : AccountSide.CREDIT;
        BigDecimal amount = debit.signum() > 0 ? debit : credit;

        String accountNumber = resolveSclxAccountReference(line.accountId(), line.accountId(), null);
        String accountName = resolveAccountName(line.accountId(), accountNumber);

        AccountingEntry entry = new AccountingEntry(amount, accountNumber, side, accountName);
        if (line.fundId() != null && !line.fundId().isBlank())
        {
            entry.setFundNumber(line.fundId());
        }
        return entry;
    }

    private void addBalancingCashLineIfNeeded(AccountingTransaction transaction, SclxDocument.Transaction tx)
    {
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        for (AccountingEntry entry : transaction.getEntries())
        {
            if (entry.getAccountSide() == AccountSide.DEBIT)
            {
                debitTotal = debitTotal.add(entry.getAmount());
            }
            else if (entry.getAccountSide() == AccountSide.CREDIT)
            {
                creditTotal = creditTotal.add(entry.getAmount());
            }
        }

        BigDecimal delta = debitTotal.subtract(creditTotal).setScale(2, RoundingMode.HALF_UP);
        if (delta.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }

        if (!this.options.hasCashAccountReference())
        {
            transaction.getInfo().put("sclx.generatedCashCounterline", "false");
            transaction.getInfo().put("sclx.unbalancedDelta", delta.toPlainString());
            return;
        }

        String cashRef = this.options.cashAccountReference();
        String cashName = resolveAccountName(cashRef, cashRef);
        AccountingEntry cashEntry;
        if (delta.signum() > 0)
        {
            cashEntry = new AccountingEntry(delta.abs(), cashRef, AccountSide.CREDIT, cashName);
        }
        else
        {
            cashEntry = new AccountingEntry(delta.abs(), cashRef, AccountSide.DEBIT, cashName);
        }

        transaction.addEntry(cashEntry);
        transaction.getInfo().put("sclx.generatedCashCounterline", "true");
        transaction.getInfo().put("sclx.generatedCashCounterline.reason",
            tx.lines().size() == 1 ? "single-sided-source-transaction" : "net-unbalanced-source-transaction");
        transaction.getInfo().put("sclx.generatedCashCounterline.account", cashRef);
    }

    private void storeTransactionInfo(AccountingTransaction target, SclxDocument.Transaction tx)
    {
        Map<String, String> info = target.getInfo();
        putIfPresent(info, "sclx.version", this.document == null ? null : this.document.version());
        putIfPresent(info, "sclx.transactionId", tx.transactionId());
        putIfPresent(info, "sclx.reference", tx.reference());
        putIfPresent(info, "sclx.status", tx.status());
        putIfPresent(info, "sclx.source", tx.source());
        putIfPresent(info, "sclx.bankTiming", tx.bankTiming());
        putIfPresent(info, "sclx.budgetTiming", tx.budgetTiming());
        putIfPresent(info, "sclx.budgetId", tx.budgetId());
        putIfPresent(info, "sclx.eventId", tx.eventId());
        info.put("sclx.originalLineCount", Integer.toString(tx.lines() == null ? 0 : tx.lines().size()));
        if (tx.workbookLink() != null)
        {
            putIfPresent(info, "sclx.workbook.sheetKey", tx.workbookLink().sheetKey());
            if (tx.workbookLink().ledgerRowIndex() != null)
            {
                info.put("sclx.workbook.ledgerRowIndex", tx.workbookLink().ledgerRowIndex().toString());
            }
        }

        Object personName = extractWorkbookValue(tx.extensions(), "personOrBusinessName");
        putIfPresent(info, "sclx.workbook.personOrBusinessName", toStringValue(personName));
        Object merchant = extractWorkbookValue(tx.extensions(), "merchant");
        putIfPresent(info, "sclx.workbook.merchant", toStringValue(merchant));
    }

    private void storeLineInfo(AccountingTransaction target, SclxDocument.TransactionLine line)
    {
        Map<String, String> info = target.getInfo();
        String prefix = "sclx.line." + firstNonBlank(line.lineId(), Integer.toString(info.size()));
        putIfPresent(info, prefix + ".lineId", line.lineId());
        putIfPresent(info, prefix + ".accountId", line.accountId());
        putIfPresent(info, prefix + ".fundId", line.fundId());
        putIfPresent(info, prefix + ".budgetId", line.budgetId());
        putIfPresent(info, prefix + ".personId", line.personId());
        putIfPresent(info, prefix + ".eventId", line.eventId());
        putIfPresent(info, prefix + ".documentId", line.documentId());
        putIfPresent(info, prefix + ".memo", line.memo());
        putIfPresent(info, prefix + ".usedFor", line.usedFor());
        putIfPresent(info, prefix + ".itemNumber", line.itemNumber());
        if (line.quantity() != null)
        {
            info.put(prefix + ".quantity", line.quantity().toString());
        }
        if (line.workbookLink() != null)
        {
            putIfPresent(info, prefix + ".workbook.sheetKey", line.workbookLink().sheetKey());
            if (line.workbookLink().ledgerRowIndex() != null)
            {
                info.put(prefix + ".workbook.ledgerRowIndex", line.workbookLink().ledgerRowIndex().toString());
            }
        }
    }

    private AccountingTransaction resolveSupplementalTransaction(SclxDocument.SupplementalItem item)
    {
        if (item == null)
        {
            return null;
        }

        if (item.ledgerRowIndex() != null)
        {
            AccountingTransaction txn = this.importedTransactionsByWorkbookRow.get(item.ledgerRowIndex());
            if (txn != null)
            {
                return txn;
            }
        }

        if (item.workbookLink() != null && item.workbookLink().ledgerRowIndex() != null)
        {
            return this.importedTransactionsByWorkbookRow.get(item.workbookLink().ledgerRowIndex());
        }

        return null;
    }

    private TxnSupplementalLineBase toSupplementalBean(SclxDocument.SupplementalItem item)
    {
        TxnSupplementalLineBase bean = switch (upper(item.kind()))
        {
            case "RECEIVABLE" -> new ReceivablesLine();
            case "PREPAID_EXPENSE" -> new PrepaidExpenseLine();
            case "OTHER_ASSET" -> new OtherAssetsLine();
            case "DEFERRED_REVENUE" -> new DeferredRevenueLine();
            case "PAYABLE" -> new PayablesLine();
            case "OTHER_LIABILITY" -> new OtherLiabilitiesLine();
            default -> throw new SclxImportException("Unsupported supplemental item kind: " + item.kind());
        };

        Person person = resolvePerson(item.personId(), item.counterpartyName());
        if (person != null)
        {
            bean.setCounterpartyPersonId(person.getId());
        }

        bean.setDescription(firstNonBlank(item.reason(), item.counterpartyName(), item.kind()));
        bean.setReference(firstNonBlank(item.eventBudgetLabel(), item.sourceLabel(), item.budgetId(), item.supplementalItemId()));
        bean.setAmount(safeAmount(item.amountAsOf()));
        bean.setNotes(buildSupplementalNotes(item));
        return bean;
    }

    private String buildSupplementalNotes(SclxDocument.SupplementalItem item)
    {
        List<String> parts = new ArrayList<>();
        addNote(parts, "sclxId", item.supplementalItemId());
        addNote(parts, "kind", item.kind());
        addNote(parts, "year", item.year());
        addNote(parts, "subtypeCode", item.subtypeCode());
        addNote(parts, "budgetId", item.budgetId());
        addNote(parts, "eventBudgetLabel", item.eventBudgetLabel());
        addNote(parts, "sourceLabel", item.sourceLabel());
        addNote(parts, "ledgerRowIndex", item.ledgerRowIndex());
        if (item.workbookLink() != null)
        {
            addNote(parts, "workbookSheet", item.workbookLink().sheetKey());
            addNote(parts, "workbookRow", item.workbookLink().ledgerRowIndex());
        }
        return String.join("; ", parts);
    }

    private Person resolveOrCreatePerson(String name, String email, String phone)
    {
        if (name == null || name.isBlank())
        {
            return null;
        }

        String key = normalizeKey(name);
        Person existing = this.existingPeopleByName.get(key);
        if (existing != null)
        {
            boolean changed = false;
            if ((existing.getEmail() == null || existing.getEmail().isBlank()) && email != null && !email.isBlank())
            {
                existing.setEmail(email);
                changed = true;
            }
            if ((existing.getPhone() == null || existing.getPhone().isBlank()) && phone != null && !phone.isBlank())
            {
                existing.setPhone(phone);
                changed = true;
            }

            if (changed)
            {
                try
                {
                    this.personRepository.save(existing);
                }
                catch (SQLException ex)
                {
                    throw new SclxImportException("Failed to update person " + name, ex);
                }
            }

            return existing;
        }

        Person created = new Person();
        created.setName(name);
        created.setEmail(email);
        created.setPhone(phone);

        try
        {
            Person saved = this.personRepository.save(created);
            this.existingPeopleByName.put(key, saved);
            return saved;
        }
        catch (SQLException ex)
        {
            throw new SclxImportException("Failed to create person " + name, ex);
        }
    }

    private Person resolvePerson(String personId, String fallbackName)
    {
        if (personId != null && !personId.isBlank())
        {
            Person imported = this.importedPeopleBySclxId.get(personId);
            if (imported != null)
            {
                return imported;
            }
        }

        if (fallbackName != null && !fallbackName.isBlank())
        {
            return resolveOrCreatePerson(fallbackName, null, null);
        }

        return null;
    }


private OrganizationRecord toOrganizationRecord(SclxDocument.Organization organization)
{
    return new OrganizationRecord(
        organization.organizationId(),
        organization.name(),
        organization.parentOrganization(),
        organization.baseCurrency(),
        organization.fiscalYearStart(),
        organization.fiscalYearEnd(),
        safeMap(organization.extensions()));
}

private ReportingPeriodRecord toReportingPeriodRecord(SclxDocument.ReportingPeriod reportingPeriod)
{
    return new ReportingPeriodRecord(
        reportingPeriod.startDate(),
        reportingPeriod.endDate(),
        reportingPeriod.label(),
        reportingPeriod.fiscalYear(),
        reportingPeriod.periodType(),
        safeMap(reportingPeriod.extensions()));
}

private FundRecord toFundRecord(SclxDocument.Fund fund)
{
    return new FundRecord(
        fund.fundId(),
        fund.name(),
        Boolean.TRUE.equals(fund.restricted()),
        fund.description(),
        safeMap(fund.extensions()));
}

private EventRecord toEventRecord(SclxDocument.Event event)
{
    return new EventRecord(
        event.eventId(),
        event.name(),
        event.startDate(),
        event.endDate(),
        event.hostingOrganizationId(),
        safeMap(event.extensions()));
}

private DocumentRecord toDocumentRecord(SclxDocument.Document document)
{
    return new DocumentRecord(
        document.documentId(),
        document.documentType(),
        document.referenceNumber(),
        document.documentDate(),
        document.fileName(),
        document.notes(),
        safeMap(document.extensions()));
}

private OutstandingItemRecord toOutstandingItemRecord(SclxDocument.OutstandingItem src)
{
    OutstandingItemRecord.LedgerLinkRef ledgerLink = src.ledgerLink() == null
        ? null
        : new OutstandingItemRecord.LedgerLinkRef(src.ledgerLink().transactionId(), src.ledgerLink().lineId());

    OutstandingItemRecord.WorkbookLinkRef workbookLink = src.workbookLink() == null
        ? null
        : new OutstandingItemRecord.WorkbookLinkRef(src.workbookLink().sheetKey(), src.workbookLink().ledgerRowIndex());

    OutstandingItemRecord.LedgerLinkRef reversalLedgerLink = src.reversalLedgerLink() == null
        ? null
        : new OutstandingItemRecord.LedgerLinkRef(src.reversalLedgerLink().transactionId(), src.reversalLedgerLink().lineId());

    return new OutstandingItemRecord(
        src.outstandingItemId(),
        src.kind(),
        ledgerLink,
        workbookLink,
        src.dateSentOrReceived(),
        src.incomingCheckOrTransferDate(),
        src.transferIdOrCheckNumber(),
        src.dateShowsOnStatement(),
        src.personOrBusinessName(),
        src.detailsNotes(),
        src.fromToCardMerchant(),
        src.accountForPaymentOrDeposit(),
        src.amount(),
        src.dateReversed(),
        src.reversalReasonAndApproval(),
        reversalLedgerLink,
        src.status(),
        safeMap(src.extensions()));
}

private OtherAssetItemRecord toOtherAssetItemRecord(SclxDocument.OtherAssetItem src)
{
    OtherAssetItemRecord.LedgerLinkRef ledgerLink = src.ledgerLink() == null
        ? null
        : new OtherAssetItemRecord.LedgerLinkRef(src.ledgerLink().transactionId(), src.ledgerLink().lineId());

    OtherAssetItemRecord.WorkbookLinkRef workbookLink = src.workbookLink() == null
        ? null
        : new OtherAssetItemRecord.WorkbookLinkRef(src.workbookLink().sheetKey(), src.workbookLink().ledgerRowIndex());

    OtherAssetItemRecord.LedgerLinkRef settlementLedgerLink = src.settlementLedgerLink() == null
        ? null
        : new OtherAssetItemRecord.LedgerLinkRef(src.settlementLedgerLink().transactionId(), src.settlementLedgerLink().lineId());

    return new OtherAssetItemRecord(
        src.otherAssetItemId(),
        ledgerLink,
        workbookLink,
        src.paidTo(),
        src.year(),
        src.reason(),
        src.type(),
        src.typeCode(),
        src.eventBudgetLabel(),
        src.amountAsOfPriorYearEnd(),
        src.paidReturnedOnLedgerRowIndex(),
        settlementLedgerLink,
        src.status(),
        safeMap(src.extensions()));
}

private AssetRecord toAssetRecord(SclxDocument.Asset src)
{
    AssetRecord.GuardianRecord currentGuardian = src.currentGuardian() == null
        ? null
        : new AssetRecord.GuardianRecord(
            src.currentGuardian().legalName(),
            src.currentGuardian().email(),
            src.currentGuardian().phone());

    AssetRecord.GuardianshipDetailsRecord guardianshipDetails = src.guardianshipDetails() == null
        ? null
        : new AssetRecord.GuardianshipDetailsRecord(
            src.guardianshipDetails().dateAsOf(),
            src.guardianshipDetails().confirmed(),
            src.guardianshipDetails().confirmationStatus(),
            src.guardianshipDetails().notes());

    AssetRecord.RemovalDetailsRecord removalDetails = src.removalDetails() == null
        ? null
        : new AssetRecord.RemovalDetailsRecord(
            src.removalDetails().approvedBy(),
            src.removalDetails().approvalDate(),
            src.removalDetails().reason(),
            src.removalDetails().numberRemoved(),
            src.removalDetails().removed(),
            src.removalDetails().removalType());

    return new AssetRecord(
        src.assetId(),
        src.dateAcquired(),
        src.description(),
        src.itemCount(),
        src.approxValueTotal(),
        src.valuePerItem(),
        src.itemType(),
        src.usedFor(),
        src.lotPaidTotal(),
        src.lotItemCount(),
        currentGuardian,
        guardianshipDetails,
        removalDetails,
        safeMap(src.extensions()));
}

private SupplyRecord toSupplyRecord(SclxDocument.Supply src)
{
    SupplyRecord.GuardianRecord guardian = src.guardian() == null
        ? null
        : new SupplyRecord.GuardianRecord(
            src.guardian().legalName(),
            src.guardian().email(),
            src.guardian().phone());

    SupplyRecord.GuardianshipDetailsRecord guardianshipDetails = src.guardianshipDetails() == null
        ? null
        : new SupplyRecord.GuardianshipDetailsRecord(
            src.guardianshipDetails().dateAsOf(),
            src.guardianshipDetails().lastConfirmed(),
            src.guardianshipDetails().returned(),
            src.guardianshipDetails().notes());

    SupplyRecord.RemovalDetailsRecord removalDetails = src.removalDetails() == null
        ? null
        : new SupplyRecord.RemovalDetailsRecord(
            src.removalDetails().approvedBy(),
            src.removalDetails().reason(),
            src.removalDetails().numberRemoved(),
            src.removalDetails().removed(),
            src.removalDetails().removalType());

    return new SupplyRecord(
        src.supplyId(),
        src.itemNumber(),
        src.dateAcquired(),
        src.description(),
        src.count(),
        src.approxValueTotal(),
        src.valuePerItem(),
        guardian,
        guardianshipDetails,
        removalDetails,
        src.additionalNotes(),
        safeMap(src.extensions()));
}


private BudgetRecord toBudgetRecord(SclxDocument.Budget budget)
{
    List<BudgetRecord.BudgetLineRecord> lines = new ArrayList<>();
    if (budget.lines() != null)
    {
        for (SclxDocument.BudgetLine line : budget.lines())
        {
            if (line == null)
            {
                continue;
            }

            lines.add(new BudgetRecord.BudgetLineRecord(
                line.eventName(),
                line.budgetedAmount(),
                parseBudgetRevenueCategory(line.revenueCategory()),
                parseBudgetExpenseCategory(line.expenseCategory()),
                line.accountId(),
                line.notes(),
                safeMap(line.extensions())));
        }
    }

    return new BudgetRecord(
        budget.budgetId(),
        budget.name(),
        budget.fiscalYear() == null ? 0 : budget.fiscalYear(),
        budget.fundId(),
        Boolean.TRUE.equals(budget.active()),
        budget.description(),
        lines,
        safeMap(budget.extensions()));
}

private BankingItemRecord toBankingItemRecord(SclxDocument.BankingItem bankingItem)
{
    BankingItemRecord.OfxTransactionRecord ofx = null;
    if (bankingItem.ofx() != null)
    {
        SclxDocument.OfxTransaction src = bankingItem.ofx();
        ofx = new BankingItemRecord.OfxTransactionRecord(
            src.fitId(),
            src.transactionType(),
            src.datePosted(),
            src.dateUser(),
            src.dateAvailable(),
            src.checkNumber(),
            src.referenceNumber(),
            src.name(),
            src.memo(),
            src.payeeId(),
            src.sic(),
            src.serverTransactionId(),
            src.correctFitId(),
            src.correctAction(),
            safeMap(src.extensions()));
    }

    return new BankingItemRecord(
        bankingItem.bankingItemId(),
        parseBankingItemKind(bankingItem.kind()),
        bankingItem.bankAccountId(),
        bankingItem.transactionId(),
        List.of(),
        deriveClearedDate(bankingItem),
        bankingItem.amount(),
        bankingItem.checkNumber(),
        bankingItem.payee(),
        bankingItem.depositDate(),
        bankingItem.payer(),
        null,
        firstNonBlank(extractString(bankingItem.extensions(), "memo"), extractString(ofx == null ? null : ofx.extensions(), "memo")),
        parseBankingItemSource(bankingItem.source()),
        parseBankingItemStatus(bankingItem.status()),
        bankingItem.importId(),
        ofx,
        safeMap(bankingItem.extensions()));
}

private BankStatementRecord toBankStatementRecord(SclxDocument.BankStatementImport src)
{
    BankStatementRecord.BankAccountRef bankAccount = null;
    if (src.bankAccount() != null)
    {
        bankAccount = new BankStatementRecord.BankAccountRef(
            src.bankAccount().bankId(),
            src.bankAccount().accountId(),
            src.bankAccount().accountType());
    }

    BankStatementRecord.BalanceSnapshot ledgerBalance = null;
    if (src.ledgerBalance() != null)
    {
        ledgerBalance = new BankStatementRecord.BalanceSnapshot(
            src.ledgerBalance().amount(),
            src.ledgerBalance().asOf());
    }

    BankStatementRecord.BalanceSnapshot availableBalance = null;
    if (src.availableBalance() != null)
    {
        availableBalance = new BankStatementRecord.BalanceSnapshot(
            src.availableBalance().amount(),
            src.availableBalance().asOf());
    }

    return new BankStatementRecord(
        src.importId(),
        parseSourceFormat(src.sourceFormat()),
        src.sourceVersion(),
        parseStatementKind(src.statementKind()),
        bankAccount,
        src.currency(),
        src.statementStart(),
        src.statementEnd(),
        ledgerBalance,
        availableBalance,
        src.documentId(),
        safeMap(src.extensions()));
}

private BudgetRecord.BudgetRevenueCategory parseBudgetRevenueCategory(String value)
{
    if (value == null || value.isBlank())
    {
        return BudgetRecord.BudgetRevenueCategory.GeneralRevenue;
    }

    try
    {
        return BudgetRecord.BudgetRevenueCategory.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return BudgetRecord.BudgetRevenueCategory.GeneralRevenue;
    }
}

private BudgetRecord.BudgetExpenseCategory parseBudgetExpenseCategory(String value)
{
    if (value == null || value.isBlank())
    {
        return BudgetRecord.BudgetExpenseCategory.Overhead;
    }

    try
    {
        return BudgetRecord.BudgetExpenseCategory.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return BudgetRecord.BudgetExpenseCategory.Overhead;
    }
}

private BankingItemRecord.BankingItemKind parseBankingItemKind(String value)
{
    if (value == null || value.isBlank())
    {
        return BankingItemRecord.BankingItemKind.ADJUSTMENT;
    }

    try
    {
        return BankingItemRecord.BankingItemKind.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return BankingItemRecord.BankingItemKind.ADJUSTMENT;
    }
}

private BankingItemRecord.BankingItemSource parseBankingItemSource(String value)
{
    if (value == null || value.isBlank())
    {
        return null;
    }

    try
    {
        return BankingItemRecord.BankingItemSource.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return null;
    }
}

private BankingItemRecord.BankingItemStatus parseBankingItemStatus(String value)
{
    if (value == null || value.isBlank())
    {
        return null;
    }

    try
    {
        return BankingItemRecord.BankingItemStatus.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return null;
    }
}

private BankStatementRecord.SourceFormat parseSourceFormat(String value)
{
    if (value == null || value.isBlank())
    {
        return BankStatementRecord.SourceFormat.OFX;
    }

    try
    {
        return BankStatementRecord.SourceFormat.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return BankStatementRecord.SourceFormat.OFX;
    }
}

private BankStatementRecord.StatementKind parseStatementKind(String value)
{
    if (value == null || value.isBlank())
    {
        return BankStatementRecord.StatementKind.BANK;
    }

    try
    {
        return BankStatementRecord.StatementKind.valueOf(value);
    }
    catch (IllegalArgumentException ex)
    {
        return BankStatementRecord.StatementKind.BANK;
    }
}

private LocalDate deriveClearedDate(SclxDocument.BankingItem bankingItem)
{
    LocalDate fromExtensions = null;
    String extensionDate = extractString(bankingItem.extensions(), "clearedDate");
    if (extensionDate != null)
    {
        try
        {
            fromExtensions = LocalDate.parse(extensionDate);
        }
        catch (Exception ex)
        {
            fromExtensions = null;
        }
    }

    if (fromExtensions != null)
    {
        return fromExtensions;
    }
    if (bankingItem.depositDate() != null)
    {
        return bankingItem.depositDate();
    }
    if (bankingItem.ofx() != null && bankingItem.ofx().datePosted() != null)
    {
        return bankingItem.ofx().datePosted();
    }
    return this.document != null && this.document.reportingPeriod() != null
        ? this.document.reportingPeriod().endDate()
        : LocalDate.now();
}

private Map<String, Object> safeMap(Map<String, Object> value)
{
    return value == null ? Map.of() : value;
}

private String extractString(Map<String, Object> map, String key)
{
    if (map == null || key == null)
    {
        return null;
    }
    Object value = map.get(key);
    return value == null ? null : String.valueOf(value);
}

    private void persistJsonDocument(String name, Object value)
    {
        if (value == null)
        {
            return;
        }

        try
        {
            this.documentRepository.upsert(name, this.mapper.writeValueAsString(value));
        }
        catch (SQLException | IOException ex)
        {
            throw new SclxImportException("Failed to persist document " + name, ex);
        }
    }

    private String resolveSclxAccountReference(String preferred, String alternate, String fallback)
    {
        String raw = firstNonBlank(preferred, alternate, fallback);
        return raw == null ? null : this.options.resolveAccountReference(raw);
    }

    private String resolveAccountName(String sclxReference, String resolvedAccountNumber)
    {
        String fromSclx = firstNonBlank(
            this.sclxAccountNameByRef.get(sclxReference),
            this.sclxAccountNameByRef.get(resolvedAccountNumber));
        if (fromSclx != null)
        {
            return fromSclx;
        }

        Account existing = this.existingAccountsByRef.get(normalizeKey(resolvedAccountNumber));
        if (existing != null && existing.getName() != null && !existing.getName().isBlank())
        {
            return existing.getName();
        }

        return resolvedAccountNumber;
    }

    private String resolveFundName(String fundId)
    {
        return firstNonBlank(this.fundNameById.get(fundId), fundId);
    }

    private String resolveBudgetName(String budgetId)
    {
        return firstNonBlank(this.budgetNameById.get(budgetId), budgetId);
    }

    private String resolveTransactionToFrom(SclxDocument.Transaction tx)
    {
        String workbookName = toStringValue(extractWorkbookValue(tx.extensions(), "personOrBusinessName"));
        if (workbookName != null && !workbookName.isBlank())
        {
            return workbookName;
        }

        String workbookMerchant = toStringValue(extractWorkbookValue(tx.extensions(), "merchant"));
        if (workbookMerchant != null && !workbookMerchant.isBlank())
        {
            return workbookMerchant;
        }

        if (tx.lines() != null)
        {
            for (SclxDocument.TransactionLine line : tx.lines())
            {
                Person person = resolvePerson(line.personId(), null);
                if (person != null && person.getName() != null && !person.getName().isBlank())
                {
                    return person.getName();
                }
            }
        }

        return firstNonBlank(tx.reference(), tx.description(), tx.transactionId());
    }

    private String firstLineFundId(SclxDocument.Transaction tx)
    {
        if (tx.lines() == null)
        {
            return null;
        }

        for (SclxDocument.TransactionLine line : tx.lines())
        {
            if (line != null && line.fundId() != null && !line.fundId().isBlank())
            {
                return line.fundId();
            }
        }
        return null;
    }

    private String firstLineBudgetId(SclxDocument.Transaction tx)
    {
        if (tx.lines() == null)
        {
            return null;
        }

        for (SclxDocument.TransactionLine line : tx.lines())
        {
            if (line != null && line.budgetId() != null && !line.budgetId().isBlank())
            {
                return line.budgetId();
            }
        }
        return null;
    }

    private static AccountType parseAccountType(String value)
    {
        if (value == null || value.isBlank())
        {
            return AccountType.UNKNOWN;
        }

        return switch (upper(value))
        {
            case "ASSET" -> AccountType.ASSET;
            case "LIABILITY" -> AccountType.LIABILITY;
            case "EXPENSE" -> AccountType.EXPENSE;
            case "REVENUE", "INCOME" -> AccountType.INCOME;
            case "NET_ASSETS", "EQUITY" -> AccountType.EQUITY;
            case "BANK", "CHECKING" -> AccountType.BANK;
            case "CASH" -> AccountType.CASH;
            default -> AccountType.fromString(upper(value));
        };
    }

    private static AccountSide parseAccountSide(String increaseSide, String accountType)
    {
        if (increaseSide != null && !increaseSide.isBlank())
        {
            return AccountSide.fromString(upper(increaseSide));
        }

        return switch (upper(accountType))
        {
            case "ASSET", "EXPENSE", "CASH", "BANK", "CHECKING", "FIXED_ASSET" -> AccountSide.DEBIT;
            case "LIABILITY", "REVENUE", "INCOME", "NET_ASSETS", "EQUITY", "CREDITCARD", "CREDIT" -> AccountSide.CREDIT;
            default -> AccountSide.UNKNOWN;
        };
    }

    private static LocalDate firstNonNull(LocalDate a, LocalDate b)
    {
        return a != null ? a : b;
    }

    private static long epochMillis(LocalDate date)
    {
        LocalDate value = date == null ? LocalDate.now() : date;
        return value.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private static BigDecimal safeAmount(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String stringDate(LocalDate date)
    {
        return date == null ? LocalDate.now().toString() : date.toString();
    }

    private static String blankToEmpty(String value)
    {
        return value == null ? "" : value;
    }

    private static String normalizeKey(String value)
    {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String upper(String value)
    {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }

        for (String value : values)
        {
            if (value != null && !value.isBlank())
            {
                return value;
            }
        }
        return null;
    }

    private static void putIfPresent(Map<String, String> map, String key, String value)
    {
        if (map == null || key == null || value == null || value.isBlank())
        {
            return;
        }
        map.put(key, value);
    }

    private static void putIfPresent(Map<String, Account> map, String key, Account value)
    {
        if (map == null || key == null || key.isBlank() || value == null)
        {
            return;
        }
        map.put(normalizeKey(key), value);
    }

    private static Object extractWorkbookValue(Map<String, Object> extensions, String fieldName)
    {
        if (extensions == null || extensions.isEmpty() || fieldName == null || fieldName.isBlank())
        {
            return null;
        }

        Object workbook = extensions.get("workbook");
        if (workbook instanceof Map<?, ?> workbookMap)
        {
            return workbookMap.get(fieldName);
        }

        return null;
    }

    private static String extractWorkbookString(Map<String, Object> extensions, String fieldName)
    {
        return toStringValue(extractWorkbookValue(extensions, fieldName));
    }

    private static String toStringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private static void addNote(Collection<String> parts, String key, Object value)
    {
        if (parts == null || key == null || key.isBlank() || value == null)
        {
            return;
        }
        String s = String.valueOf(value);
        if (!s.isBlank())
        {
            parts.add(key + "=" + s);
        }
    }
}
