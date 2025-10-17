package nonprofitbookkeeping.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.FileExportService.ExportResult;
import nonprofitbookkeeping.service.FileExportService.StatementFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileExportServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void exportAccountStatement_writesOfxFile() throws Exception
    {
        Company company = buildCompany("Example Org");
        ChartOfAccounts coa = company.getChartOfAccounts();
        Account bank = createAccount("1000", "Checking", AccountType.CHECKING, AccountSide.DEBIT);
        Account income = createAccount("4000", "Donations", AccountType.INCOME, AccountSide.CREDIT);
        coa.addAccount(bank);
        coa.addAccount(income);

        AccountingTransaction deposit = AccountingTransactionBuilder.create()
                .debit(new BigDecimal("150.00"), bank.getAccountNumber())
                .credit(new BigDecimal("150.00"), income.getAccountNumber())
                .build();
        deposit.setDate("2024-01-05");
        deposit.setMemo("Donation");
        company.getLedger().getJournal().addTransaction(deposit);

        Path output = this.tempDir.resolve("statement.ofx");
        ExportResult result = FileExportService.exportAccountStatement(output.toFile(),
                company,
                bank,
                StatementFormat.OFX);

        assertTrue(result.success(), "Export should succeed");
        assertEquals(1, result.transactionsWritten());
        assertTrue(Files.exists(output));
        String content = Files.readString(output);
        assertTrue(content.contains("<OFX>"));
        assertTrue(content.contains("<TRNAMT>150.00"));
        assertTrue(content.contains("CHECKING"));
    }

    @Test
    void exportAccountStatement_writesQfxWithNegativeCharges() throws Exception
    {
        Company company = buildCompany("Sample Charity");
        ChartOfAccounts coa = company.getChartOfAccounts();
        Account card = createAccount("2000", "Visa", AccountType.CREDITCARD, AccountSide.CREDIT);
        Account expense = createAccount("6000", "Supplies", AccountType.EXPENSE, AccountSide.DEBIT);
        coa.addAccount(card);
        coa.addAccount(expense);

        AccountingTransaction charge = AccountingTransactionBuilder.create()
                .debit(new BigDecimal("75.00"), expense.getAccountNumber())
                .credit(new BigDecimal("75.00"), card.getAccountNumber())
                .build();
        charge.setDate("2024-02-10");
        charge.setMemo("Art supplies");
        company.getLedger().getJournal().addTransaction(charge);

        Path output = this.tempDir.resolve("card.qfx");
        ExportResult result = FileExportService.exportAccountStatement(output.toFile(),
                company,
                card,
                StatementFormat.QFX);

        assertTrue(result.success());
        String content = Files.readString(output);
        assertTrue(content.contains("<CREDITCARDMSGSRSV1>"));
        assertTrue(content.contains("<TRNAMT>-75.00"));
    }

    private static Company buildCompany(String name)
    {
        Company company = new Company();
        company.getCompanyProfile().setCompanyName(name);
        company.getCompanyProfile().setBaseCurrency("USD");
        company.getCompanyProfile().setDefaultBankAccount("123456789");
        return company;
    }

    private static Account createAccount(String number,
            String name,
            AccountType type,
            AccountSide increaseSide)
    {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setName(name);
        account.setAccountType(type);
        account.setIncreaseSide(increaseSide);
        return account;
    }
}
