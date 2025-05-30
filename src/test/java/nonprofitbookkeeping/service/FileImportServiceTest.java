
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.impex.ImportedTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webcohesion.ofx4j.io.OFXParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class FileImportServiceTest
{
	
	@Mock private ChartOfAccounts mockChartOfAccounts;
	@Mock private Ledger mockLedger;
	@Mock private Account mockTargetAccount;
	@Mock private Account mockNeedsCatAccount;
	
	private final String NEEDS_CAT_ACC_NUM = FileImportService.NEEDS_CATEGORIZATION_ACCOUNT_NUMBER;
	private final String FITID_KEY = "IMPORT_ID";
	
	
	@BeforeEach
		void setUp()
	{
		lenient().when(mockNeedsCatAccount.getAccountNumber()).thenReturn(NEEDS_CAT_ACC_NUM);
		lenient().when(mockNeedsCatAccount.getIncreaseSide()).thenReturn(AccountSide.DEBIT);
		lenient().when(mockChartOfAccounts.getAccount(NEEDS_CAT_ACC_NUM))
			.thenReturn(mockNeedsCatAccount);
		
		lenient().when(mockTargetAccount.getAccountNumber()).thenReturn("TARGET_ACC_NUM");
		lenient().when(mockTargetAccount.getIncreaseSide()).thenReturn(AccountSide.DEBIT);
		lenient().when(mockChartOfAccounts.getAccount("TARGET_ACC_NUM"))
			.thenReturn(mockTargetAccount);
		
		lenient().when(mockLedger.getTransactions()).thenReturn(new ArrayList<>());
	}
	
	private InputStream getResourceAsStream(String resourceName) throws FileNotFoundException
	{
		URL resourceUrl = getClass().getClassLoader().getResource("sample_files/" + resourceName);
		
		if (resourceUrl == null)
		{
			throw new FileNotFoundException("Resource not found: sample_files/" + resourceName);
		}
		
		try
		{
			return new FileInputStream(new File(resourceUrl.toURI()));
		}
		catch (URISyntaxException e)
		{
			throw new FileNotFoundException("Invalid URI syntax for resource: " + e.getMessage());
		}
		
	}
	
	// I. OFX Parsing Tests
	@Test
		void testParseOfx_ValidBankStatement() throws IOException, OFXParseException
	{
		
		try (InputStream is = getResourceAsStream("sample_bank.ofx"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseOfx(is);
			assertEquals(2, transactions.size());
			ImportedTransaction tx1 = transactions.get(0);
			assertEquals(LocalDate.of(2023, 1, 5), tx1.getDatePosted());
			assertEquals(0, new BigDecimal("-25.50").compareTo(tx1.getAmount()));
			assertEquals("FITID001", tx1.getTransactionId());
		}
		
	}
	
	@Test
		void testParseOfx_ValidCreditCardStatement() throws IOException, OFXParseException
	{
		
		try (InputStream is = getResourceAsStream("sample_cc.ofx"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseOfx(is);
			assertEquals(3, transactions.size());
			ImportedTransaction tx1 = transactions.get(0); // Payment
			assertEquals(LocalDate.of(2023, 2, 5), tx1.getDatePosted());
			assertEquals(0, new BigDecimal("150.00").compareTo(tx1.getAmount()));
		}
		
	}
	
	@Test
		void testParseOfx_MalformedFile_viaImportFile() throws URISyntaxException
	{
		URL resourceUrl = getClass().getClassLoader().getResource("sample_files/malformed.ofx");
		File malformedFile = new File(resourceUrl.toURI());
		List<AccountingTransaction> result = FileImportService.importFile(malformedFile,
			mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertTrue(result.isEmpty());
	}
	
	@Test
		void testParseOfx_EmptyTransactionList() throws IOException, OFXParseException
	{
		
		try (InputStream is = getResourceAsStream("empty_txns.ofx"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseOfx(is);
			assertTrue(transactions.isEmpty());
		}
		
	}
	
	// II. QIF Parsing Tests
	@Test
		void testParseQif_ValidBankFile() throws IOException
	{
		
		try (InputStream is = getResourceAsStream("sample_bank.qif"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseQif(is, "Bank");
			assertEquals(4, transactions.size());
			ImportedTransaction tx3 = transactions.get(2); // D1/20/23
			assertEquals(LocalDate.of(2023, 1, 20), tx3.getDatePosted());
			assertEquals(0, new BigDecimal("-105.00").compareTo(tx3.getAmount()));
			assertEquals("CK2345", tx3.getTransactionId());
		}
		
	}
	
	@Test
		void testParseQif_ValidCCardFile() throws IOException
	{
		
		try (InputStream is = getResourceAsStream("sample_cc.qif"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseQif(is, "CreditCard");
			assertEquals(3, transactions.size());
			assertEquals("CREDITCARD", transactions.get(0).getOriginalAccountType());
		}
		
	}
	
	@Test
		void testParseQif_UnsupportedType() throws IOException
	{
		
		try (InputStream is = getResourceAsStream("unsupported_type.qif"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseQif(is, "Bank"); // Hint
																								// is
																								// "Bank"
			assertEquals(1, transactions.size());
			assertEquals("Bank", transactions.get(0).getOriginalAccountType());
		}
		
	}
	
	@Test
		void testParseQif_MalformedDate() throws IOException
	{
		
		try (InputStream is = getResourceAsStream("malformed_date.qif"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseQif(is, "Bank");
			assertEquals(2, transactions.size()); // Only valid date transactions
		}
		
	}
	
	@Test
		void testParseQif_MissingCaretAtEnd() throws IOException
	{
		
		try (InputStream is = getResourceAsStream("no_caret_end.qif"))
		{
			List<ImportedTransaction> transactions = FileImportService.parseQif(is, "Bank");
			assertEquals(1, transactions.size());
		}
		
	}
	
	// III. Transaction Mapping Tests
	@Test
		void testMap_DebitNormalAccount_DepositAndWithdrawal()
	{
		when(mockTargetAccount.getIncreaseSide()).thenReturn(AccountSide.DEBIT);
		when(mockTargetAccount.getAccountNumber()).thenReturn("BANK123");
		
		ImportedTransaction deposit = new ImportedTransaction(LocalDate.now(),
			new BigDecimal("100.00"), "D", "m", "fit1", "USD", "BANK", "orig");
		ImportedTransaction withdrawal = new ImportedTransaction(LocalDate.now(),
			new BigDecimal("-50.00"), "W", "m", "fit2", "USD", "BANK", "orig");
		List<ImportedTransaction> imported = List.of(deposit, withdrawal);
		
		List<AccountingTransaction> result = FileImportService.mapToAccountingTransactions(imported,
			mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertEquals(2, result.size());
		// Check deposit
		AccountingTransaction atDeposit = result.stream()
			.filter(r -> "fit1".equals(r.getInfo().get(FITID_KEY))).findFirst().get();
		assertTrue(atDeposit.getEntries().stream()
			.anyMatch(e -> "BANK123".equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.DEBIT &&
				e.getAmount().compareTo(new BigDecimal("100.00")) == 0));
		assertTrue(atDeposit.getEntries().stream()
			.anyMatch(e -> NEEDS_CAT_ACC_NUM.equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.CREDIT));
		// Check withdrawal
		AccountingTransaction atWithdrawal = result.stream()
			.filter(r -> "fit2".equals(r.getInfo().get(FITID_KEY))).findFirst().get();
		assertTrue(atWithdrawal.getEntries().stream()
			.anyMatch(e -> "BANK123".equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.CREDIT &&
				e.getAmount().compareTo(new BigDecimal("50.00")) == 0));
		assertTrue(atWithdrawal.getEntries().stream()
			.anyMatch(e -> NEEDS_CAT_ACC_NUM.equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.DEBIT));
	}
	
	@Test
		void testMap_CreditNormalAccount_ChargeAndPayment()
	{
		when(mockTargetAccount.getIncreaseSide()).thenReturn(AccountSide.CREDIT);
		when(mockTargetAccount.getAccountNumber()).thenReturn("CC123");
		
		ImportedTransaction charge = new ImportedTransaction(LocalDate.now(),
			new BigDecimal("-75.00"), "Charge", "m", "fit1", "USD", "CC", "orig");
		ImportedTransaction payment = new ImportedTransaction(LocalDate.now(),
			new BigDecimal("100.00"), "Payment", "m", "fit2", "USD", "CC", "orig");
		List<ImportedTransaction> imported = List.of(charge, payment);
		
		List<AccountingTransaction> result = FileImportService.mapToAccountingTransactions(imported,
			mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertEquals(2, result.size());
		// Check charge
		AccountingTransaction atCharge = result.stream()
			.filter(r -> "fit1".equals(r.getInfo().get(FITID_KEY))).findFirst().get();
		assertTrue(atCharge.getEntries().stream()
			.anyMatch(e -> "CC123".equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.CREDIT &&
				e.getAmount().compareTo(new BigDecimal("75.00")) == 0));
		assertTrue(atCharge.getEntries().stream()
			.anyMatch(e -> NEEDS_CAT_ACC_NUM.equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.DEBIT));
		// Check payment
		AccountingTransaction atPayment = result.stream()
			.filter(r -> "fit2".equals(r.getInfo().get(FITID_KEY))).findFirst().get();
		assertTrue(atPayment.getEntries().stream()
			.anyMatch(e -> "CC123".equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.DEBIT &&
				e.getAmount().compareTo(new BigDecimal("100.00")) == 0));
		assertTrue(atPayment.getEntries().stream()
			.anyMatch(e -> NEEDS_CAT_ACC_NUM.equals(e.getAccountNumber()) &&
				e.getAccountSide() == AccountSide.CREDIT));
	}
	
	@Test
		void testMap_MissingNeedsCategorizationAccount()
	{
		when(mockChartOfAccounts.getAccount(NEEDS_CAT_ACC_NUM)).thenReturn(null);
		ImportedTransaction impTxn = new ImportedTransaction(LocalDate.now(), BigDecimal.TEN,
			"Test", null, "1", null, null, null);
		Exception ex = assertThrows(IllegalArgumentException.class,
			() -> FileImportService.mapToAccountingTransactions(List.of(impTxn), mockTargetAccount,
				mockChartOfAccounts, mockLedger));
		assertTrue(ex.getMessage().contains("'Needs Categorization' account not found"));
	}
	
	// IV. Duplicate Detection Tests
	@Test
		void testDuplicate_Ofx_FITIDMatch()
	{
		ImportedTransaction impTxn = new ImportedTransaction(LocalDate.now(), BigDecimal.TEN,
			"Test OFX", null, "DUPLICATE_FITID", "USD", "BANK", "orig");
		AccountingTransaction existingTx = Mockito.mock(AccountingTransaction.class);
		when(existingTx.getInfo()).thenReturn(Map.of(FITID_KEY, "DUPLICATE_FITID"));
		when(mockLedger.getTransactions()).thenReturn(null);
		
		List<AccountingTransaction> result = FileImportService.mapToAccountingTransactions(
			List.of(impTxn), mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertTrue(result.isEmpty());
	}
	
	@Test
		void testDuplicate_Qif_HeuristicMatch()
	{
		LocalDate txDate = LocalDate.of(2023, 3, 15);
		BigDecimal txAmount = new BigDecimal("25.00"); // Positive for QIF usually means
														// deposit-like for bank
		String txDesc = "QIF Heuristic Match";
		
		ImportedTransaction impTxn = new ImportedTransaction(txDate, txAmount, txDesc, null,
			"QIF_N_FIELD", "USD", "BANK", "orig_qif");
		
		AccountingTransaction existingTx = Mockito.mock(AccountingTransaction.class);
		when(existingTx.getBookingDateTimestamp())
			.thenReturn(txDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
		when(existingTx.getMemo()).thenReturn(txDesc);
		
		Set<AccountingEntry> entries = new HashSet<>();
		// For targetAccount (DEBIT normal), a positive impTxn.amount (deposit) means
		// DEBIT to target.
		// So existingTxAmountForTarget should be positive.
		entries.add(new AccountingEntry(txAmount, "TARGET_ACC_NUM", AccountSide.DEBIT));
		entries.add(new AccountingEntry(txAmount, NEEDS_CAT_ACC_NUM, AccountSide.CREDIT));
		when(existingTx.getEntries()).thenReturn(entries);
		when(mockLedger.getTransactions()).thenReturn(null);
		
		List<AccountingTransaction> result = FileImportService.mapToAccountingTransactions(
			List.of(impTxn), mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertTrue(result.isEmpty());
	}
	
	@Test
		void testDuplicate_NoMatch()
	{
		ImportedTransaction impTxn = new ImportedTransaction(LocalDate.now(), BigDecimal.TEN,
			"Unique OFX", null, "UNIQUE_FITID", "USD", "BANK", "orig");
		when(mockLedger.getTransactions()).thenReturn(null, null);
		
		List<AccountingTransaction> result = FileImportService.mapToAccountingTransactions(
			List.of(impTxn), mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertEquals(1, result.size());
	}
	
	// V. importFile Integration Tests
	@Test
		void testImportFile_SuccessOFX() throws URISyntaxException
	{
		URL resourceUrl = getClass().getClassLoader().getResource("sample_files/sample_bank.ofx");
		File file = new File(resourceUrl.toURI());
		List<AccountingTransaction> result =
			FileImportService.importFile(file, mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertEquals(2, result.size());
	}
	
	@Test
		void testImportFile_SuccessQIF() throws URISyntaxException
	{
		URL resourceUrl = getClass().getClassLoader().getResource("sample_files/sample_bank.qif");
		File file = new File(resourceUrl.toURI());
		List<AccountingTransaction> result =
			FileImportService.importFile(file, mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertEquals(4, result.size()); // From sample_bank.qif
	}
	
	@Test
		void testImportFile_FileNotFound()
	{
		File nonExistentFile = new File("non_existent_file.ofx");
		List<AccountingTransaction> result = FileImportService.importFile(nonExistentFile,
			mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertTrue(result.isEmpty());
	}
	
	@Test
		void testImportFile_UnsupportedType() throws IOException
	{
		File unsupportedFile = File.createTempFile("unsupported", ".txt");
		unsupportedFile.deleteOnExit(); // Ensure cleanup
		List<AccountingTransaction> result = FileImportService.importFile(unsupportedFile,
			mockTargetAccount, mockChartOfAccounts, mockLedger);
		assertTrue(result.isEmpty());
	}
	
}
