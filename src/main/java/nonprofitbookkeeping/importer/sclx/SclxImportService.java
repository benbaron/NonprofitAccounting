package nonprofitbookkeeping.importer.sclx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level SCLX import coordinator.
 *
 * <p>This service performs lightweight structural checks and then streams the
 * staged collections into an {@link SclxImportTarget}. It does not impose a
 * storage model; that choice belongs to NonprofitBookkeeping.
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
        log.debug("Read raw SCLX source bytes={}, path={}", rawSource.length(), path);
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
        target.persistRawSource(rawSource, options);
        log.debug("Persisted raw SCLX source for runId={}", options.effectiveImportRunId());
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
            throw new SclxImportException("Failed to read raw SCLX source: " + path, ex);
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

    private static void validateLedgerNativePolicy(SclxDocument document, SclxImportOptions options)
    {
        if (!options.allowSingleSidedTransactions())
        {
            for (SclxDocument.Transaction transaction : normalize(document.transactions(), options))
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
