package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.records.BudgetRecord;
import nonprofitbookkeeping.persistence.records.BudgetRecordRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bridges budget workspace panels to persisted budget records.
 */
public class BudgetWorkspaceStore
{
	private final BudgetRecordRepository repository;

	public BudgetWorkspaceStore()
	{
		this(new BudgetRecordRepository());
	}

	BudgetWorkspaceStore(BudgetRecordRepository repository)
	{
		this.repository = repository;
	}

	public List<BudgetEditorPanel.BudgetRow> loadEditorRows() throws SQLException
	{
		List<BudgetEditorPanel.BudgetRow> rows = new ArrayList<>();
		for (BudgetRecord budget : repository.listAll())
		{
			String fund = budget.fundId() == null ? "General" : budget.fundId();
			for (BudgetRecord.BudgetLineRecord line : budget.lines())
			{
				rows.add(new BudgetEditorPanel.BudgetRow(
					blankToEmpty(line.accountId()),
					fund,
					blankToEmpty(line.eventName()),
					amountText(line.budgetedAmount())));
			}
		}
		return rows;
	}

	public int saveEditorRows(List<BudgetEditorPanel.BudgetRow> rows) throws SQLException
	{
		Map<String, List<BudgetRecord.BudgetLineRecord>> byFund = new LinkedHashMap<>();
		for (BudgetEditorPanel.BudgetRow row : rows)
		{
			String fund = defaultFund(row.fund());
			byFund.computeIfAbsent(fund, ignored -> new ArrayList<>())
				.add(new BudgetRecord.BudgetLineRecord(
					blankToEmpty(row.period()),
					parseAmount(row.amount()),
					null,
					null,
					blankToEmpty(row.account()),
					null,
					Map.of()));
		}

		int written = 0;
		for (Map.Entry<String, List<BudgetRecord.BudgetLineRecord>> entry : byFund.entrySet())
		{
			String fund = entry.getKey();
			String budgetId = "ui-budget-" + slug(fund);
			BudgetRecord record = new BudgetRecord(
				budgetId,
				"Budget " + fund,
				2026,
				fund,
				true,
				"Saved from Budget Editor",
				entry.getValue(),
				Map.of("source", "budget-editor-ui"),
				null);
			repository.upsert(record);
			written += entry.getValue().size();
		}
		return written;
	}

	public List<BudgetVsActualPanel.GroupRow> loadBudgetVsActual() throws SQLException
	{
		Map<String, Map<String, BigDecimal>> byFundThenAccount = new LinkedHashMap<>();
		for (BudgetRecord budget : repository.listAll())
		{
			String fund = defaultFund(budget.fundId());
			Map<String, BigDecimal> accountTotals =
				byFundThenAccount.computeIfAbsent(fund, ignored -> new LinkedHashMap<>());
			for (BudgetRecord.BudgetLineRecord line : budget.lines())
			{
				String account = blankToEmpty(line.accountId());
				BigDecimal amount = line.budgetedAmount() == null ?
					BigDecimal.ZERO : line.budgetedAmount();
				accountTotals.merge(account, amount, BigDecimal::add);
			}
		}

		List<BudgetVsActualPanel.GroupRow> groups = new ArrayList<>();
		for (Map.Entry<String, Map<String, BigDecimal>> fundEntry : byFundThenAccount.entrySet())
		{
			List<BudgetVsActualPanel.AccountRow> accounts = fundEntry.getValue().entrySet().stream()
				.map(e -> new BudgetVsActualPanel.AccountRow(e.getKey(), e.getValue(), BigDecimal.ZERO))
				.sorted(Comparator.comparing(BudgetVsActualPanel.AccountRow::account))
				.toList();
			groups.add(new BudgetVsActualPanel.GroupRow(fundEntry.getKey(), accounts));
		}
		return groups;
	}

	private static String slug(String text)
	{
		return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
	}

	private static String defaultFund(String fund)
	{
		return (fund == null || fund.isBlank()) ? "General" : fund.trim();
	}

	private static BigDecimal parseAmount(String amount)
	{
		if (amount == null || amount.isBlank())
		{
			return BigDecimal.ZERO;
		}
		return new BigDecimal(amount.trim());
	}

	private static String amountText(BigDecimal amount)
	{
		return amount == null ? "0.00" : amount.toPlainString();
	}

	private static String blankToEmpty(String value)
	{
		return value == null ? "" : value;
	}
}
