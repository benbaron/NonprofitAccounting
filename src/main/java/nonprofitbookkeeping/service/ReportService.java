package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.report.template.RenderedSemanticReport;
import nonprofitbookkeeping.report.template.WorkbookSemanticReportService;
import nonprofitbookkeeping.reports.ReportMetadata;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class responsible for semantic workbook-modeled report generation.
 *
 * <p>The former JasperReports registry/generator path has been removed. Reports
 * now flow through the semantic JSON report templates and renderers.</p>
 */
public class ReportService
{
	/** Logger for this class. */
	public static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

	private final WorkbookSemanticReportService semanticReports;

	/** Default constructor uses the semantic workbook report service. */
	public ReportService()
	{
		this(new WorkbookSemanticReportService());
	}

	public ReportService(WorkbookSemanticReportService semanticReports)
	{
		this.semanticReports = semanticReports;
	}

	/**
	 * Returns the available semantic workbook report template ids.
	 *
	 * @return immutable list of template ids
	 */
	public List<String> semanticReportTemplateIds()
	{
		return this.semanticReports.templateIds();
	}

	/**
	 * Generates a semantic workbook report to the user's report output folder.
	 *
	 * @param templateId semantic template id, such as {@code BalanceStmt}
	 * @param start start date for period reports
	 * @param end end date for period reports
	 * @param outputFormat {@code text}, {@code txt}, or {@code csv}; unsupported
	 *                     values fall back to text
	 * @return generated file
	 * @throws IOException if the report cannot be written
	 */
	public File generateSemanticReport(String templateId, LocalDate start,
		LocalDate end, String outputFormat) throws IOException
	{
		if (templateId == null || templateId.isBlank())
		{
			throw new IllegalArgumentException("templateId is required.");
		}

		RenderedSemanticReport report = this.semanticReports.renderText(templateId,
			start, end);
		String fmt = outputFormat == null ? "text" : outputFormat.trim().toLowerCase();
		boolean csv = "csv".equals(fmt);
		String extension = csv ? ".csv" : ".txt";
		String payload = csv ? report.csv() : report.text();

		File dir = reportOutputDirectory();
		if (!dir.exists() && !dir.mkdirs())
		{
			throw new IOException("Could not create report output directory: " + dir);
		}

		File out = new File(dir,
			templateId + "_" + System.currentTimeMillis() + extension);
		Files.writeString(out.toPath(), payload, StandardCharsets.UTF_8);
		LOGGER.info("Semantic report generated: {}", out.getAbsolutePath());
		return out;
	}

	/**
	 * Backward-compatible plain-text entry point for callers that have not yet
	 * moved to {@link #generateSemanticReport(String, LocalDate, LocalDate, String)}.
	 *
	 * @param templateId report/template id
	 * @param start start date
	 * @param end end date
	 * @return generated file
	 * @throws IOException if the report cannot be written
	 */
	public File generatePlainTextReport(String templateId, LocalDate start,
		LocalDate end) throws IOException
	{
		return generateSemanticReport(templateId, start, end, "text");
	}

	/**
	 * Calculates the balance of the provided account using the supplied
	 * collection of accounting entries. The account's opening balance is used
	 * as the starting value and each entry is applied according to the account's
	 * {@link AccountSide increase side}.
	 *
	 * @param account the {@link Account} whose balance should be calculated
	 * @param entries the accounting entries affecting the account. Entries for
	 *                other accounts are ignored
	 * @return the resulting balance as a {@link BigDecimal}
	 */
	public static BigDecimal calculateBalanceForAccount(Account account,
		Collection<AccountingEntry> entries)
	{
		if (account == null)
		{
			throw new NullPointerException(
				"Account cannot be null for balance calculation.");
		}

		BigDecimal balance = account.getOpeningBalance() == null
			? BigDecimal.ZERO
			: account.getOpeningBalance();

		if (entries == null)
		{
			return balance;
		}

		AccountSide increaseSide = account.getIncreaseSide();

		if (increaseSide == null)
		{
			LOGGER.warn("Account {} has no defined increase side.",
				account.getAccountNumber());
			return balance;
		}

		for (AccountingEntry entry : entries)
		{
			if (entry == null || entry.getAmount() == null)
			{
				continue;
			}

			if (!account.getAccountNumber().equals(entry.getAccountNumber()))
			{
				continue;
			}

			if (increaseSide == AccountSide.DEBIT)
			{
				if (entry.getAccountSide() == AccountSide.DEBIT)
				{
					balance = balance.add(entry.getAmount());
				}
				else
				{
					balance = balance.subtract(entry.getAmount());
				}
			}
			else
			{
				if (entry.getAccountSide() == AccountSide.CREDIT)
				{
					balance = balance.add(entry.getAmount());
				}
				else
				{
					balance = balance.subtract(entry.getAmount());
				}
			}
		}

		return balance;
	}

	/**
	 * Lists metadata of previously generated reports.
	 *
	 * @return report metadata for files in the report output directory
	 */
	public static List<ReportMetadata> listGeneratedReports()
	{
		List<ReportMetadata> results = new ArrayList<>();
		File dir = reportOutputDirectory();

		if (dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();

			if (files != null)
			{
				for (File f : files)
				{
					if (!f.isFile())
					{
						continue;
					}

					String created = Instant.ofEpochMilli(f.lastModified()).toString();
					results.add(new ReportMetadata(f.getName(), created,
						f.getAbsolutePath()));
				}

				results.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));
			}
		}

		return results;
	}

	private static File reportOutputDirectory()
	{
		return new File(System.getProperty("user.home"),
			"NonprofitBookkeepingReports");
	}
}
