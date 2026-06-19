package org.nonprofitbookkeeping.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.ImpexRecordService;
import nonprofitbookkeeping.service.ReportService;

/**
 * Service-backed global search for the alternate shell.
 *
 * <p>Every domain is best-effort: unavailable services or unopened database/company
 * state produce no hits for that domain instead of failing the whole search.</p>
 */
public class GlobalSearchService
{
    private final UiServiceProvider services;

    public GlobalSearchService(UiServiceProvider services)
    {
        this.services = services;
    }

    public List<GlobalSearchResult> search(String rawQuery)
    {
        if (rawQuery == null || rawQuery.isBlank())
        {
            return List.of();
        }
        String query = rawQuery.trim();
        String normalized = normalize(query);
        List<GlobalSearchResult> results = new ArrayList<>();
        searchAccounts(normalized, results);
        searchTransactions(normalized, results);
        searchFunds(normalized, results);
        searchDonors(normalized, results);
        searchReports(normalized, results);
        searchCompanies(normalized, results);
        searchImportExportHistory(normalized, results);
        return List.copyOf(results);
    }

    private void searchAccounts(String query, List<GlobalSearchResult> results)
    {
        try
        {
            services.accountLookup().listActivePostingAccounts().stream()
                .filter(account -> matches(query, account.getCode(), account.getName(), account.getDescription()))
                .map(account -> new GlobalSearchResult(SearchResultType.ACCOUNT,
                    compact(account.getCode(), account.getName()),
                    "Posting account" + suffix(account.getAccountType()), AppPanelId.CHART_OF_ACCOUNTS,
                    "Account drilldown is not implemented yet; opening Chart of Accounts."))
                .forEach(results::add);
        }
        catch (RuntimeException ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private void searchTransactions(String query, List<GlobalSearchResult> results)
    {
        try
        {
            if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null || CurrentCompany.getCompany().getLedger() == null)
            {
                return;
            }
            CurrentCompany.getCompany().getLedger().getJournal().getJournalTransactions().stream()
                .filter(txn -> matches(query, String.valueOf(txn.getId()), txn.getDate(), txn.getMemo(), txn.getToFrom(),
                    txn.getCheckNumber(), txn.getAssociatedFundName()))
                .map(txn -> transactionResult(txn))
                .forEach(results::add);
        }
        catch (RuntimeException ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private GlobalSearchResult transactionResult(AccountingTransaction txn)
    {
        String title = "Transaction #" + txn.getId();
        if (txn.getMemo() != null && !txn.getMemo().isBlank())
        {
            title += " — " + txn.getMemo();
        }
        return new GlobalSearchResult(SearchResultType.TRANSACTION, title,
            compact(txn.getDate(), txn.getToFrom(), txn.getTotalAmount().toPlainString()), AppPanelId.LEDGER_REGISTER,
            "Transaction drilldown is not implemented yet; opening Journal.");
    }

    private void searchFunds(String query, List<GlobalSearchResult> results)
    {
        try
        {
            services.fundLookup().listActiveFunds().stream()
                .filter(fund -> matches(query, fund.getCode(), fund.getName(), fund.getRestrictionText()))
                .map(fund -> new GlobalSearchResult(SearchResultType.FUND, compact(fund.getCode(), fund.getName()),
                    "Active fund" + suffix(fund.getFundType()), AppPanelId.FUNDS,
                    "Fund drilldown is not implemented yet; opening Funds."))
                .forEach(results::add);
        }
        catch (RuntimeException ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private void searchDonors(String query, List<GlobalSearchResult> results)
    {
        try
        {
            DonorService donorService = new DonorService();
            donorService.loadDonors(null);
            donorService.getAllDonors().stream()
                .filter(donor -> matches(query, donor.getId(), donor.getName(), donor.getEmail(), donor.getPhone()))
                .map(this::donorResult)
                .forEach(results::add);
        }
        catch (Exception ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private GlobalSearchResult donorResult(DonorContact donor)
    {
        return new GlobalSearchResult(SearchResultType.DONOR, donor.getName(), compact(donor.getEmail(), donor.getPhone()),
            null, "Donor search hit found, but donor drilldown is not implemented in the alternate shell yet.");
    }

    private void searchReports(String query, List<GlobalSearchResult> results)
    {
        try
        {
            ReportService reportService = new ReportService();
            reportService.semanticReportTemplateIds().stream()
                .filter(id -> matches(query, id))
                .map(id -> new GlobalSearchResult(SearchResultType.REPORT, id, "Semantic report template",
                    AppPanelId.REPORTS_WORKSPACE, "Opening Reports Library."))
                .forEach(results::add);
        }
        catch (RuntimeException ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private void searchCompanies(String query, List<GlobalSearchResult> results)
    {
        try
        {
            services.companyAdministration().listCompanies().stream()
                .filter(company -> matches(query, String.valueOf(company.id()), company.name()))
                .map(this::companyResult)
                .forEach(results::add);
        }
        catch (Exception ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private GlobalSearchResult companyResult(CompanyRecord company)
    {
        return new GlobalSearchResult(SearchResultType.COMPANY, company.name(),
            "Company ID " + company.id() + " • Updated " + company.updatedAt(), AppPanelId.COMPANY_ADMIN,
            "Opening Company Administration.");
    }

    private void searchImportExportHistory(String query, List<GlobalSearchResult> results)
    {
        try
        {
            ImpexRecordService impex = new ImpexRecordService();
            impex.listImportedTransactions().stream()
                .filter(row -> matches(query, String.valueOf(row)))
                .map(row -> new GlobalSearchResult(SearchResultType.IMPORT_EXPORT_HISTORY, "Imported transaction",
                    String.valueOf(row), AppPanelId.IMPORT_EXPORT,
                    "Import/export history drilldown is not implemented yet; opening Import/Export."))
                .forEach(results::add);
        }
        catch (RuntimeException ex)
        {
            // Domain unavailable for the current session.
        }
    }

    private static boolean matches(String query, Object... values)
    {
        for (Object value : values)
        {
            if (value != null && normalize(String.valueOf(value)).contains(query))
            {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String compact(Object... values)
    {
        List<String> parts = new ArrayList<>();
        for (Object value : values)
        {
            if (value != null && !String.valueOf(value).isBlank())
            {
                parts.add(String.valueOf(value));
            }
        }
        return String.join(" • ", parts);
    }

    private static String suffix(Object value)
    {
        return value == null ? "" : " • " + value;
    }
}
