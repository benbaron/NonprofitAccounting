package nonprofitbookkeeping.report.template;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;

/**
 * First-pass value binding service for workbook-modeled semantic reports.
 *
 * <p>This class intentionally does not depend on the Jasper report registry. It
 * gives the semantic JSON templates a stable preview/export path while later
 * PRs map value keys onto the mature NonprofitAccounting domain services.</p>
 */
public class WorkbookSemanticReportService
{
	private final SemanticReportRenderer textRenderer = new SemanticReportRenderer();
	private final SemanticReportFxRenderer fxRenderer = new SemanticReportFxRenderer();

	public JsonNode loadTemplate(String templateId)
	{
		return SemanticReportTemplateLoader.load(templateId);
	}

	public SemanticReportValueSet loadValues(String templateId,
		LocalDate start, LocalDate end)
	{
		LocalDate effectiveStart = start == null ? LocalDate.now().withDayOfYear(1)
			: start;
		LocalDate effectiveEnd = end == null ? LocalDate.now() : end;

		SemanticReportValueSet values = new SemanticReportValueSet();
		values.put("context.periodStart", effectiveStart);
		values.put("context.periodEnd", effectiveEnd);

		// Placeholders keep the forms renderable before final domain mappings are
		// added. Follow-up work should replace these with service/database values.
		putZero(values,
			"balanceStmt.totalAssets",
			"balanceStmt.totalLiabilities",
			"balanceStmt.totalNetAssets",
			"balanceStmt.totalLiabilitiesAndNetAssets",
			"balanceStmt.balanceCheck",
			"incomeStmt.totalIncome",
			"incomeStmt.totalExpenses",
			"incomeStmt.netIncomeLoss",
			"workbookSummary.totalAssets",
			"workbookSummary.totalLiabilities",
			"workbookSummary.totalNetAssets",
			"workbookSummary.netIncomeLoss",
			"workbookSummary.balanceCheck");

		values.putTable("balanceStmt.assets.rows", List.of());
		values.putTable("balanceStmt.liabilities.rows", List.of());
		values.putTable("balanceStmt.netAssets.rows", List.of());
		values.putTable("incomeStmt.income.rows", List.of());
		values.putTable("incomeStmt.expense.rows", List.of());
		values.putTable("transactionsList.rows", List.of());
		values.putTable("allChecksTfrs.rows", List.of());
		values.putTable("fundTransfers.rows", List.of());

		return values;
	}

	public RenderedSemanticReport renderText(String templateId, LocalDate start,
		LocalDate end)
	{
		JsonNode template = loadTemplate(templateId);
		return this.textRenderer.render(template, loadValues(templateId, start, end));
	}

	public Node renderFx(String templateId, LocalDate start, LocalDate end)
	{
		JsonNode template = loadTemplate(templateId);
		return this.fxRenderer.render(template, loadValues(templateId, start, end));
	}

	public List<String> templateIds()
	{
		return List.of("BalanceStmt", "IncomeStmt", "WorkbookSummary",
			"TransactionsList", "AllChecksTfrs", "FundTransfers");
	}

	public Map<String, String> displayNames()
	{
		Map<String, String> names = new LinkedHashMap<>();
		names.put("BalanceStmt", "Balance Statement (SCA workbook)");
		names.put("IncomeStmt", "Income Statement (SCA workbook)");
		names.put("WorkbookSummary", "Workbook Summary (SCA workbook)");
		names.put("TransactionsList", "Transactions List (SCA workbook)");
		names.put("AllChecksTfrs", "All Checks & Transfers (SCA workbook)");
		names.put("FundTransfers", "Fund Transfers (SCA workbook)");
		return names;
	}

	private static void putZero(SemanticReportValueSet values, String... keys)
	{
		for (String key : keys)
		{
			values.put(key, BigDecimal.ZERO);
		}
	}
}
