
package nonprofitbookkeeping.ui.actions;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.jasperreports.engine.JRException;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Swing {@link javax.swing.Action} base class that generates Jasper reports
 * using the {@link ReportService}. Subclasses specify the {@link ReportType}
 * they wish to produce and may further customise the {@link ReportContext}
 * before generation.
 */
public abstract class SwingReportAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported date formats for parsing transaction dates when
	 * {@link AccountingTransaction#getBookingDateTimestamp()} is not populated.
	 */
	private static final DateTimeFormatter[] DATE_FORMATS =
	{
		DateTimeFormatter.ISO_LOCAL_DATE,
		DateTimeFormatter.ofPattern("M/d/yyyy"),
		DateTimeFormatter.ofPattern("MM/dd/yyyy")
	};
	
	private final ReportService reportService;
	private final ReportType reportType;
	private final String reportDisplayName;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected SwingReportAction(ReportService reportService,
		ReportType reportType,
		String reportDisplayName)
	{
		this.reportService =
			Objects.requireNonNull(reportService, "reportService");
		this.reportType = Objects.requireNonNull(reportType, "reportType");
		this.reportDisplayName =
			Objects.requireNonNull(reportDisplayName, "reportDisplayName");
		
	}
	
	@Override
	public void actionPerformed(ActionEvent event)
	{
		
		if (!isCompanyOpen())
		{
			handleNoCompanyOpen();
			return;
		}
		
		Company company = getCurrentCompany();
		ReportContext context = new ReportContext();
		context.setReportType(this.reportType.id());
		LocalDate[] range = resolveDateRange(company);
		context.setStartDate(range[0]);
		context.setEndDate(range[1]);
		context.setOutputFormat(defaultOutputFormat());
		
		configureContext(context, company);
		
		try
		{
			File output = this.reportService
				.generateJasperReport(context, context.getOutputFormat());
			handleSuccess(output);
		}
		catch (JRException | IOException ex)
		{
			this.logger.warn("Failed to generate {} report",
				this.reportDisplayName,
				ex);
			handleError(ex);
		}
		
	}
	
	/**
	 * Allows subclasses to add report-specific context information prior to
	 * report generation.
	 *
	 * @param context the context that will be supplied to {@link ReportService}
	 * @param company the active company (may be {@code null})
	 */
	protected void configureContext(ReportContext context, Company company)
	{
		
		// Default implementation does nothing.
	}
	
	/**
	 * @return the output format that should be used when exporting the report.
	 */
	protected String defaultOutputFormat()
	{
		return "pdf";
		
	}
	
	/**
	 * Determines whether a company is currently open. Subclasses may override
	 * to inject test data.
	 */
	protected boolean isCompanyOpen()
	{
		return CurrentCompany.isOpen();
		
	}
	
	/**
	 * Retrieves the active company. Subclasses may override to inject test data.
	 */
	protected Company getCurrentCompany()
	{
		return CurrentCompany.getCompany();
		
	}
	
	private LocalDate[] resolveDateRange(Company company)
	{
		LocalDate today = LocalDate.now();
		LocalDate fallbackStart = today.withDayOfYear(1);
		LocalDate fallbackEnd = today;
		LocalDate start = null;
		LocalDate end = null;
		
		if (company != null)
		{
			Ledger ledger = company.getLedger();
			
			if (ledger != null)
			{
				List<AccountingTransaction> transactions =
					ledger.getTransactions();
				
				for (AccountingTransaction transaction : transactions)
				{
					LocalDate date = extractDate(transaction);
					
					if (date == null)
					{
						continue;
					}
					
					if (start == null || date.isBefore(start))
					{
						start = date;
					}
					
					if (end == null || date.isAfter(end))
					{
						end = date;
					}
					
				}
				
			}
			
		}
		
		if (start == null)
		{
			start = (end != null) ? end : fallbackStart;
		}
		
		if (end == null)
		{
			end = (start != null) ? start : fallbackEnd;
		}
		
		if (end.isBefore(start))
		{
			end = start;
		}
		
		return new LocalDate[]
		{ start, end };
		
	}
	
	private LocalDate extractDate(AccountingTransaction transaction)
	{
		
		if (transaction == null)
		{
			return null;
		}
		
		Long timestamp = transaction.getBookingDateTimestamp();
		
		if (timestamp != null && timestamp > 0L)
		{
			return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}
		
		String rawDate = transaction.getDate();
		
		if (rawDate == null)
		{
			return null;
		}
		
		String trimmed = rawDate.trim();
		
		if (trimmed.isEmpty())
		{
			return null;
		}
		
		for (DateTimeFormatter formatter : DATE_FORMATS)
		{
			
			try
			{
				return LocalDate.parse(trimmed, formatter);
			}
			catch (Exception ignore)
			{
				// Try next format
			}
			
		}
		
		return null;
		
	}
	
	/** Displays a warning when no company is currently open. */
	protected void handleNoCompanyOpen()
	{
		showDialog(
			"No company is currently open. Open or create a company to generate " +
				this.reportDisplayName + " reports.",
			JOptionPane.WARNING_MESSAGE);
		
	}
	
	/** Displays a success message once the report has been generated. */
	protected void handleSuccess(File output)
	{
		String path =
			(output != null) ? output.getAbsolutePath() : "(unknown location)";
		showDialog(this.reportDisplayName + " report generated at: " + path,
			JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	/** Displays an error message when report generation fails. */
	protected void handleError(Exception ex)
	{
		String message = (ex != null && ex.getMessage() != null) ?
			ex.getMessage() : "Unknown error";
		showDialog(
			"Unable to generate " + this.reportDisplayName + " report: " +
				message,
			JOptionPane.ERROR_MESSAGE);
		
	}
	
	/**
	 * Shows a dialog if the environment supports Swing; otherwise logs the
	 * message. Subclasses may override to intercept the messaging for tests.
	 */
	protected void showDialog(String message, int messageType)
	{
		
		if (!GraphicsEnvironment.isHeadless())
		{
			JOptionPane.showMessageDialog(null, message, "Reports",
				messageType);
		}
		else
		{
			switch (messageType)
			{
				case JOptionPane.ERROR_MESSAGE -> this.logger.error(message);
				case JOptionPane.WARNING_MESSAGE -> this.logger.warn(message);
				default -> this.logger.info(message);
			};
		}
		
	}
	
	/** @return the backing {@link ReportService}. */
	protected ReportService getReportService()
	{
		return this.reportService;
		
	}
	
	/** @return the report type generated by this action. */
	protected ReportType getReportType()
	{
		return this.reportType;
		
	}
	
	/** @return a human readable name for the generated report. */
	protected String getReportDisplayName()
	{
		return this.reportDisplayName;
		
	}
	
}
