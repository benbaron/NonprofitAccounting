package nonprofitbookkeeping.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sf.jasperreports.engine.JRException;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;
import nonprofitbookkeeping.reports.ReportContext;

/**
 * Tests for {@link SwingReportAction} and its concrete subclasses.
 */
public class SwingReportActionTest
{
        @Test
        public void actionRequiresOpenCompany()
        {
                CapturingReportService service = new CapturingReportService();
                Company company = new Company();
                TestSwingAction action = new TestSwingAction(service, ReportType.INCOME_STATEMENT_JASPER,
                        "Income Statement", false, company);

                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                assertEquals(0, service.invocationCount);
                assertTrue(action.messages().stream().anyMatch(msg -> msg.contains("No company")));
        }

        @Test
        public void actionUsesLedgerDatesWhenAvailable()
        {
                CapturingReportService service = new CapturingReportService();
                Company company = companyWithTransactions();
                TestSwingAction action = new TestSwingAction(service, ReportType.BALANCE_SHEET_JASPER,
                        "Balance Sheet", true, company);

                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                assertEquals(1, service.invocationCount);
                assertEquals(ReportType.BALANCE_SHEET_JASPER.id(), service.lastContext.getReportType());
                assertEquals(LocalDate.of(2021, 2, 5), service.lastContext.getStartDate());
                assertEquals(LocalDate.of(2022, 3, 10), service.lastContext.getEndDate());
                assertEquals("pdf", service.lastFormat);
                assertTrue(action.messages().stream().anyMatch(msg -> msg.contains("Balance Sheet report generated")));
        }

        @Test
        public void actionReportsErrorsFromService()
        {
                FailingReportService service = new FailingReportService();
                Company company = new Company();
                TestSwingAction action = new TestSwingAction(service, ReportType.TRIAL_BALANCE_JASPER,
                        "Trial Balance", true, company);

                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                assertEquals(1, service.invocationCount);
                assertTrue(action.messages().stream().anyMatch(msg -> msg.contains("Unable to generate Trial Balance report")));
        }

        @Test
        public void incomeStatementActionTargetsCorrectReportType()
        {
                CapturingReportService service = new CapturingReportService();
                Company company = new Company();
                TestIncomeStatementAction action = new TestIncomeStatementAction(service, company);

                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                assertEquals(ReportType.INCOME_STATEMENT_JASPER.id(), service.lastContext.getReportType());
        }

        @Test
        public void balanceSheetActionTargetsCorrectReportType()
        {
                        CapturingReportService service = new CapturingReportService();
                        Company company = new Company();
                        TestBalanceSheetAction action = new TestBalanceSheetAction(service, company);

                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                        assertEquals(ReportType.BALANCE_SHEET_JASPER.id(), service.lastContext.getReportType());
        }

        @Test
        public void trialBalanceActionTargetsCorrectReportType()
        {
                        CapturingReportService service = new CapturingReportService();
                        Company company = new Company();
                        TestTrialBalanceAction action = new TestTrialBalanceAction(service, company);

                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                        assertEquals(ReportType.TRIAL_BALANCE_JASPER.id(), service.lastContext.getReportType());
        }

        @Test
        public void cashFlowActionTargetsCorrectReportType()
        {
                        CapturingReportService service = new CapturingReportService();
                        Company company = new Company();
                        TestCashFlowAction action = new TestCashFlowAction(service, company);

                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                        assertEquals(ReportType.CASH_FLOW_STATEMENT_JASPER.id(), service.lastContext.getReportType());
        }

        @Test
        public void accountActivityActionTargetsTransactionReport()
        {
                        CapturingReportService service = new CapturingReportService();
                        Company company = new Company();
                        TestAccountActivityAction action = new TestAccountActivityAction(service, company);

                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));

                        assertEquals(ReportType.TRANSACTION_REPORT_JASPER.id(), service.lastContext.getReportType());
        }

        private static Company companyWithTransactions()
        {
                Company company = new Company();

                AccountingTransaction early = new AccountingTransaction();
                early.setBookingDateTimestamp(LocalDate.of(2021, 2, 5)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli());

                AccountingTransaction later = new AccountingTransaction();
                later.setDate("2022-03-10");

                company.getLedger().getJournal().replaceAllTransactions(List.of(early, later));
                return company;
        }

        private static final class CapturingReportService extends ReportService
        {
                private ReportContext lastContext;
                private String lastFormat;
                private int invocationCount;

                private CapturingReportService()
                {
                        super(Collections.emptyMap());
                }

                @Override
                public File generateJasperReport(ReportContext ctx, String outputFormat)
                {
                        this.invocationCount++;
                        this.lastContext = ctx;
                        this.lastFormat = outputFormat;
                        return new File("test-" + this.invocationCount + ".pdf");
                }
        }

        private static final class FailingReportService extends ReportService
        {
                private int invocationCount;

                private FailingReportService()
                {
                        super(Collections.emptyMap());
                }

                @Override
                public File generateJasperReport(ReportContext ctx, String outputFormat)
                        throws JRException, IOException
                {
                        this.invocationCount++;
                        throw new IOException("Simulated failure");
                }
        }

        private static class TestSwingAction extends SwingReportAction
        {
                private static final long serialVersionUID = 1L;

                private final boolean open;
                private final Company company;
                private final List<String> messages = new ArrayList<>();

                private TestSwingAction(ReportService service,
                        ReportType type,
                        String name,
                        boolean open,
                        Company company)
                {
                        super(service, type, name);
                        this.open = open;
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return this.open;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                        this.messages.add(message);
                }

                List<String> messages()
                {
                        return this.messages;
                }
        }

        private static class TestIncomeStatementAction extends GenerateIncomeStatementAction
        {
                private static final long serialVersionUID = 1L;
                private final Company company;

                private TestIncomeStatementAction(ReportService service, Company company)
                {
                        super(service);
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return true;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                        // no-op for tests
                }
        }

        private static class TestBalanceSheetAction extends GenerateBalanceSheetAction
        {
                private static final long serialVersionUID = 1L;
                private final Company company;

                private TestBalanceSheetAction(ReportService service, Company company)
                {
                        super(service);
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return true;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                }
        }

        private static class TestTrialBalanceAction extends GenerateTrialBalanceAction
        {
                private static final long serialVersionUID = 1L;
                private final Company company;

                private TestTrialBalanceAction(ReportService service, Company company)
                {
                        super(service);
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return true;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                }
        }

        private static class TestCashFlowAction extends GenerateCashFlowStatementAction
        {
                private static final long serialVersionUID = 1L;
                private final Company company;

                private TestCashFlowAction(ReportService service, Company company)
                {
                        super(service);
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return true;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                }
        }

        private static class TestAccountActivityAction extends GenerateAccountActivityReportAction
        {
                private static final long serialVersionUID = 1L;
                private final Company company;

                private TestAccountActivityAction(ReportService service, Company company)
                {
                        super(service);
                        this.company = company;
                }

                @Override
                protected boolean isCompanyOpen()
                {
                        return true;
                }

                @Override
                protected Company getCurrentCompany()
                {
                        return this.company;
                }

                @Override
                protected void showDialog(String message, int messageType)
                {
                }
        }
}
