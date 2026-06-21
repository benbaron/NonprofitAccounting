package nonprofitbookkeeping.importer.sclx;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nonprofitbookkeeping.persistence.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level SCLX import coordinator.
 *
 * <p>This service performs lightweight structural checks and then streams the
 * staged collections into an {@link SclxImportTarget}. It does not impose a
 * storage model; that choice belongs to NonprofitBookkeeping.</p>
 */
public class SclxImportService
{
    private static final Logger log = LoggerFactory.getLogger(SclxImportService.class);
    private final SclxParser parser;

    public SclxImportService()
    {
        this(new SclxParser());
    }

    public SclxImportService(SclxParser parser)
    {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public SclxImportResult importFile(Path path, SclxImportTarget target)
    {
        return importFile(path, target, SclxImportOptions.defaults());
    }

    public SclxImportResult importFile(Path path, SclxImportTarget target, SclxImportOptions options)
    {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(options, "options");

        log.debug("Starting SCLX import for path={}, runId={}", path, options.effectiveImportRunId());
        String rawSource = readRawSource(path);
        log.debug("Read SCLX source bytes={}, path={}", rawSource.length(), path);
        SclxDocument document;
        try
        {
            document = parser.parse(rawSource);
        }
        catch (SclxImportException ex)
        {
            log.error("SCLX parse failed for path={}, runId={}: {}", path, options.effectiveImportRunId(), ex.getMessage());
            throw new SclxImportException("Failed to parse SCLX JSON from file: " + path, ex);
        }

        // The source file is an interchange artifact, not application data.
        // Import the typed records only; do not persist the complete raw payload.
        return importDocument(document, target, options);
    }

    private static String readRawSource(Path path)
    {
        try
        {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (IOException ex)
        {
            throw new SclxImportException("Failed to read SCLX source: " + path, ex);
        }
    }

    public static SclxImportResult importDocument(SclxDocument document, SclxImportTarget target, SclxImportOptions options)
    {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(options, "options");

        log.debug(
            "Importing SCLX document format={}, version={}, orgPresent={}, reportingPeriodPresent={}",
            document.format(),
            document.version(),
            document.organization() != null,
            document.reportingPeriod() != null);
        validateEnvelope(document, options);

        List<SclxDocument.Transaction> postingTransactions =
            preparePostingTransactions(normalize(document.transactions(), options), options);
        validateLedgerNativePolicy(postingTransactions, options);

        target.beginImport(document, options);

        if (document.compatibility() != null)
        {
            target.importCompatibility(document.compatibility());
        }
        if (document.organization() != null)
        {
            log.debug(
                "Importing SCLX document format={}, version={}, orgPresent={}, reportingPeriodPresent={}",
                document.format(),
                document.version(),
                document.organization() != null,
                document.reportingPeriod() != null);
            validateEnvelope(document, options);
            validateLedgerNativePolicy(document, options);

            target.beginImport(document, options);

            if (document.compatibility() != null)
            {
                target.importCompatibility(document.compatibility());
            }
            if (document.organization() != null)
            {
                target.importOrganization(document.organization());
            }
            if (document.reportingPeriod() != null)
            {
                target.importReportingPeriod(document.reportingPeriod());
            }

            target.importAccounts(normalize(document.chartOfAccounts(), options));
            target.importFunds(normalize(document.funds(), options));
            target.importBudgets(normalize(document.budgets(), options));
            target.importPeople(normalize(document.people(), options));
            target.importBankAccounts(normalize(document.bankAccounts(), options));
            target.importOfficeAssignments(normalize(document.officeAssignments(), options));
            target.importCommitteeMemberships(normalize(document.committeeMemberships(), options));
            target.importEvents(normalize(document.events(), options));
            target.importDocuments(normalize(document.documents(), options));
            target.importTransactions(normalize(document.transactions(), options));
            target.importOutstandingItems(normalize(document.outstandingItems(), options));
            target.importOtherAssetItems(normalize(document.otherAssetItems(), options));
            target.importSupplementalItems(normalize(document.supplementalItems(), options));
            target.importAssets(normalize(document.assets(), options));
            target.importSupplies(normalize(document.supplies(), options));
            target.importBankingItems(normalize(document.bankingItems(), options));
            target.importBankStatementImports(normalize(document.bankStatementImports(), options));

            SclxImportResult result = new SclxImportResult(
                document.version(),
                size(document.chartOfAccounts()),
                size(document.funds()),
                size(document.budgets()),
                size(document.people()),
                size(document.events()),
                size(document.documents()),
                size(document.transactions()),
                countLines(document.transactions()),
                size(document.outstandingItems()),
                size(document.otherAssetItems()),
                size(document.supplementalItems()),
                size(document.assets()),
                size(document.supplies()),
                size(document.bankingItems()),
                size(document.bankStatementImports()));
            log.debug(
                "SCLX import staged counts accounts={}, funds={}, budgets={}, people={}, events={}, documents={}, txns={}, txnLines={}",
                result.accountCount(),
                result.fundCount(),
                result.budgetCount(),
                result.personCount(),
                result.eventCount(),
                result.documentCount(),
                result.transactionCount(),
                result.transactionLineCount());

            target.completeImport(result);
            log.debug("Completed SCLX import for version={}", result.version());
            return result;
        }
        finally
        {
            DocumentRepository.clearThreadScopedEphemeralDocuments();
        }

        target.importAccounts(normalize(document.chartOfAccounts(), options));
        target.importFunds(normalize(document.funds(), options));
        target.importBudgets(normalize(document.budgets(), options));
        target.importPeople(normalize(document.people(), options));
        target.importBankAccounts(normalize(document.bankAccounts(), options));
        target.importOfficeAssignments(normalize(document.officeAssignments(), options));
        target.importCommitteeMemberships(normalize(document.committeeMemberships(), options));
        target.importEvents(normalize(document.events(), options));
        target.importDocuments(normalize(document.documents(), options));
        target.importTransactions(postingTransactions);
        target.importOutstandingItems(normalize(document.outstandingItems(), options));
        target.importOtherAssetItems(normalize(document.otherAssetItems(), options));
        target.importSupplementalItems(normalize(document.supplementalItems(), options));
        target.importAssets(normalize(document.assets(), options));
        target.importSupplies(normalize(document.supplies(), options));
        target.importBankingItems(normalize(document.bankingItems(), options));
        target.importBankStatementImports(normalize(document.bankStatementImports(), options));

        SclxImportResult result = new SclxImportResult(
            document.version(),
            size(document.chartOfAccounts()),
            size(document.funds()),
            size(document.budgets()),
            size(document.people()),
            size(document.events()),
            size(document.documents()),
            postingTransactions.size(),
            countLines(postingTransactions),
            size(document.outstandingItems()),
            size(document.otherAssetItems()),
            size(document.supplementalItems()),
            size(document.assets()),
            size(document.supplies()),
            size(document.bankingItems()),
            size(document.bankStatementImports()));
        log.debug(
            "SCLX import staged counts accounts={}, funds={}, budgets={}, people={}, events={}, documents={}, txns={}, txnLines={}",
            result.accountCount(),
            result.fundCount(),
            result.budgetCount(),
            result.personCount(),
            result.eventCount(),
            result.documentCount(),
            result.transactionCount(),
            result.transactionLineCount());

        target.completeImport(result);
        log.debug("Completed SCLX import for version={}", result.version());
        return result;
    }

    private static List<SclxDocument.Transaction> preparePostingTransactions(
        List<SclxDocument.Transaction> transactions,
        SclxImportOptions options)
    {
        if (transactions == null || transactions.isEmpty())
        {
            return List.of();
        }

        List<SclxDocument.Transaction> prepared = new ArrayList<>();
        for (SclxDocument.Transaction transaction : transactions)
        {
            if (transaction == null)
            {
                continue;
            }

            List<SclxDocument.TransactionLine> sourceLines =
                normalize(transaction.lines(), options);
            List<SclxDocument.TransactionLine> postingLines = new ArrayList<>();

            for (SclxDocument.TransactionLine line : sourceLines)
            {
                if (line == null)
                {
                    continue;
                }
                if (isZeroValueLine(line))
                {
                    log.warn(
                        "Skipping zero-value SCLX transaction line transactionId={} lineId={} ledgerRow={}",
                        transaction.transactionId(),
                        line.lineId(),
                        workbookRow(transaction));
                    continue;
                }
                postingLines.add(line);
            }

            if (postingLines.isEmpty())
            {
                log.warn(
                    "Skipping non-posting SCLX transaction transactionId={} ledgerRow={} description={}",
                    transaction.transactionId(),
                    workbookRow(transaction),
                    transaction.description());
                continue;
            }

            if (postingLines.size() == sourceLines.size())
            {
                prepared.add(transaction);
            }
            else
            {
                prepared.add(copyWithLines(transaction, List.copyOf(postingLines)));
            }
        }
        return List.copyOf(prepared);
    }

    private static boolean isZeroValueLine(SclxDocument.TransactionLine line)
    {
        BigDecimal debit = line.debit() == null ? BigDecimal.ZERO : line.debit();
        BigDecimal credit = line.credit() == null ? BigDecimal.ZERO : line.credit();
        return debit.signum() == 0 && credit.signum() == 0;
    }

    private static Integer workbookRow(SclxDocument.Transaction transaction)
    {
        return transaction.workbookLink() == null
            ? null
            : transaction.workbookLink().ledgerRowIndex();
    }

    private static SclxDocument.Transaction copyWithLines(
        SclxDocument.Transaction source,
        List<SclxDocument.TransactionLine> lines)
    {
        return new SclxDocument.Transaction(
            source.transactionId(),
            source.transactionDate(),
            source.postingDate(),
            source.description(),
            source.reference(),
            source.checkNumber(),
            source.checkNumberId(),
            source.personId(),
            source.personDisplayName(),
            source.personOrBusinessName(),
            source.status(),
            source.source(),
            source.bankTiming(),
            source.budgetTiming(),
            source.budgetId(),
            source.workbookLink(),
            source.approval(),
            source.documentIds(),
            source.eventId(),
            lines,
            source.extensions());
    }

    private static void validateEnvelope(SclxDocument document, SclxImportOptions options)
    {
        if (options.requireStrictFormat() && !"SCLX".equals(document.format()))
        {
            throw new SclxImportException("Unsupported format: " + document.format());
        }

        String version = document.version();
        if (version == null || version.isBlank())
        {
            throw new SclxImportException("Missing SCLX version.");
        }

        if (options.failOnUnknownVersion())
        {
            boolean supported = "1.0".equals(version) || "1.2".equals(version) || "1.3".equals(version);
            if (!supported)
            {
                throw new SclxImportException("Unsupported SCLX version: " + version);
            }
        }
    }

    private static void validateLedgerNativePolicy(
        List<SclxDocument.Transaction> transactions,
        SclxImportOptions options)
    {
        if (!options.allowSingleSidedTransactions())
        {
            for (SclxDocument.Transaction transaction : transactions)
            {
                int lineCount = size(transaction.lines());
                if (lineCount < 2)
                {
                    throw new SclxImportException(
                        "Single-sided transaction not allowed by import options: " + transaction.transactionId());
                }
            }
        }
    }

    private static <T> List<T> normalize(List<T> value, SclxImportOptions options)
    {
        if (value == null)
        {
            return List.of();
        }
        if (options.trimEmptyCollections() && value.isEmpty())
        {
            return List.of();
        }
        return value;
    }

    private static int size(List<?> value)
    {
        return value == null ? 0 : value.size();
    }

    private static int countLines(List<SclxDocument.Transaction> transactions)
    {
        if (transactions == null || transactions.isEmpty())
        {
            return 0;
        }

        int total = 0;
        for (SclxDocument.Transaction transaction : transactions)
        {
            total += size(transaction.lines());
        }
        return total;
    }
}
