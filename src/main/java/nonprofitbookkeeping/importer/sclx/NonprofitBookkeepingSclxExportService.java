/*
 * 
 */
package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.impex.BankStatementRecord;
import nonprofitbookkeeping.model.impex.BankingItemRecord;
import nonprofitbookkeeping.model.impex.BudgetRecord;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.JournalRepository;
import nonprofitbookkeeping.persistence.PersonRepository;
import nonprofitbookkeeping.persistence.impex.BankStatementRecordRepository;
import nonprofitbookkeeping.persistence.impex.BankingItemRecordRepository;
import nonprofitbookkeeping.persistence.impex.BudgetRecordRepository;
import nonprofitbookkeeping.service.FundAccountingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Exports the current NonprofitBookkeeping state to an SCLX document.
 *
 * <p>This service focuses on the modeled parts of the current archive:
 * accounts, funds, people, transactions, supplemental lines, staged budget
 * records, staged banking items, and staged bank statement imports.
 */
public class NonprofitBookkeepingSclxExportService
{
    
    /** The account repository. */
    private final AccountRepository accountRepository;
    
    /** The person repository. */
    private final PersonRepository personRepository;
    
    /** The fund accounting service. */
    private final FundAccountingService fundAccountingService;
    
    /** The budget record repository. */
    private final BudgetRecordRepository budgetRecordRepository;
    
    /** The banking item record repository. */
    private final BankingItemRecordRepository bankingItemRecordRepository;
    
    /** The bank statement record repository. */
    private final BankStatementRecordRepository bankStatementRecordRepository;

    /**
     * Instantiates a new nonprofit bookkeeping sclx export service.
     */
    public NonprofitBookkeepingSclxExportService()
    {
        this(
            new AccountRepository(),
            new PersonRepository(),
            new FundAccountingService(),
            new BudgetRecordRepository(),
            new BankingItemRecordRepository(),
            new BankStatementRecordRepository()
        );
    }

    /**
     * Instantiates a new nonprofit bookkeeping sclx export service.
     *
     * @param accountRepository the account repository
     * @param personRepository the person repository
     * @param fundAccountingService the fund accounting service
     * @param budgetRecordRepository the budget record repository
     * @param bankingItemRecordRepository the banking item record repository
     * @param bankStatementRecordRepository the bank statement record repository
     */
    public NonprofitBookkeepingSclxExportService(
        AccountRepository accountRepository,
        PersonRepository personRepository,
        FundAccountingService fundAccountingService,
        BudgetRecordRepository budgetRecordRepository,
        BankingItemRecordRepository bankingItemRecordRepository,
        BankStatementRecordRepository bankStatementRecordRepository)
    {
        this.accountRepository = accountRepository;
        this.personRepository = personRepository;
        this.fundAccountingService = fundAccountingService;
        this.budgetRecordRepository = budgetRecordRepository;
        this.bankingItemRecordRepository = bankingItemRecordRepository;
        this.bankStatementRecordRepository = bankStatementRecordRepository;
    }

    /**
     * Export document.
     *
     * @return the sclx document
     * @throws SQLException the SQL exception
     */
    public SclxDocument exportDocument() throws java.sql.SQLException
    {
        Company company = CurrentCompany.getCompany();
        CompanyProfileModel profile = company == null ? null : company.getCompanyProfileModel();

        SclxDocument.Organization organization = new SclxDocument.Organization(
            "org-" + safe(profile == null ? null : profile.getCompanyName()),
            profile == null ? null : profile.getCompanyName(),
            null,
            profile == null ? null : profile.getBaseCurrency(),
            null,
            null,
            Map.of()
        );

        SclxDocument.ReportingPeriod reportingPeriod = new SclxDocument.ReportingPeriod(
            null,
            null,
            null,
            null,
            null,
            Map.of()
        );

        List<SclxDocument.Account> accounts = new ArrayList<>();
        for (Account account : accountRepository.listAll())
        {
            accounts.add(new SclxDocument.Account(
                account.getAccountNumber(),
                account.getName(),
                account.getAccountType() == null ? null : account.getAccountType().name(),
                account.getParentAccountId(),
                account.getIncreaseSide() == null ? null : account.getIncreaseSide().name(),
                account.getOpeningBalance(),
                account.getSupplementalLineKinds() == null ? List.of() : account.getSupplementalLineKinds().stream().map(Enum::name).toList(),
                account.getAccountNumber(),
                account.getAccountCode(),
                null,
                Boolean.TRUE,
                List.of(),
                Map.of()
            ));
        }

        try
		{        	
			fundAccountingService.loadFunds(null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        
        List<SclxDocument.Fund> funds = fundAccountingService.listFunds().stream()
            .map(fund -> new SclxDocument.Fund(fund.getFundId(), fund.getName(), null, null, Map.of()))
            .toList();

        List<SclxDocument.Budget> budgets = budgetRecordRepository.listAll().stream()
            .map(this::toSclxBudget)
            .toList();

        List<SclxDocument.Person> people = buildSclxPeople();

        List<SclxDocument.Transaction> transactions = JournalRepository.listTransactions().stream()
            .map(this::toSclxTransaction)
            .toList();

        List<SclxDocument.SupplementalItem> supplementalItems = new ArrayList<>();
        for (AccountingTransaction txn : JournalRepository.listTransactions())
        {
            for (TxnSupplementalLineBase line : txn.getSupplementalLines())
            {
                supplementalItems.add(new SclxDocument.SupplementalItem(
                    "supp-" + txn.getId() + "-" + line.getKind().name() + "-" + line.getId(),
                    line.getKind().name(),
                    null,
                    line.getCounterpartyPersonId() == null ? null : "person-" + line.getCounterpartyPersonId(),
                    line.getDueDate() == null ? null : line.getDueDate().getYear(),
                    line.getDescription(),
                    null,
                    null,
                    null,
                    line.getReference(),
                    line.getAmount(),
                    parseInteger(txn.getInfo() == null ? null : txn.getInfo().get("sclx.ledgerRowIndex")),
                    new SclxDocument.WorkbookLink(
                        txn.getInfo() == null ? null : txn.getInfo().get("sclx.sheetKey"),
                        parseInteger(txn.getInfo() == null ? null : txn.getInfo().get("sclx.ledgerRowIndex"))
                    ),
                    Map.of()
                ));
            }
        }

        List<SclxDocument.BankingItem> bankingItems = bankingItemRecordRepository.listAll().stream()
            .map(this::toSclxBankingItem)
            .toList();

        List<SclxDocument.BankStatementImport> bankStatementImports = bankStatementRecordRepository.listAll().stream()
            .map(this::toSclxBankStatementImport)
            .toList();

        return new SclxDocument(
            "SCLX",
            "1.3",
            OffsetDateTime.now(ZoneOffset.UTC),
            null,
            organization,
            reportingPeriod,
            accounts,
            funds,
            budgets,
            people,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            transactions,
            bankingItems,
            List.of(),
            List.of(),
            supplementalItems,
            List.of(),
            List.of(),
            bankStatementImports,
            Map.<String, Object>of()
        );
    }

    /**
     * To sclx budget.
     *
     * @param row the row
     * @return the sclx document. budget
     */
    private SclxDocument.Budget toSclxBudget(BudgetRecord row)
    {
        return new SclxDocument.Budget(
            row.budgetId(),
            row.name(),
            row.fiscalYear(),
            row.fundId(),
            row.active(),
            row.description(),
            row.lines().stream()
                .map(line -> new SclxDocument.BudgetLine(
                    line.eventName(),
                    line.budgetedAmount(),
                    line.revenueCategory(),
                    line.expenseCategory(),
                    line.accountId(),
                    line.notes(),
                    line.extensions()))
                .toList(),
            row.extensions()
        );
    }

    
/**
 * To sclx transaction.
 *
 * @param txn the txn
 * @return the sclx document. transaction
 */
private SclxDocument.Transaction toSclxTransaction(AccountingTransaction txn)
{
    String personDisplayName = blankToNull(txn.getToFrom());
    String personId = resolveExportPersonId(personDisplayName);
    String checkNumber = blankToNull(txn.getCheckNumber());
    String reference = checkNumber;
    String checkNumberId = normalizeId("check-", checkNumber);

    List<SclxDocument.TransactionLine> lines = new ArrayList<>();
    for (AccountingEntry entry : txn.getEntries())
    {
        BigDecimal debit = entry.getAccountSide() == AccountSide.DEBIT ? entry.getAmount() : BigDecimal.ZERO;
        BigDecimal credit = entry.getAccountSide() == AccountSide.CREDIT ? entry.getAmount() : BigDecimal.ZERO;

        lines.add(new SclxDocument.TransactionLine(
            "txn-" + txn.getId() + "-line-" + lines.size(),
            entry.getAccountNumber(),
            debit,
            credit,
            entry.getFundNumber(),
            txn.getBudgetTracking(),
            personId,
            null,
            null,
            txn.getMemo(),
            List.of(),
            null,
            null,
            null,
            null,
            null,
            txn.getInfo() == null ? null : new SclxDocument.WorkbookLink(
                txn.getInfo().get("sclx.sheetKey"),
                parseInteger(txn.getInfo().get("sclx.ledgerRowIndex"))
            ),
            List.of(),
            Map.of()
        ));
    }

    return new SclxDocument.Transaction(
        "txn-" + txn.getId(),
        parseDate(txn.getDate()),
        parseDate(txn.getDate()),
        txn.getMemo(),
        reference,
        checkNumber,
        checkNumberId,
        personId,
        personDisplayName,
        personDisplayName,
        txn.isBalanced() ? "POSTED" : "WORKSHEET_NATIVE",
        txn.getInfo() == null ? null : txn.getInfo().get("sclx.source"),
        txn.getBank(),
        txn.getClearBank(),
        txn.getBudgetTracking(),
        txn.getInfo() == null ? null : new SclxDocument.WorkbookLink(
            txn.getInfo().get("sclx.sheetKey"),
            parseInteger(txn.getInfo().get("sclx.ledgerRowIndex"))
        ),
        null,
        List.of(),
        null,
        lines,
        Map.of()
    );
}

/**
 * To sclx banking item.
 *
 * @param row the row
 * @return the sclx document. banking item
 */
private SclxDocument.BankingItem toSclxBankingItem(BankingItemRecord row)
    {
        return new SclxDocument.BankingItem(
            row.bankingItemId(),
            row.kind(),
            row.source(),
            row.status(),
            row.transactionId(),
            row.bankAccountId(),
            row.depositDate(),
            row.payer(),
            row.payee(),
            row.checkNumber(),
            row.amount(),
            row.importId(),
            row.ofx() == null ? null : new SclxDocument.OfxTransaction(
                row.ofx().fitId(),
                row.ofx().transactionType(),
                row.ofx().datePosted(),
                row.ofx().dateUser(),
                row.ofx().dateAvailable(),
                row.ofx().checkNumber(),
                row.ofx().referenceNumber(),
                row.ofx().name(),
                row.ofx().memo(),
                row.ofx().payeeId(),
                row.ofx().sic(),
                row.ofx().serverTransactionId(),
                row.ofx().correctFitId(),
                row.ofx().correctAction(),
                row.ofx().extensions()
            ),
            row.extensions()
        );
    }

    /**
     * To sclx bank statement import.
     *
     * @param row the row
     * @return the sclx document. bank statement import
     */
    private SclxDocument.BankStatementImport toSclxBankStatementImport(BankStatementRecord row)
    {
        return new SclxDocument.BankStatementImport(
            row.importId(),
            row.sourceFormat() == null ? null : row.sourceFormat().name(),
            row.sourceVersion(),
            row.statementKind() == null ? null : row.statementKind().name(),
            row.bankAccount() == null ? null : new SclxDocument.BankAccountRef(
                row.bankAccount().bankId(),
                row.bankAccount().accountId(),
                row.bankAccount().accountType()
            ),
            row.currency(),
            row.statementStart(),
            row.statementEnd(),
            row.ledgerBalance() == null ? null : new SclxDocument.StatementBalance(row.ledgerBalance().amount(), row.ledgerBalance().asOf()),
            row.availableBalance() == null ? null : new SclxDocument.StatementBalance(row.availableBalance().amount(), row.availableBalance().asOf()),
            row.documentId(),
            row.extensions()
        );
    }


/**
 * Builds the sclx people.
 *
 * @return the list
 * @throws SQLException the SQL exception
 */
private List<SclxDocument.Person> buildSclxPeople() throws java.sql.SQLException
{
    Map<String, SclxDocument.Person> byId = new LinkedHashMap<>();

    for (Person person : personRepository.list())
    {
        String personId = "person-" + person.getId();
        byId.put(personId, new SclxDocument.Person(
            personId,
            person.getName(),
            "OTHER",
            person.getEmail(),
            person.getPhone(),
            Map.of()
        ));
    }

    for (AccountingTransaction txn : JournalRepository.listTransactions())
    {
        String displayName = blankToNull(txn.getToFrom());
        if (displayName == null)
        {
            continue;
        }

        String personId = resolveExportPersonId(displayName);
        if (personId == null || byId.containsKey(personId))
        {
            continue;
        }

        byId.put(personId, new SclxDocument.Person(
            personId,
            displayName,
            "OTHER",
            null,
            null,
            Map.of()
        ));
    }

    return new ArrayList<>(byId.values());
}

/**
 * Resolve export person id.
 *
 * @param displayName the display name
 * @return the string
 */
private String resolveExportPersonId(String displayName)
{
    if (displayName == null || displayName.isBlank())
    {
        return null;
    }

    try
    {
        for (Person person : personRepository.list())
        {
            if (person.getName() != null && person.getName().equalsIgnoreCase(displayName))
            {
                return "person-" + person.getId();
            }
        }
    }
    catch (java.sql.SQLException ex)
    {
        throw new IllegalStateException("Failed to resolve export person id for " + displayName, ex);
    }

    return normalizeId("person-", displayName);
}

/**
 * Normalize id.
 *
 * @param prefix the prefix
 * @param raw the raw
 * @return the string
 */
private static String normalizeId(String prefix, String raw)
{
    String value = blankToNull(raw);
    if (value == null)
    {
        return null;
    }
    return prefix + safe(value);
}

/**
 * Blank to null.
 *
 * @param value the value
 * @return the string
 */
private static String blankToNull(String value)
{
    return value == null || value.isBlank() ? null : value;
}

    /**
     * Parses the date.
     *
     * @param value the value
     * @return the local date
     */
    private static LocalDate parseDate(String value)
    {
        try
        {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Parses the integer.
     *
     * @param value the value
     * @return the integer
     */
    private static Integer parseInteger(String value)
    {
        try
        {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Safe.
     *
     * @param value the value
     * @return the string
     */
    private static String safe(String value)
    {
        return value == null ? "unknown" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
