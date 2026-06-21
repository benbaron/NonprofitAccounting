package nonprofitbookkeeping.importer.sclx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.model.records.BudgetRecord;
import nonprofitbookkeeping.model.supplemental.*;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.persistence.PersonRepository;
import nonprofitbookkeeping.persistence.records.BankStatementRecordRepository;
import nonprofitbookkeeping.persistence.records.BankingItemRecordRepository;
import nonprofitbookkeeping.persistence.records.BudgetRecordRepository;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineMapper;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineRecord;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineRepository;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.service.scaledger.JournalLedgerPersistenceGateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete SCLX import target for NonprofitBookkeeping.
 */
public class NonprofitBookkeepingSclxImportTarget implements SclxImportTarget
{
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final JournalLedgerPersistenceGateway journalGateway;
    private final TxnSupplementalLineRepository supplementalRepository;
    private final BudgetRecordRepository budgetRecordRepository;
    private final BankingItemRecordRepository bankingItemRecordRepository;
    private final BankStatementRecordRepository bankStatementRecordRepository;
    private final DocumentRepository documentRepository;
    private final FundAccountingService fundAccountingService;
    private final nonprofitbookkeeping.persistence.sclx.OrganizationRepository sclxOrganizationRepository;
    private final nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository sclxReportingPeriodRepository;
    private final nonprofitbookkeeping.persistence.sclx.CompatibilityRepository sclxCompatibilityRepository;
    private final nonprofitbookkeeping.persistence.sclx.BankAccountRepository sclxBankAccountRepository;
    private final nonprofitbookkeeping.persistence.sclx.OfficeAssignmentRepository sclxOfficeAssignmentRepository;
    private final nonprofitbookkeeping.persistence.sclx.CommitteeMembershipRepository sclxCommitteeMembershipRepository;
    private final nonprofitbookkeeping.persistence.sclx.EventRepository sclxEventRepository;
    private final nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository;
    private final nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository sclxOutstandingItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository sclxOtherAssetItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.AssetRepository sclxAssetRepository;
    private final nonprofitbookkeeping.persistence.sclx.SupplyRepository sclxSupplyRepository;

    private final Map<String, Integer> importedTransactionIdsBySclxId = new LinkedHashMap<>();
    private final Map<Integer, Integer> importedTransactionIdsByLedgerRow = new LinkedHashMap<>();
    private final Map<String, Long> personDbIdBySclxPersonId = new LinkedHashMap<>();
    private final Map<String, String> personDisplayNameBySclxPersonId = new LinkedHashMap<>();
    private final Map<String, Integer> rawStagingWriteCounts = new LinkedHashMap<>();
    private final List<String> importWarnings = new ArrayList<>();
    private SclxImportOptions currentOptions = SclxImportOptions.defaults();
    private String currentImportRunId = SclxImportOptions.defaults().effectiveImportRunId();

    public static final class Dependencies
    {
        private final AccountRepository accountRepository;
        private final PersonRepository personRepository;
        private final JournalLedgerPersistenceGateway journalGateway;
        private final TxnSupplementalLineRepository supplementalRepository;
        private final BudgetRecordRepository budgetRecordRepository;
        private final BankingItemRecordRepository bankingItemRecordRepository;
        private final BankStatementRecordRepository bankStatementRecordRepository;
        private final DocumentRepository documentRepository;
        private final FundAccountingService fundAccountingService;
        private final nonprofitbookkeeping.persistence.sclx.OrganizationRepository sclxOrganizationRepository;
        private final nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository sclxReportingPeriodRepository;
        private final nonprofitbookkeeping.persistence.sclx.CompatibilityRepository sclxCompatibilityRepository;
        private final nonprofitbookkeeping.persistence.sclx.BankAccountRepository sclxBankAccountRepository;
        private final nonprofitbookkeeping.persistence.sclx.OfficeAssignmentRepository sclxOfficeAssignmentRepository;
        private final nonprofitbookkeeping.persistence.sclx.CommitteeMembershipRepository sclxCommitteeMembershipRepository;
        private final nonprofitbookkeeping.persistence.sclx.EventRepository sclxEventRepository;
        private final nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository;
        private final nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository sclxOutstandingItemRepository;
        private final nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository sclxOtherAssetItemRepository;
        private final nonprofitbookkeeping.persistence.sclx.AssetRepository sclxAssetRepository;
        private final nonprofitbookkeeping.persistence.sclx.SupplyRepository sclxSupplyRepository;

        public Dependencies(
            AccountRepository accountRepository,
            PersonRepository personRepository,
            JournalLedgerPersistenceGateway journalGateway,
            TxnSupplementalLineRepository supplementalRepository,
            BudgetRecordRepository budgetRecordRepository,
            BankingItemRecordRepository bankingItemRecordRepository,
            BankStatementRecordRepository bankStatementRecordRepository,
            DocumentRepository documentRepository,
            FundAccountingService fundAccountingService,
            nonprofitbookkeeping.persistence.sclx.OrganizationRepository sclxOrganizationRepository,
            nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository sclxReportingPeriodRepository,
            nonprofitbookkeeping.persistence.sclx.CompatibilityRepository sclxCompatibilityRepository,
            nonprofitbookkeeping.persistence.sclx.BankAccountRepository sclxBankAccountRepository,
            nonprofitbookkeeping.persistence.sclx.OfficeAssignmentRepository sclxOfficeAssignmentRepository,
            nonprofitbookkeeping.persistence.sclx.CommitteeMembershipRepository sclxCommitteeMembershipRepository,
            nonprofitbookkeeping.persistence.sclx.EventRepository sclxEventRepository,
            nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository,
            nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository sclxOutstandingItemRepository,
            nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository sclxOtherAssetItemRepository,
            nonprofitbookkeeping.persistence.sclx.AssetRepository sclxAssetRepository,
            nonprofitbookkeeping.persistence.sclx.SupplyRepository sclxSupplyRepository)
        {
            this.accountRepository = Objects.requireNonNull(accountRepository);
            this.personRepository = Objects.requireNonNull(personRepository);
            this.journalGateway = Objects.requireNonNull(journalGateway);
            this.supplementalRepository = Objects.requireNonNull(supplementalRepository);
            this.budgetRecordRepository = Objects.requireNonNull(budgetRecordRepository);
            this.bankingItemRecordRepository = Objects.requireNonNull(bankingItemRecordRepository);
            this.bankStatementRecordRepository = Objects.requireNonNull(bankStatementRecordRepository);
            this.documentRepository = Objects.requireNonNull(documentRepository);
            this.fundAccountingService = Objects.requireNonNull(fundAccountingService);
            this.sclxOrganizationRepository = Objects.requireNonNull(sclxOrganizationRepository);
            this.sclxReportingPeriodRepository = Objects.requireNonNull(sclxReportingPeriodRepository);
            this.sclxCompatibilityRepository = Objects.requireNonNull(sclxCompatibilityRepository);
            this.sclxBankAccountRepository = Objects.requireNonNull(sclxBankAccountRepository);
            this.sclxOfficeAssignmentRepository = Objects.requireNonNull(sclxOfficeAssignmentRepository);
            this.sclxCommitteeMembershipRepository = Objects.requireNonNull(sclxCommitteeMembershipRepository);
            this.sclxEventRepository = Objects.requireNonNull(sclxEventRepository);
            this.sclxDocumentRepository = Objects.requireNonNull(sclxDocumentRepository);
            this.sclxOutstandingItemRepository = Objects.requireNonNull(sclxOutstandingItemRepository);
            this.sclxOtherAssetItemRepository = Objects.requireNonNull(sclxOtherAssetItemRepository);
            this.sclxAssetRepository = Objects.requireNonNull(sclxAssetRepository);
            this.sclxSupplyRepository = Objects.requireNonNull(sclxSupplyRepository);
        }
    }

    private static Dependencies defaultDependencies()
    {
        return new Dependencies(
            new AccountRepository(),
            new PersonRepository(),
            new JournalLedgerPersistenceGateway(),
            new TxnSupplementalLineRepository(),
            new BudgetRecordRepository(),
            new BankingItemRecordRepository(),
            new BankStatementRecordRepository(),
            new DocumentRepository(),
            new FundAccountingService(),
            new nonprofitbookkeeping.persistence.sclx.OrganizationRepository(),
            new nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository(),
            new nonprofitbookkeeping.persistence.sclx.CompatibilityRepository(),
            new nonprofitbookkeeping.persistence.sclx.BankAccountRepository(),
            new nonprofitbookkeeping.persistence.sclx.OfficeAssignmentRepository(),
            new nonprofitbookkeeping.persistence.sclx.CommitteeMembershipRepository(),
            new nonprofitbookkeeping.persistence.sclx.EventRepository(),
            new nonprofitbookkeeping.persistence.sclx.DocumentRepository(),
            new nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository(),
            new nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository(),
            new nonprofitbookkeeping.persistence.sclx.AssetRepository(),
            new nonprofitbookkeeping.persistence.sclx.SupplyRepository());
    }

    public NonprofitBookkeepingSclxImportTarget()
    {
        this(defaultDependencies());
    }

    public NonprofitBookkeepingSclxImportTarget(Dependencies dependencies)
    {
        this.accountRepository = dependencies.accountRepository;
        this.personRepository = dependencies.personRepository;
        this.journalGateway = dependencies.journalGateway;
        this.supplementalRepository = dependencies.supplementalRepository;
        this.budgetRecordRepository = dependencies.budgetRecordRepository;
        this.bankingItemRecordRepository = dependencies.bankingItemRecordRepository;
        this.bankStatementRecordRepository = dependencies.bankStatementRecordRepository;
        this.documentRepository = dependencies.documentRepository;
        this.fundAccountingService = dependencies.fundAccountingService;
        this.sclxOrganizationRepository = dependencies.sclxOrganizationRepository;
        this.sclxReportingPeriodRepository = dependencies.sclxReportingPeriodRepository;
        this.sclxCompatibilityRepository = dependencies.sclxCompatibilityRepository;
        this.sclxBankAccountRepository = dependencies.sclxBankAccountRepository;
        this.sclxOfficeAssignmentRepository = dependencies.sclxOfficeAssignmentRepository;
        this.sclxCommitteeMembershipRepository = dependencies.sclxCommitteeMembershipRepository;
        this.sclxEventRepository = dependencies.sclxEventRepository;
        this.sclxDocumentRepository = dependencies.sclxDocumentRepository;
        this.sclxOutstandingItemRepository = dependencies.sclxOutstandingItemRepository;
        this.sclxOtherAssetItemRepository = dependencies.sclxOtherAssetItemRepository;
        this.sclxAssetRepository = dependencies.sclxAssetRepository;
        this.sclxSupplyRepository = dependencies.sclxSupplyRepository;
    }

    public NonprofitBookkeepingSclxImportTarget(
        AccountRepository accountRepository,
        PersonRepository personRepository,
        JournalLedgerPersistenceGateway journalGateway,
        TxnSupplementalLineRepository supplementalRepository,
        BudgetRecordRepository budgetRecordRepository,
        BankingItemRecordRepository bankingItemRecordRepository,
        BankStatementRecordRepository bankStatementRecordRepository,
        DocumentRepository documentRepository,
        FundAccountingService fundAccountingService,
        nonprofitbookkeeping.persistence.sclx.OrganizationRepository sclxOrganizationRepository,
        nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository sclxReportingPeriodRepository,
        nonprofitbookkeeping.persistence.sclx.CompatibilityRepository sclxCompatibilityRepository,
        nonprofitbookkeeping.persistence.sclx.BankAccountRepository sclxBankAccountRepository,
        nonprofitbookkeeping.persistence.sclx.OfficeAssignmentRepository sclxOfficeAssignmentRepository,
        nonprofitbookkeeping.persistence.sclx.CommitteeMembershipRepository sclxCommitteeMembershipRepository,
        nonprofitbookkeeping.persistence.sclx.EventRepository sclxEventRepository,
        nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository,
        nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository sclxOutstandingItemRepository,
        nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository sclxOtherAssetItemRepository,
        nonprofitbookkeeping.persistence.sclx.AssetRepository sclxAssetRepository,
        nonprofitbookkeeping.persistence.sclx.SupplyRepository sclxSupplyRepository)
    {
        this(new Dependencies(
            accountRepository,
            personRepository,
            journalGateway,
            supplementalRepository,
            budgetRecordRepository,
            bankingItemRecordRepository,
            bankStatementRecordRepository,
            documentRepository,
            fundAccountingService,
            sclxOrganizationRepository,
            sclxReportingPeriodRepository,
            sclxCompatibilityRepository,
            sclxBankAccountRepository,
            sclxOfficeAssignmentRepository,
            sclxCommitteeMembershipRepository,
            sclxEventRepository,
            sclxDocumentRepository,
            sclxOutstandingItemRepository,
            sclxOtherAssetItemRepository,
            sclxAssetRepository,
            sclxSupplyRepository));
    }

    @Override
    public void persistRawSource(String rawSourceJson, SclxImportOptions options)
    {
        if (rawSourceJson == null)
        {
            return;
        }

        SclxImportOptions effectiveOptions = options == null ? SclxImportOptions.defaults() : options;
        String runId = effectiveOptions.effectiveImportRunId();
        upsertDocumentContent("sclx.raw." + runId, rawSourceJson);
    }

    @Override
    public void beginImport(SclxDocument document, SclxImportOptions options)
    {
        this.currentOptions = options == null ? SclxImportOptions.defaults() : options;
        this.importedTransactionIdsBySclxId.clear();
        this.importedTransactionIdsByLedgerRow.clear();
        this.personDbIdBySclxPersonId.clear();
        this.personDisplayNameBySclxPersonId.clear();
        this.rawStagingWriteCounts.clear();
        this.importWarnings.clear();
        this.currentImportRunId = this.currentOptions.effectiveImportRunId();
        ensureRunRawPayload(this.currentImportRunId, document);
        upsertDocumentJson("sclx.raw.latest", document);
    }

    @Override
    public void importCompatibility(SclxDocument.Compatibility compatibility)
    {
        try
        {
            nonprofitbookkeeping.model.sclx.Compatibility row = new nonprofitbookkeeping.model.sclx.Compatibility();
            row.setMinimumReaderVersion(compatibility.minimumReaderVersion());
            row.setLossyDowngradeTo(compatibility.lossyDowngradeTo());
            this.sclxCompatibilityRepository.save(runScopedId("compatibility"), row);
            incrementRawStagingCount("compatibility");
        }
        catch (IllegalArgumentException | SQLException ex)
        {
            throw new IllegalStateException("Failed to persist SCLX compatibility metadata", ex);
        }
    }

    @Override
    public void importOrganization(SclxDocument.Organization organization)
    {
        try
        {
            nonprofitbookkeeping.model.sclx.Organization row =
                new nonprofitbookkeeping.model.sclx.Organization();
            row.setOrganizationId(organization.organizationId());
            row.setName(organization.name());
            row.setParentOrganization(organization.parentOrganization());
            row.setBaseCurrency(organization.baseCurrency());
            row.setFiscalYearStart(organization.fiscalYearStart() == null ? null : organization.fiscalYearStart().toString());
            row.setFiscalYearEnd(organization.fiscalYearEnd() == null ? null : organization.fiscalYearEnd().toString());
            row.setExtensions(toExtensions(organization.extensions()));
            this.sclxOrganizationRepository.save(runScopedId("organization"), row);
            incrementRawStagingCount("organization");
        }
        catch (IllegalArgumentException | SQLException ex)
        {
            throw new IllegalStateException("Failed to persist SCLX organization", ex);
        }
    }

    @Override
    public void importReportingPeriod(SclxDocument.ReportingPeriod reportingPeriod)
    {
        try
        {
            nonprofitbookkeeping.model.sclx.ReportingPeriod row =
                new nonprofitbookkeeping.model.sclx.ReportingPeriod();
            row.setStartDate(reportingPeriod.startDate() == null ? null : reportingPeriod.startDate().toString());
            row.setEndDate(reportingPeriod.endDate() == null ? null : reportingPeriod.endDate().toString());
            row.setLabel(reportingPeriod.label());
            row.setFiscalYear(reportingPeriod.fiscalYear());
            row.setPeriodType(parseReportingPeriodType(reportingPeriod.periodType()));
            row.setExtensions(toExtensions(reportingPeriod.extensions()));
            this.sclxReportingPeriodRepository.save(runScopedId("reportingPeriod"), row);
            incrementRawStagingCount("reportingPeriod");
        }
        catch (IllegalArgumentException | SQLException ex)
        {
            throw new IllegalStateException(
                "Failed to persist SCLX reporting period (startDate="
                    + reportingPeriod.startDate()
                    + ", endDate="
                    + reportingPeriod.endDate()
                    + ", periodType="
                    + reportingPeriod.periodType()
                    + ")",
                ex);
        }
    }

    private nonprofitbookkeeping.model.sclx.ReportingPeriod.PeriodType parseReportingPeriodType(String periodType)
    {
        if (periodType == null || periodType.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.ReportingPeriod.PeriodType.fromValue(periodType);
    }

    @Override
    public void importAccounts(List<SclxDocument.Account> accounts)
    {
        for (SclxDocument.Account source : accounts)
        {
            Account account = new Account();
            account.setAccountNumber(resolveAccountNumber(source.accountId(), source.Number()));
            account.setName(firstNonBlank(source.Name(), source.accountId(), source.Number()));
            account.setIncreaseSide(AccountSide.fromString(nullToEmpty(source.IncreaseSide())));
            account.setAccountType(AccountType.fromString(nullToEmpty(source.Type())));
            account.setParentAccountId(source.Parent());
            account.setCurrency("USD");
            account.setOpeningBalance(source.OpeningBalance() == null ? BigDecimal.ZERO : source.OpeningBalance());
            if (source.SupplementalKinds() != null)
            {
                List<SupplementalLineKind> kinds = source.SupplementalKinds().stream()
                    .map(this::parseSupplementalKind)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
                account.setSupplementalLineKinds(kinds);
            }
            try
            {
                this.accountRepository.upsert(account);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist account " + account.getAccountNumber(), ex);
            }
        }
    }

    @Override
    public void importFunds(List<SclxDocument.Fund> funds)
    {
        for (SclxDocument.Fund source : funds)
        {
            if (source == null || source.name() == null || source.name().isBlank())
            {
                continue;
            }
            try
            {
                this.fundAccountingService.addFund(new Fund(source.name()));
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
        try
        {
            this.fundAccountingService.saveFunds(null);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to persist funds", ex);
        }
    }

    @Override
    public void importBudgets(List<SclxDocument.Budget> budgets)
    {
        for (SclxDocument.Budget budget : budgets)
        {
            BudgetRecord row = new BudgetRecord(
                budget.budgetId(),
                budget.name(),
                budget.fiscalYear(),
                budget.fundId(),
                budget.active(),
                budget.description(),
                budget.lines() == null ? List.of() : budget.lines().stream()
                    .map(line -> new BudgetRecord.BudgetLineRecord(
                        line.eventName(),
                        line.budgetedAmount(),
                        line.revenueCategory(),
                        line.expenseCategory(),
                        resolveAccountNumber(line.accountId(), line.accountId()),
                        line.notes(),
                        line.extensions()))
                    .toList(),
                budget.extensions(),
                toJson(budget)
            );
            try
            {
                this.budgetRecordRepository.upsert(row);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist budget " + budget.budgetId(), ex);
            }
        }
    }

    @Override
    public void importPeople(List<SclxDocument.Person> people)
    {
        for (SclxDocument.Person source : people)
        {
            if (source == null)
            {
                continue;
            }
            Person person = resolvePerson(source);
            if (source.personId() != null)
            {
                if (person.getId() > 0)
                {
                    this.personDbIdBySclxPersonId.put(source.personId(), person.getId());
                }
                this.personDisplayNameBySclxPersonId.put(source.personId(), source.displayName());
            }
        }
    }

    @Override
    public void importBankAccounts(List<SclxDocument.BankAccount> bankAccounts)
    {
        for (SclxDocument.BankAccount bankAccount : bankAccounts)
        {
            if (bankAccount == null || bankAccount.bankAccountId() == null || bankAccount.bankAccountId().isBlank())
            {
                continue;
            }
            String scopedKey = scopedId("bankAccount", bankAccount.bankAccountId());
            nonprofitbookkeeping.model.sclx.BankAccount bean = new nonprofitbookkeeping.model.sclx.BankAccount();
            bean.setBankAccountId(bankAccount.bankAccountId());
            bean.setAccountName(bankAccount.accountName());
            bean.setInstitutionName(bankAccount.institutionName());
            bean.setInstitutionEmail(bankAccount.institutionEmail());
            bean.setInstitutionPhone(bankAccount.institutionPhone());
            bean.setAccountNumberMasked(bankAccount.accountNumberMasked());
            bean.setAccountType(bankAccount.accountType());
            bean.setCurrency(bankAccount.currency());
            bean.setInterestBearing(bankAccount.interestBearing());
            bean.setSignatureRequirement(bankAccount.signatureRequirement());
            bean.setAccountHolderName(bankAccount.accountHolderName());
            bean.setChartAccountId(bankAccount.chartAccountId());
            bean.setAuthorizedSigners(bankAccount.authorizedSigners() == null
                ? null
                : bankAccount.authorizedSigners().stream()
                    .map(NonprofitBookkeepingSclxImportTarget::toBankAccountSigner)
                    .toList());
            bean.setExtensions(toExtensions(bankAccount.extensions()));
            try
            {
                this.sclxBankAccountRepository.save(scopedKey, bean);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist SCLX bank account " + scopedKey, ex);
            }
        }
    }

    @Override
    public void importOfficeAssignments(List<SclxDocument.OfficeAssignment> officeAssignments)
    {
        for (SclxDocument.OfficeAssignment officeAssignment : officeAssignments)
        {
            if (officeAssignment == null || officeAssignment.officeAssignmentId() == null || officeAssignment.officeAssignmentId().isBlank())
            {
                continue;
            }
            String scopedKey = scopedId("officeAssignment", officeAssignment.officeAssignmentId());
            nonprofitbookkeeping.model.sclx.OfficeAssignment bean = new nonprofitbookkeeping.model.sclx.OfficeAssignment();
            bean.setOfficeAssignmentId(officeAssignment.officeAssignmentId());
            bean.setPersonId(officeAssignment.personId());
            bean.setOrganizationId(officeAssignment.organizationId());
            bean.setRoleTitle(officeAssignment.roleTitle());
            bean.setMembershipNumber(officeAssignment.membershipNumber());
            bean.setMembershipExpiry(officeAssignment.membershipExpiry());
            bean.setStartDate(officeAssignment.startDate());
            bean.setEndDate(officeAssignment.endDate());
            bean.setActive(officeAssignment.active());
            bean.setExtensions(toExtensions(officeAssignment.extensions()));
            try
            {
                this.sclxOfficeAssignmentRepository.save(scopedKey, bean);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist SCLX office assignment " + scopedKey, ex);
            }
        }
    }

    @Override
    public void importCommitteeMemberships(List<SclxDocument.CommitteeMembership> committeeMemberships)
    {
        for (SclxDocument.CommitteeMembership committeeMembership : committeeMemberships)
        {
            if (committeeMembership == null || committeeMembership.committeeMembershipId() == null || committeeMembership.committeeMembershipId().isBlank())
            {
                continue;
            }
            String scopedKey = scopedId("committeeMembership", committeeMembership.committeeMembershipId());
            nonprofitbookkeeping.model.sclx.CommitteeMembership bean = new nonprofitbookkeeping.model.sclx.CommitteeMembership();
            bean.setCommitteeMembershipId(committeeMembership.committeeMembershipId());
            bean.setCommitteeType(committeeMembership.committeeType());
            bean.setPersonId(committeeMembership.personId());
            bean.setOrganizationId(committeeMembership.organizationId());
            bean.setRoleTitle(committeeMembership.roleTitle());
            bean.setMembershipNumber(committeeMembership.membershipNumber());
            bean.setMembershipExpiry(committeeMembership.membershipExpiry());
            bean.setStartDate(committeeMembership.startDate());
            bean.setEndDate(committeeMembership.endDate());
            bean.setActive(committeeMembership.active());
            bean.setExtensions(toExtensions(committeeMembership.extensions()));
            try
            {
                this.sclxCommitteeMembershipRepository.save(scopedKey, bean);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist SCLX committee membership " + scopedKey, ex);
            }
        }
    }

    @Override
    public void importEvents(List<SclxDocument.Event> events)
    {
        for (int i = 0; i < events.size(); i++)
        {
            SclxDocument.Event event = events.get(i);
            String id = firstNonBlank(event.eventId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.Event row = new nonprofitbookkeeping.model.sclx.Event();
                row.setEventId(event.eventId());
                row.setName(event.name());
                row.setStartDate(asIsoDate(event.startDate()));
                row.setEndDate(asIsoDate(event.endDate()));
                row.setHostingOrganizationId(event.hostingOrganizationId());
                row.setExtensions(toExtensions(event.extensions()));
                this.sclxEventRepository.save(runScopedId(id), row);
                incrementRawStagingCount("events");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX event "
                        + id
                        + " (startDate="
                        + event.startDate()
                        + ", endDate="
                        + event.endDate()
                        + ")",
                    ex);
            }
        }
    }

    @Override
    public void importDocuments(List<SclxDocument.Document> documents)
    {
        for (int i = 0; i < documents.size(); i++)
        {
            SclxDocument.Document document = documents.get(i);
            String id = firstNonBlank(document.documentId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.Document row = new nonprofitbookkeeping.model.sclx.Document();
                row.setDocumentId(document.documentId());
                row.setDocumentType(parseDocumentType(document.documentType()));
                row.setReferenceNumber(document.referenceNumber());
                row.setDocumentDate(asIsoDate(document.documentDate()));
                row.setFileName(document.fileName());
                row.setNotes(document.notes());
                row.setExtensions(toExtensions(document.extensions()));
                this.sclxDocumentRepository.save(runScopedId(id), row);
                incrementRawStagingCount("documents");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX document " + id + " (documentDate=" + document.documentDate() + ")",
                    ex);
            }
        }
    }

    private nonprofitbookkeeping.model.sclx.Document.DocumentType parseDocumentType(String documentType)
    {
        if (documentType == null || documentType.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.Document.DocumentType.fromValue(documentType);
    }

    @Override
    public void importTransactions(List<SclxDocument.Transaction> transactions)
    {
        for (SclxDocument.Transaction source : transactions)
        {
            AccountingTransaction txn = mapTransaction(source);
            AccountingTransaction saved = this.journalGateway.saveTransactionWithEntries(txn);
            this.importedTransactionIdsBySclxId.put(source.transactionId(), saved.getId());
            if (source.workbookLink() != null && source.workbookLink().ledgerRowIndex() != null)
            {
                this.importedTransactionIdsByLedgerRow.put(source.workbookLink().ledgerRowIndex(), saved.getId());
            }
        }
    }

    @Override
    public void importOutstandingItems(List<SclxDocument.OutstandingItem> outstandingItems)
    {
        for (int i = 0; i < outstandingItems.size(); i++)
        {
            SclxDocument.OutstandingItem item = outstandingItems.get(i);
            String id = firstNonBlank(item.outstandingItemId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.OutstandingItem row = new nonprofitbookkeeping.model.sclx.OutstandingItem();
                row.setOutstandingItemId(item.outstandingItemId());
                row.setKind(parseOutstandingItemKind(item.kind()));
                row.setLedgerLink(toLedgerLink(item.ledgerLink()));
                row.setWorkbookLink(toWorkbookLink(item.workbookLink()));
                row.setDateSentOrReceived(asIsoDate(item.dateSentOrReceived()));
                row.setIncomingCheckOrTransferDate(asIsoDate(item.incomingCheckOrTransferDate()));
                row.setTransferIdOrCheckNumber(item.transferIdOrCheckNumber());
                row.setDateShowsOnStatement(asIsoDate(item.dateShowsOnStatement()));
                row.setPersonOrBusinessName(item.personOrBusinessName());
                row.setDetailsNotes(item.detailsNotes());
                row.setFromToCardMerchant(item.fromToCardMerchant());
                row.setAccountForPaymentOrDeposit(item.accountForPaymentOrDeposit());
                row.setAmount(asMoneyAmount(item.amount()));
                row.setDateReversed(asIsoDate(item.dateReversed()));
                row.setReversalReasonAndApproval(item.reversalReasonAndApproval());
                row.setReversalLedgerLink(toLedgerLink(item.reversalLedgerLink()));
                row.setStatus(parseOutstandingItemStatus(item.status()));
                row.setExtensions(toExtensions(item.extensions()));
                this.sclxOutstandingItemRepository.save(runScopedId(id), row);
                incrementRawStagingCount("outstandingItems");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX outstanding item "
                        + id
                        + " (dateSentOrReceived="
                        + item.dateSentOrReceived()
                        + ", incomingCheckOrTransferDate="
                        + item.incomingCheckOrTransferDate()
                        + ", dateShowsOnStatement="
                        + item.dateShowsOnStatement()
                        + ", dateReversed="
                        + item.dateReversed()
                        + ")",
                    ex);
            }
        }
    }

    private static String asIsoDate(LocalDate date)
    {
        return date == null ? null : date.toString();
    }

    private static String asMoneyAmount(BigDecimal amount)
    {
        if (amount == null)
        {
            return null;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static nonprofitbookkeeping.model.sclx.Extensions toExtensions(Map<String, Object> extensions)
    {
        if (extensions == null)
        {
            return null;
        }
        return MAPPER.convertValue(extensions, nonprofitbookkeeping.model.sclx.Extensions.class);
    }

    private static nonprofitbookkeeping.model.sclx.LedgerLink toLedgerLink(SclxDocument.LedgerLink ledgerLink)
    {
        if (ledgerLink == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.LedgerLink model = new nonprofitbookkeeping.model.sclx.LedgerLink();
        model.setTransactionId(ledgerLink.transactionId());
        model.setLineId(ledgerLink.lineId());
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.WorkbookLink toWorkbookLink(SclxDocument.WorkbookLink workbookLink)
    {
        if (workbookLink == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.WorkbookLink model = new nonprofitbookkeeping.model.sclx.WorkbookLink();
        model.setSheetKey(workbookLink.sheetKey());
        model.setLedgerRowIndex(workbookLink.ledgerRowIndex());
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.BankAccountSigner toBankAccountSigner(SclxDocument.BankAccountSigner signer)
    {
        if (signer == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.BankAccountSigner model = new nonprofitbookkeeping.model.sclx.BankAccountSigner();
        model.setPersonId(signer.personId());
        model.setLegalName(signer.legalName());
        model.setMembershipNumber(signer.membershipNumber());
        model.setMembershipExpiry(signer.membershipExpiry());
        model.setRole(signer.role());
        model.setStatus(signer.status());
        model.setExtensions(toExtensions(signer.extensions()));
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.Guardian toGuardian(SclxDocument.Guardian guardian)
    {
        if (guardian == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.Guardian model = new nonprofitbookkeeping.model.sclx.Guardian();
        model.setLegalName(guardian.legalName());
        model.setEmail(guardian.email());
        model.setPhone(guardian.phone());
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset toGuardianshipDetailsAsset(
        SclxDocument.GuardianshipDetailsAsset details)
    {
        if (details == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset model =
            new nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset();
        model.setDateAsOf(asIsoDate(details.dateAsOf()));
        model.setConfirmed(details.confirmed());
        model.setConfirmationStatus(parseConfirmationStatus(details.confirmationStatus()));
        model.setNotes(details.notes());
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.GuardianshipDetailsSupply toGuardianshipDetailsSupply(
        SclxDocument.GuardianshipDetailsSupply details)
    {
        if (details == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.GuardianshipDetailsSupply model =
            new nonprofitbookkeeping.model.sclx.GuardianshipDetailsSupply();
        model.setDateAsOf(asIsoDate(details.dateAsOf()));
        model.setLastConfirmed(asIsoDate(details.lastConfirmed()));
        model.setReturned(details.returned());
        model.setNotes(details.notes());
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.RemovalDetailsAsset toRemovalDetailsAsset(
        SclxDocument.RemovalDetailsAsset details)
    {
        if (details == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.RemovalDetailsAsset model = new nonprofitbookkeeping.model.sclx.RemovalDetailsAsset();
        model.setApprovedBy(details.approvedBy());
        model.setApprovalDate(asIsoDate(details.approvalDate()));
        model.setReason(details.reason());
        model.setNumberRemoved(details.numberRemoved());
        model.setRemoved(details.removed());
        model.setRemovalType(parseRemovalType(details.removalType()));
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.RemovalDetailsSupply toRemovalDetailsSupply(
        SclxDocument.RemovalDetailsSupply details)
    {
        if (details == null)
        {
            return null;
        }
        nonprofitbookkeeping.model.sclx.RemovalDetailsSupply model = new nonprofitbookkeeping.model.sclx.RemovalDetailsSupply();
        model.setApprovedBy(details.approvedBy());
        model.setReason(details.reason());
        model.setNumberRemoved(details.numberRemoved());
        model.setRemoved(details.removed());
        model.setRemovalType(parseRemovalType(details.removalType()));
        return model;
    }

    private static nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset.ConfirmationStatus parseConfirmationStatus(String status)
    {
        if (status == null || status.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset.ConfirmationStatus.fromValue(status);
    }

    private static nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType parseRemovalType(String removalType)
    {
        if (removalType == null || removalType.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType.fromValue(removalType);
    }

    private static nonprofitbookkeeping.model.sclx.OutstandingItem.OutstandingItemKind parseOutstandingItemKind(String kind)
    {
        if (kind == null || kind.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.OutstandingItem.OutstandingItemKind.fromValue(kind);
    }

    private static nonprofitbookkeeping.model.sclx.OutstandingItem.OutstandingItemStatus parseOutstandingItemStatus(String status)
    {
        if (status == null || status.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.OutstandingItem.OutstandingItemStatus.fromValue(status);
    }

    @Override
    public void importOtherAssetItems(List<SclxDocument.OtherAssetItem> otherAssetItems)
    {
        for (int i = 0; i < otherAssetItems.size(); i++)
        {
            SclxDocument.OtherAssetItem item = otherAssetItems.get(i);
            String id = firstNonBlank(item.otherAssetItemId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.OtherAssetItem row = new nonprofitbookkeeping.model.sclx.OtherAssetItem();
                row.setOtherAssetItemId(item.otherAssetItemId());
                row.setLedgerLink(toLedgerLink(item.ledgerLink()));
                row.setWorkbookLink(toWorkbookLink(item.workbookLink()));
                row.setPaidTo(item.paidTo());
                row.setYear(item.year());
                row.setReason(item.reason());
                row.setType(parseOtherAssetType(item.type()));
                row.setTypeCode(parseOtherAssetTypeCode(item.typeCode()));
                row.setEventBudgetLabel(item.eventBudgetLabel());
                row.setAmountAsOfPriorYearEnd(asMoneyAmount(item.amountAsOfPriorYearEnd()));
                row.setPaidReturnedOnLedgerRowIndex(item.paidReturnedOnLedgerRowIndex());
                row.setSettlementLedgerLink(toLedgerLink(item.settlementLedgerLink()));
                row.setStatus(parseOtherAssetStatus(item.status()));
                row.setExtensions(toExtensions(item.extensions()));
                this.sclxOtherAssetItemRepository.save(runScopedId(id), row);
                incrementRawStagingCount("otherAssetItems");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX other-asset item "
                        + id
                        + " (amountAsOfPriorYearEnd="
                        + item.amountAsOfPriorYearEnd()
                        + ", status="
                        + item.status()
                        + ")",
                    ex);
            }
        }
    }

    private static nonprofitbookkeeping.model.sclx.OtherAssetItem.OtherAssetItemType parseOtherAssetType(String type)
    {
        if (type == null || type.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.OtherAssetItem.OtherAssetItemType.fromValue(type);
    }

    private static nonprofitbookkeeping.model.sclx.OtherAssetItem.TypeCode parseOtherAssetTypeCode(String typeCode)
    {
        if (typeCode == null || typeCode.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.OtherAssetItem.TypeCode.fromValue(typeCode);
    }

    private static nonprofitbookkeeping.model.sclx.OtherAssetItem.OtherAssetItemStatus parseOtherAssetStatus(String status)
    {
        if (status == null || status.isBlank())
        {
            return null;
        }
        return nonprofitbookkeeping.model.sclx.OtherAssetItem.OtherAssetItemStatus.fromValue(status);
    }

    @Override
    public void importSupplementalItems(List<SclxDocument.SupplementalItem> supplementalItems)
    {
        for (SclxDocument.SupplementalItem item : supplementalItems)
        {
            Integer txnId = item.ledgerRowIndex() == null ? null : this.importedTransactionIdsByLedgerRow.get(item.ledgerRowIndex());
            if (txnId == null)
            {
                this.importWarnings.add("Skipped supplemental item " + item.supplementalItemId() +
                    " because no imported transaction was found for ledgerRowIndex=" + item.ledgerRowIndex());
                continue;
            }

            TxnSupplementalLineBase bean = createSupplementalBean(item.kind());
            bean.setTxnId(txnId.longValue());
            bean.setCounterpartyPersonId(resolveCounterpartyId(item.personId(), item.counterpartyName()));
            bean.setDescription(item.reason());
            bean.setReference(firstNonBlank(item.eventBudgetLabel(), item.sourceLabel(), item.subtypeCode()));
            bean.setAmount(item.amountAsOf());
            bean.setNotes(buildSupplementalNotes(item));

            TxnSupplementalLineRecord record = TxnSupplementalLineMapper.toRecord(bean);

            try (Connection c = Database.get().getConnection())
            {
                List<TxnSupplementalLineRecord> all = this.supplementalRepository.listByTxnId(txnId.longValue());
                all.add(record);
                c.setAutoCommit(false);
                this.supplementalRepository.replaceForTxn(c, txnId.longValue(), all);
                c.commit();
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist supplemental item " + item.supplementalItemId(), ex);
            }
        }
    }

    @Override
    public void importAssets(List<SclxDocument.Asset> assets)
    {
        for (int i = 0; i < assets.size(); i++)
        {
            SclxDocument.Asset item = assets.get(i);
            String id = firstNonBlank(item.assetId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.Asset row = new nonprofitbookkeeping.model.sclx.Asset();
                row.setAssetId(item.assetId());
                row.setDateAcquired(asIsoDate(item.dateAcquired()));
                row.setDescription(item.description());
                row.setItemCount(item.itemCount());
                row.setApproxValueTotal(asMoneyAmount(item.approxValueTotal()));
                row.setValuePerItem(asMoneyAmount(item.valuePerItem()));
                row.setItemType(item.itemType());
                row.setUsedFor(item.usedFor());
                row.setLotPaidTotal(asMoneyAmount(item.lotPaidTotal()));
                row.setLotItemCount(item.lotItemCount());
                row.setCurrentGuardian(toGuardian(item.currentGuardian()));
                row.setGuardianshipDetails(toGuardianshipDetailsAsset(item.guardianshipDetails()));
                row.setRemovalDetails(toRemovalDetailsAsset(item.removalDetails()));
                row.setExtensions(toExtensions(item.extensions()));
                this.sclxAssetRepository.save(runScopedId(id), row);
                incrementRawStagingCount("assets");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX asset " + id + " (dateAcquired=" + item.dateAcquired() + ")",
                    ex);
            }
        }
    }

    @Override
    public void importSupplies(List<SclxDocument.Supply> supplies)
    {
        for (int i = 0; i < supplies.size(); i++)
        {
            SclxDocument.Supply item = supplies.get(i);
            String id = firstNonBlank(item.supplyId(), "index-" + i);
            try
            {
                nonprofitbookkeeping.model.sclx.Supply row = new nonprofitbookkeeping.model.sclx.Supply();
                row.setSupplyId(item.supplyId());
                row.setItemNumber(item.itemNumber());
                row.setDateAcquired(asIsoDate(item.dateAcquired()));
                row.setDescription(item.description());
                row.setCount(item.count());
                row.setApproxValueTotal(asMoneyAmount(item.approxValueTotal()));
                row.setValuePerItem(asMoneyAmount(item.valuePerItem()));
                row.setGuardian(toGuardian(item.guardian()));
                row.setGuardianshipDetails(toGuardianshipDetailsSupply(item.guardianshipDetails()));
                row.setRemovalDetails(toRemovalDetailsSupply(item.removalDetails()));
                row.setAdditionalNotes(item.additionalNotes());
                row.setExtensions(toExtensions(item.extensions()));
                this.sclxSupplyRepository.save(runScopedId(id), row);
                incrementRawStagingCount("supplies");
            }
            catch (IllegalArgumentException | SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to persist SCLX supply " + id + " (dateAcquired=" + item.dateAcquired() + ")",
                    ex);
            }
        }
    }

    @Override
    public void importBankingItems(List<SclxDocument.BankingItem> bankingItems)
    {
        for (SclxDocument.BankingItem item : bankingItems)
        {
            BankingItemRecord row = new BankingItemRecord(
                item.bankingItemId(),
                item.kind(),
                item.bankAccountId(),
                item.transactionId(),
                List.of(),
                item.ofx() != null && item.ofx().datePosted() != null ? item.ofx().datePosted() : item.depositDate(),
                item.amount(),
                item.checkNumber(),
                item.payee(),
                item.depositDate(),
                item.payer(),
                null,
                item.ofx() == null ? null : item.ofx().memo(),
                item.source(),
                item.status(),
                item.importId(),
                item.ofx() == null ? null : new BankingItemRecord.OfxTransactionRecord(
                    item.ofx().fitId(),
                    item.ofx().transactionType(),
                    item.ofx().datePosted(),
                    item.ofx().dateUser(),
                    item.ofx().dateAvailable(),
                    item.ofx().checkNumber(),
                    item.ofx().referenceNumber(),
                    item.ofx().name(),
                    item.ofx().memo(),
                    item.ofx().payeeId(),
                    item.ofx().sic(),
                    item.ofx().serverTransactionId(),
                    item.ofx().correctFitId(),
                    item.ofx().correctAction(),
                    item.ofx().extensions()
                ),
                item.extensions(),
                toJson(item)
            );
            try
            {
                this.bankingItemRecordRepository.upsert(row);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist banking item " + item.bankingItemId(), ex);
            }
        }
    }

    @Override
    public void importBankStatementImports(List<SclxDocument.BankStatementImport> bankStatementImports)
    {
        for (SclxDocument.BankStatementImport item : bankStatementImports)
        {
            BankStatementRecord row = new BankStatementRecord(
                item.importId(),
                parseSourceFormat(item.sourceFormat()),
                item.sourceVersion(),
                parseStatementKind(item.statementKind()),
                item.bankAccount() == null ? null : new BankStatementRecord.BankAccountRef(
                    item.bankAccount().bankId(),
                    item.bankAccount().accountId(),
                    item.bankAccount().accountType()
                ),
                item.currency(),
                item.statementStart(),
                item.statementEnd(),
                item.ledgerBalance() == null ? null : new BankStatementRecord.BalanceSnapshot(item.ledgerBalance().amount(), item.ledgerBalance().asOf()),
                item.availableBalance() == null ? null : new BankStatementRecord.BalanceSnapshot(item.availableBalance().amount(), item.availableBalance().asOf()),
                item.documentId(),
                item.extensions(),
                toJson(item)
            );
            try
            {
                this.bankStatementRecordRepository.upsert(row);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Failed to persist bank statement import " + item.importId(), ex);
            }
        }
    }

    @Override
    public void completeImport(SclxImportResult result)
    {
        Map<String, Object> runSummary = new LinkedHashMap<>();
        runSummary.put("importRunId", this.currentImportRunId);
        runSummary.put("version", result.version());
        runSummary.put("rawStagingWriteCounts", new LinkedHashMap<>(this.rawStagingWriteCounts));
        runSummary.put("canonicalWriteCounts", buildCanonicalWriteCounts(result));
        runSummary.put("warnings", List.copyOf(this.importWarnings));
        upsertDocumentJson("sclx.importSummary." + this.currentImportRunId, runSummary);
        upsertDocumentJson("sclx.importSummary", result);
    }

    private Map<String, Integer> buildCanonicalWriteCounts(SclxImportResult result)
    {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("accounts", result.accountCount());
        counts.put("funds", result.fundCount());
        counts.put("budgets", result.budgetCount());
        counts.put("people", result.personCount());
        counts.put("transactions", result.transactionCount());
        counts.put("transactionLines", result.transactionLineCount());
        counts.put("supplementalItems", result.supplementalItemCount());
        counts.put("bankingItems", result.bankingItemCount());
        counts.put("bankStatementImports", result.bankStatementImportCount());
        return counts;
    }

    private void incrementRawStagingCount(String key)
    {
        this.rawStagingWriteCounts.merge(key, 1, Integer::sum);
    }

    private String runScopedId(String baseId)
    {
        return this.currentImportRunId + "::" + firstNonBlank(baseId, "default");
    }

    
private AccountingTransaction mapTransaction(SclxDocument.Transaction source)
{
    LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
    for (SclxDocument.TransactionLine line : source.lines())
    {
        BigDecimal amount = debitAmount(line.debit(), line.credit());
        AccountSide side = line.debit() != null && line.debit().compareTo(BigDecimal.ZERO) > 0 ? AccountSide.DEBIT : AccountSide.CREDIT;
        AccountingEntry entry = new AccountingEntry(
            amount,
            resolveAccountNumber(line.accountId(), line.accountId()),
            side,
            line.accountId()
        );
        entry.setFundNumber(line.fundId());
        entries.add(entry);

    }

    BigDecimal debitTotal = entries.stream()
        .filter(e -> e.getAccountSide() == AccountSide.DEBIT)
        .map(AccountingEntry::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal creditTotal = entries.stream()
        .filter(e -> e.getAccountSide() == AccountSide.CREDIT)
        .map(AccountingEntry::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal delta = debitTotal.subtract(creditTotal);

    if (delta.compareTo(BigDecimal.ZERO) != 0)
    {
        if (!this.currentOptions.allowSingleSidedTransactions())
        {
            throw new IllegalStateException("Unbalanced SCLX transaction " + source.transactionId());
        }
        if (!this.currentOptions.hasCashAccountReference())
        {
            throw new IllegalStateException("cashAccountReference is required to import single-sided or unbalanced SCLX transactions.");
        }

        if (delta.compareTo(BigDecimal.ZERO) > 0)
        {
            entries.add(new AccountingEntry(delta.abs(), this.currentOptions.cashAccountReference(), AccountSide.CREDIT, "Cash"));
        }
        else
        {
            entries.add(new AccountingEntry(delta.abs(), this.currentOptions.cashAccountReference(), AccountSide.DEBIT, "Cash"));
        }
    }

    AccountingTransaction txn = new AccountingTransaction();
    txn.setEntries(entries);
    txn.setDate(source.transactionDate() == null ? (source.postingDate() == null ? "" : source.postingDate().toString()) : source.transactionDate().toString());
    txn.setMemo(source.description());
    txn.setToFrom(firstNonBlank(source.personOrBusinessName(), workbookPersonOrBusinessName(source), source.personDisplayName()));
    txn.setCheckNumber(firstNonBlank(source.reference(), source.checkNumber(), source.checkNumberId()));
    txn.setClearBank(source.bankTiming());
    txn.setBank(source.bankTiming());
    txn.setBudgetTracking(source.budgetId());
    txn.setAssociatedFundName(firstLineFund(source));
    txn.setBookingDateTimestamp(source.postingDate() == null ? System.currentTimeMillis() : source.postingDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());

    Map<String, String> info = new LinkedHashMap<>();
    info.put("sclx.transactionId", nullToEmpty(source.transactionId()));
    info.put("sclx.source", nullToEmpty(source.source()));
    info.put("sclx.status", nullToEmpty(source.status()));
    info.put("sclx.checkNumberId", nullToEmpty(source.checkNumberId()));
    info.put("sclx.personId", nullToEmpty(source.personId()));
    info.put("sclx.personDisplayName", nullToEmpty(source.personDisplayName()));
    if (source.workbookLink() != null)
    {
        info.put("sclx.sheetKey", nullToEmpty(source.workbookLink().sheetKey()));
        if (source.workbookLink().ledgerRowIndex() != null)
        {
            info.put("sclx.ledgerRowIndex", String.valueOf(source.workbookLink().ledgerRowIndex()));
        }
    }
    txn.setInfo(info);
    txn.setSupplementalLines(new ArrayList<>());
    return txn;
}

private Person resolvePerson(SclxDocument.Person source)
    {
        try
        {
            for (Person existing : this.personRepository.list())
            {
                if (equalsIgnoreCase(existing.getName(), source.displayName()) ||
                    (source.email() != null && equalsIgnoreCase(existing.getEmail(), source.email())))
                {
                    if (source.email() != null && (existing.getEmail() == null || existing.getEmail().isBlank()))
                    {
                        existing.setEmail(source.email());
                    }
                    if (source.phone() != null && (existing.getPhone() == null || existing.getPhone().isBlank()))
                    {
                        existing.setPhone(source.phone());
                    }
                    return this.personRepository.save(existing);
                }
            }

            Person person = new Person();
            person.setName(source.displayName());
            person.setEmail(source.email());
            person.setPhone(source.phone());
            return this.personRepository.save(person);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to resolve person " + source.displayName(), ex);
        }
    }

    private Long resolveCounterpartyId(String sclxPersonId, String counterpartyName)
    {
        if (sclxPersonId != null && this.personDbIdBySclxPersonId.containsKey(sclxPersonId))
        {
            return this.personDbIdBySclxPersonId.get(sclxPersonId);
        }
        if (counterpartyName == null || counterpartyName.isBlank())
        {
            return null;
        }
        try
        {
            for (Person existing : this.personRepository.list())
            {
                if (equalsIgnoreCase(existing.getName(), counterpartyName))
                {
                    return existing.getId();
                }
            }
            Person person = new Person();
            person.setName(counterpartyName);
            return this.personRepository.save(person).getId();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to resolve counterparty " + counterpartyName, ex);
        }
    }

    private static TxnSupplementalLineBase createSupplementalBean(String kind)
    {
        return switch (kind)
        {
            case "RECEIVABLE" -> new ReceivablesLine();
            case "PREPAID_EXPENSE" -> new PrepaidExpenseLine();
            case "OTHER_ASSET" -> new OtherAssetsLine();
            case "DEFERRED_REVENUE" -> new DeferredRevenueLine();
            case "PAYABLE" -> new PayablesLine();
            case "OTHER_LIABILITY" -> new OtherLiabilitiesLine();
            default -> throw new IllegalArgumentException("Unsupported supplemental kind: " + kind);
        };
    }

    private static String buildSupplementalNotes(SclxDocument.SupplementalItem item)
    {
        List<String> parts = new ArrayList<>();
        if (item.subtypeCode() != null) parts.add("subtype=" + item.subtypeCode());
        if (item.eventBudgetLabel() != null) parts.add("budget=" + item.eventBudgetLabel());
        if (item.sourceLabel() != null) parts.add("source=" + item.sourceLabel());
        return String.join("; ", parts);
    }

    private SupplementalLineKind parseSupplementalKind(String value)
    {
        if (value == null || value.isBlank()) return null;
        try { return SupplementalLineKind.valueOf(value); }
        catch (IllegalArgumentException ex) { return null; }
    }

    private String resolveAccountNumber(String preferred, String fallback)
    {
        return this.currentOptions.resolveAccountReference(firstNonBlank(preferred, fallback));
    }


    private static String workbookPersonOrBusinessName(SclxDocument.Transaction source)
    {
        if (source.extensions() == null) return null;
        Object workbook = source.extensions().get("workbook");
        if (!(workbook instanceof Map<?, ?> workbookMap)) return null;
        Object value = workbookMap.get("personOrBusinessName");
        return value == null ? null : String.valueOf(value);
    }

    private static BigDecimal debitAmount(BigDecimal debit, BigDecimal credit)
    {
        if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) return debit;
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) return credit;
        return BigDecimal.ZERO;
    }

    private static String firstLineFund(SclxDocument.Transaction source)
    {
        if (source.lines() == null) return "";
        for (SclxDocument.TransactionLine line : source.lines())
        {
            if (line.fundId() != null && !line.fundId().isBlank())
            {
                return line.fundId();
            }
        }
        return "";
    }

    private static String firstNonBlank(String... values)
    {
        if (values == null) return null;
        for (String value : values)
        {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static boolean equalsIgnoreCase(String a, String b)

    {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private static String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }

    private String scopedId(String section, String entryId)
    {
        return this.currentImportRunId + "::" + section + "::" + entryId;
    }

    private void upsertDocumentJson(String name, Object value)
    {
        upsertDocumentContent(name, toJson(value));
    }

    private void upsertDocumentContent(String name, String content)
    {
        try
        {
            this.documentRepository.upsert(name, content);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to store document " + name, ex);
        }
    }

    private void ensureRunRawPayload(String runId, SclxDocument document)
    {
        String key = "sclx.raw." + runId;
        try
        {
            if (this.documentRepository.find(key).isEmpty())
            {
                upsertDocumentJson(key, document);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to inspect existing raw SCLX payload for run " + runId, ex);
        }
    }

    private static String toJson(Object value)
    {
        try
        {
            return MAPPER.writeValueAsString(value);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalStateException("Failed to serialize value to JSON", ex);
        }
    }

    /**
     * Parses the source format.
     *
     * @param value the value
     * @return the bank statement record. source format
     */
    private static BankStatementRecord.SourceFormat parseSourceFormat(String value)
    {
        if (value == null || value.isBlank()) return null;
        try { return BankStatementRecord.SourceFormat.valueOf(value); }
        catch (IllegalArgumentException ex) { return BankStatementRecord.SourceFormat.OTHER; }
    }

    /**
     * Parses the statement kind.
     *
     * @param value the value
     * @return the bank statement record. statement kind
     */
    private static BankStatementRecord.StatementKind parseStatementKind(String value)
    {
        if (value == null || value.isBlank()) return null;
        try { return BankStatementRecord.StatementKind.valueOf(value); }
        catch (IllegalArgumentException ex) { return BankStatementRecord.StatementKind.OTHER; }
    }

    private record ReferenceLookup(String toFrom, String checkNumber)
    {
        private static ReferenceLookup empty()
        {
            return new ReferenceLookup(null, null);
        }

        private boolean isEmpty()
        {
            return this.toFrom == null && this.checkNumber == null;
        }
    }
}
