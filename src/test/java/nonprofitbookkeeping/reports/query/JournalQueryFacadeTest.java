
package nonprofitbookkeeping.reports.query;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.JournalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalQueryFacadeTest
{
	@Mock private JournalRepository journalRepository;
	
	private JournalQueryFacade facade;
	private AccountingTransaction depositTxn;
	private AccountingTransaction transferTxn;
	
	@BeforeEach
	void setUp() throws Exception
	{
		this.facade = new JournalQueryFacade(this.journalRepository);
		this.depositTxn = buildTransaction("2024-01-10", "Annual dues",
			"deposit", "1000", 150.25);
		this.transferTxn = buildTransaction("2024-02-05", "Transfer to savings",
			"transfer", "2000", -75.50);
		
		when(this.journalRepository.listTransactions())
			.thenReturn(List.of(this.depositTxn, this.transferTxn));
		
	}
	
	@Test
	void filtersByDateAccountAndMemo()
	{
		JournalQueryCriteria criteria = JournalQueryCriteria.builder()
			.dateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
			.addAccountNumber("1000")
			.memoContains("dues")
			.transactionType("deposit")
			.build();
		
		List<AccountingTransaction> results =
			this.facade.fetchTransactions(criteria);
		assertEquals(1, results.size());
		assertEquals(this.depositTxn, results.get(0));
		
	}
	
	@Test
	void filtersRequireAllAccountsWhenRequested() throws Exception
	{
		AccountingTransaction multiAccount = buildTransaction("2024-03-01",
			"Split entry", "deposit", "1000", 10.0);
		multiAccount.getEntries()
			.add(new AccountingEntry(BigDecimal.TEN, "3000",
				AccountSide.CREDIT, "Income"));
		
		when(this.journalRepository.listTransactions())
			.thenReturn(
				List.of(this.depositTxn, this.transferTxn, multiAccount));
		
		JournalQueryCriteria criteria = JournalQueryCriteria.builder()
			.addAccountNumber("1000")
			.addAccountNumber("3000")
			.accountMatchMode(JournalQueryCriteria.AccountMatchMode.ALL)
			.transactionType("deposit")
			.build();
		
		List<AccountingTransaction> results =
			this.facade.fetchTransactions(criteria);
		assertEquals(1, results.size());
		assertEquals(multiAccount, results.get(0));
		
	}
	
	@Test
	void generatorMapsTransactionsToScaBeans()
	{
		JournalQueryCriteria criteria = JournalQueryCriteria.builder()
			.transactionType("deposit")
			.build();
		
		ScaTransferInReportDataGenerator generator =
			new ScaTransferInReportDataGenerator(this.facade);
		List<TRANSFER_IN_9Bean> beans = generator.generateBeans(criteria);
		
		assertEquals(1, beans.size());
		TRANSFER_IN_9Bean bean = beans.get(0);
		assertEquals("Annual dues", bean.getSca_funds_transferred_detail_in());
		assertEquals("2024-01-10", bean.getCheck_date());
		assertEquals("150.25", bean.getAmount());
		
	}
	
	private AccountingTransaction buildTransaction(String date, String memo,
		String type, String accountNumber, double amount)
	{
		AccountingTransaction txn = new AccountingTransaction();
		txn.setDate(date);
		txn.setMemo(memo);
		txn.setToFrom("Canton of Ealdormere");
		txn.setCheckNumber("CHK-" + date);
		
		Map<String, String> info = new LinkedHashMap<>();
		info.put("transactionType", type);
		txn.setInfo(info);
		
		AccountingEntry entry = new AccountingEntry(
			BigDecimal.valueOf(amount), accountNumber, AccountSide.DEBIT,
			"Bank");
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		entries.add(entry);
		txn.setEntries(entries);
		return txn;
		
	}
	
}
