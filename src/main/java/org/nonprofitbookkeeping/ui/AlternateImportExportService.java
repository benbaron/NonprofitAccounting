package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.importer.sclx.NonprofitBookkeepingSclxImportTarget;
import nonprofitbookkeeping.importer.sclx.SclxImportOptions;
import nonprofitbookkeeping.importer.sclx.SclxImportResult;
import nonprofitbookkeeping.importer.sclx.SclxImportService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Thin boundary service for the native alternate Import/Export workspace. */
class AlternateImportExportService
{
    private final SclxImportService sclxImportService;

    AlternateImportExportService()
    {
        this(new SclxImportService());
    }

    AlternateImportExportService(SclxImportService sclxImportService)
    {
        this.sclxImportService = Objects.requireNonNull(sclxImportService, "sclxImportService");
    }

    ImportExportOperationResult summarizeSclxResult(SclxImportResult result)
    {
        Objects.requireNonNull(result, "result");
        int created = result.accountCount() + result.fundCount() + result.budgetCount()
            + result.personCount() + result.eventCount() + result.documentCount()
            + result.transactionCount() + result.outstandingItemCount() + result.otherAssetItemCount()
            + result.supplementalItemCount() + result.assetCount() + result.supplyCount()
            + result.bankingItemCount() + result.bankStatementImportCount();
        return new ImportExportOperationResult(created, 0, 0, List.of(), List.of());
    }

    ImportExportOperationResult importSclx(Path path, SclxImportOptions options)
    {
        SclxImportResult result = this.sclxImportService.importFile(path, new NonprofitBookkeepingSclxImportTarget(), options);
        return summarizeSclxResult(result);
    }

    ImportExportOperationResult validateChartOfAccountsImport(Path path)
    {
        if (path == null)
        {
            return blockingError("Select a chart-of-accounts file before importing.");
        }
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase();
        if (!(name.endsWith(".xlsx") || name.endsWith(".json") || name.endsWith(".csv")))
        {
            return blockingError("Chart of accounts import supports .xlsx, .json, and .csv files.");
        }
        if (!Files.exists(path))
        {
            return blockingError("Chart of accounts import file does not exist: " + path);
        }
        return new ImportExportOperationResult(0, 0, 0, List.of(), List.of());
    }

    ImportExportOperationResult blockingError(String message)
    {
        return new ImportExportOperationResult(0, 0, 0, List.of(), List.of(message));
    }
}
