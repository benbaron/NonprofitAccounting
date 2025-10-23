package nonprofitbookkeeping.model.ofx;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

class InvestmentTransactionTest
{
        private Company company;
        private Account investment;
        private Account cash;

        @BeforeEach void setUp()
        {
                this.company = new Company();
                this.investment = new Account("500", "Brokerage", AccountType.INVEST, BigDecimal.ZERO);
                this.cash = new Account("600", "Settlement Cash", AccountType.CASH, BigDecimal.ZERO);

                this.company.getChartOfAccounts().addAccount(this.investment);
                this.company.getChartOfAccounts().addAccount(this.cash);

                AccountingTransaction buy = AccountingTransactionBuilder.create()
                        .debit(new BigDecimal("120.00"), this.investment.getAccountNumber())
                        .credit(new BigDecimal("120.00"), this.cash.getAccountNumber())
                        .build();
                buy.setBookingDateTimestamp(10L);
                this.company.getLedger().getJournal().addTransaction(buy);

                CurrentCompany.forceCompanyLoad(this.company);
        }

        @AfterEach void tearDown()
        {
                CurrentCompany.forceCompanyLoad(null);
        }

        @Test void getTotalReturnsNetBalanceForAccount()
        {
                assertEquals(new BigDecimal("120.00"), InvestmentTransaction.getTotal(this.investment));

                AccountingTransaction sell = AccountingTransactionBuilder.create()
                        .debit(new BigDecimal("20.00"), this.cash.getAccountNumber())
                        .credit(new BigDecimal("20.00"), this.investment.getAccountNumber())
                        .build();
                sell.setBookingDateTimestamp(11L);
                this.company.getLedger().getJournal().addTransaction(sell);

                assertEquals(new BigDecimal("100.00"), InvestmentTransaction.getTotal(this.investment));
        }

        @Test void getTotalGracefullyHandlesMissingContext()
        {
                CurrentCompany.forceCompanyLoad(null);
                assertEquals(BigDecimal.ZERO, InvestmentTransaction.getTotal(this.investment));
                assertEquals(BigDecimal.ZERO, InvestmentTransaction.getTotal(null));
        }
}
