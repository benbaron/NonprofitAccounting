
package nonprofitbookkeeping.service;

import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.banking.BankAccountDetails;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponse;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.common.TransactionList;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponse;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Ledger; // Added
import nonprofitbookkeeping.model.impex.ImportedTransaction;
import nonprofitbookkeeping.model.impex.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant; // Added
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections; // Added
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileImportService
{
	private static final Logger LOGGER = Logger.getLogger(FileImportService.class.getName());
	public static final String NEEDS_CATEGORIZATION_ACCOUNT_NUMBER = "SUSPENSE-UNCATEGORIZED";
	private static final String FITID_KEY = "IMPORT_ID";
	
	// --- OFX Parsing Logic ---
	// Package-private for testing
	static List<ImportedTransaction> parseOfx(InputStream inputStream)	throws IOException,
																		OFXParseException
	{
		List<ImportedTransaction> importedTransactions = new ArrayList<>();
		AggregateUnmarshaller<ResponseEnvelope> unmarshaller =
			new AggregateUnmarshaller<>(ResponseEnvelope.class);
		ResponseEnvelope envelope = unmarshaller.unmarshal(inputStream);
		
		
		BankStatementResponseTransaction bankResponse =	
			(BankStatementResponseTransaction) 
			envelope
			.getMessageSet(MessageSetType.banking)
			.getResponseMessages();
		
		if (bankResponse != null)
		{
			
			for (BankStatementResponse statement : bankResponse.getWrappedMessage())
			{
				BankAccountDetails bankAccount = statement.getAccount();
				String accountNumber = bankAccount.getAccountNumber();
				String accountType = bankAccount.getAccountType() != null ?
					bankAccount.getAccountType().toString() : "BANK";
				String currencyCode = statement.getCurrencyCode();
				TransactionList transactionList = statement.getTransactionList();
				
				if (transactionList != null)
				{
					
					for (Transaction ofxTransaction : transactionList.getTransactions())
					{
						importedTransactions.add(mapToImportedTransaction(ofxTransaction,
							accountNumber, accountType, currencyCode));
					}
					
				}
				
			}
			
		}
		
		CreditCardStatementResponseTransaction ccResponse =
			(CreditCardStatementResponseTransaction) envelope
				.getMessageSet(MessageSetType.creditcard)
				.getResponseMessages();
		
		if (ccResponse != null)
		{
			
			for (CreditCardStatementResponse statement : ccResponse.getStatements())
			{
				CreditCardAccountDetails ccAccount =
					statement.getAccount();
				String accountNumber = ccAccount.getAccountNumber();
				String accountType = "CREDITCARD";
				String currencyCode = statement.getCurrencyCode();
				TransactionList transactionList = statement.getTransactionList();
				
				if (transactionList != null)
				{
					
					for (Transaction ofxTransaction : transactionList.getTransactions())
					{
						importedTransactions.add(mapToImportedTransaction(ofxTransaction,
							accountNumber, accountType, currencyCode));
					}
					
				}
				
			}
			
		}
		
		return importedTransactions;
	}
	
	private static ImportedTransaction mapToImportedTransaction(Transaction ofxTransaction,
																String accountNumber,
																String accountType,
																String currencyCode)
	{
		ImportedTransaction importedTx = new ImportedTransaction();
		Date datePosted = ofxTransaction.getDatePosted();
		
		if (datePosted != null)
		{
			importedTx
				.setDatePosted(datePosted.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		
		Double amount = ofxTransaction.getAmount();
		
		if (amount != null)
		{
			importedTx.setAmount(BigDecimal.valueOf(amount));
		}
		
		importedTx.setDescription(ofxTransaction.getName());
		importedTx.setMemo(ofxTransaction.getMemo());
		importedTx.setTransactionId(ofxTransaction.getId()); // This is FITID
		
		if (ofxTransaction.getCurrency() != null)
		{
			importedTx.setCurrency(ofxTransaction.getCurrency());
		}
		else
		{
			importedTx.setCurrency(currencyCode);
		}
		
		importedTx.setOriginalAccountType(accountType);
		importedTx.setOriginalAccountNumber(accountNumber);
		return importedTx;
	}
	
	// --- QIF Parsing Logic ---
	// Package-private for testing
	static List<ImportedTransaction> parseQif(	InputStream inputStream,
												String targetAccountTypeHint) throws IOException
	{
		List<ImportedTransaction> importedTransactions = new ArrayList<>();
		ImportedTransaction currentTx = null;
		String line;
		String qifAccountType = targetAccountTypeHint;
		
		try (BufferedReader reader =
			new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
		{
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				if (line.startsWith("!Type:"))
				{
					String typeHeader = line.substring(6).toUpperCase();
					if (typeHeader.startsWith("BANK"))
						qifAccountType = "BANK";
					else if (typeHeader.startsWith("CCARD"))
						qifAccountType = "CREDITCARD";
					else if (typeHeader.startsWith("CASH"))
						qifAccountType = "CASH";
					else
						LOGGER.warning("Unsupported QIF account type in header: " + typeHeader +
							". Using hint: " + targetAccountTypeHint);
					continue;
				}
				
				if (currentTx == null)
				{
					if (line.equals("^"))
						continue;
					currentTx = new ImportedTransaction();
					currentTx.setOriginalAccountType(qifAccountType);
				}
				
				char fieldCode = line.charAt(0);
				String value = (line.length() > 1) ? line.substring(1) : "";
				
				switch(fieldCode)
				{
					case 'D':
						try
						{
							currentTx.setDatePosted(parseQifDate(value));
						}
						catch (DateTimeParseException e)
						{
							LOGGER.warning(
								"Could not parse QIF date '" + value + "': " + e.getMessage());
						}
						break;
					
					case 'T':
						try
						{
							currentTx.setAmount(new BigDecimal(value.replace(",", "")));
						}
						catch (NumberFormatException e)
						{
							LOGGER.warning(
								"Could not parse QIF amount '" + value + "': " + e.getMessage());
						}
						break;
					
					case 'P':
						currentTx.setDescription(value);
						break;
					
					case 'M':
						currentTx.setMemo(
							(currentTx.getMemo() == null ? "" : currentTx.getMemo() + " ") + value);
						break;
					
					case 'N':
						currentTx.setTransactionId(value);
						break;
					
					case 'L':
						currentTx.setMemo((currentTx.getMemo() == null ? "" :
							currentTx.getMemo() + " [Category/Transfer: " + value + "]"));
						break;
					
					case '^':
						if (isValidTransaction(currentTx))
							importedTransactions.add(currentTx);
						else
							LOGGER.warning("Skipping incomplete QIF transaction: " + currentTx);
						currentTx = null;
						break;
				}
				
			}
			
			if (currentTx != null && isValidTransaction(currentTx))
			{
				importedTransactions.add(currentTx);
			}
			
		}
		
		return importedTransactions;
	}
	
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MDY_YY =
		new DateTimeFormatterBuilder()
			.appendPattern("M/d/yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear())
			.toFormatter();
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MD_YY_APOSTROPHE =
		new DateTimeFormatterBuilder()
			.appendPattern("M/d''yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear())
			.toFormatter();
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MDYYYY =
		DateTimeFormatter.ofPattern("M/d/yyyy");
	private static final DateTimeFormatter QIF_DATE_FORMATTER_DMY_YY =
		new DateTimeFormatterBuilder()
			.appendPattern("d/M/yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear())
			.toFormatter();
	private static final DateTimeFormatter QIF_DATE_FORMATTER_DMYYYY =
		DateTimeFormatter.ofPattern("d/M/yyyy");
	
	private static
			List<ImportedTransaction>
			parseQif1(InputStream inputStream, String targetAccountTypeHint) throws IOException
	{
		List<ImportedTransaction> importedTransactions = new ArrayList<>();
		ImportedTransaction currentTx = null;
		String line;
		String qifAccountType = targetAccountTypeHint;
		
		try (BufferedReader reader =
			new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
		{
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				if (line.startsWith("!Type:"))
				{
					String typeHeader = line.substring(6).toUpperCase();
					if (typeHeader.startsWith("BANK"))
						qifAccountType = "BANK";
					else if (typeHeader.startsWith("CCARD"))
						qifAccountType = "CREDITCARD";
					else if (typeHeader.startsWith("CASH"))
						qifAccountType = "CASH";
					else
						LOGGER.warning("Unsupported QIF account type in header: " + typeHeader +
							". Using hint: " + targetAccountTypeHint);
					continue;
				}
				
				if (currentTx == null)
				{
					if (line.equals("^"))
						continue;
					currentTx = new ImportedTransaction();
					currentTx.setOriginalAccountType(qifAccountType);
				}
				
				char fieldCode = line.charAt(0);
				String value = (line.length() > 1) ? line.substring(1) : "";
				
				switch(fieldCode)
				{
					case 'D':
						try
						{
							currentTx.setDatePosted(parseQifDate(value));
						}
						catch (DateTimeParseException e)
						{
							LOGGER.warning(
								"Could not parse QIF date '" + value + "': " + e.getMessage());
						}
						break;
					
					case 'T':
						try
						{
							currentTx.setAmount(new BigDecimal(value.replace(",", "")));
						}
						catch (NumberFormatException e)
						{
							LOGGER.warning(
								"Could not parse QIF amount '" + value + "': " + e.getMessage());
						}
						break;
					
					case 'P':
						currentTx.setDescription(value);
						break;
					
					case 'M':
						currentTx.setMemo(
							(currentTx.getMemo() == null ? "" : currentTx.getMemo() + " ") + value);
						break;
					
					case 'N':
						currentTx.setTransactionId(value);
						break; // QIF 'N' field used as transactionId for heuristic check
						
					case 'L':
						currentTx.setMemo((currentTx.getMemo() == null ? "" :
							currentTx.getMemo() + " [Category/Transfer: " + value + "]"));
						break;
					
					case '^':
						if (isValidTransaction(currentTx))
							importedTransactions.add(currentTx);
						else
							LOGGER.warning("Skipping incomplete QIF transaction: " + currentTx);
						currentTx = null;
						break;
				}
				
			}
			
			if (currentTx != null && isValidTransaction(currentTx))
			{
				importedTransactions.add(currentTx);
			}
			
		}
		
		return importedTransactions;
	}
	
	private static LocalDate parseQifDate(String dateStr) throws DateTimeParseException
	{
		
		try
		{
			return LocalDate.parse(dateStr, QIF_DATE_FORMATTER_MDYYYY);
		}
		catch (DateTimeParseException ignored)
		{
		}
		
		try
		{
			return LocalDate.parse(dateStr, QIF_DATE_FORMATTER_MDY_YY);
		}
		catch (DateTimeParseException ignored)
		{
		}
		
		try
		{
			return LocalDate.parse(dateStr, QIF_DATE_FORMATTER_MD_YY_APOSTROPHE);
		}
		catch (DateTimeParseException ignored)
		{
		}
		
		try
		{
			return LocalDate.parse(dateStr, QIF_DATE_FORMATTER_DMYYYY);
		}
		catch (DateTimeParseException ignored)
		{
		}
		
		try
		{
			return LocalDate.parse(dateStr, QIF_DATE_FORMATTER_DMY_YY);
		}
		catch (DateTimeParseException ignored)
		{
		}
		
		return LocalDate.parse(dateStr);
	}
	
	private static boolean isValidTransaction(ImportedTransaction tx)
	{
		return tx.getDatePosted() != null && tx.getAmount() != null;
	}
	
	// --- Duplicate Detection & Mapping Logic ---
	// Package-private for testing (though isPotentialDuplicate remains private and
	// is tested via this)
	static List<AccountingTransaction> mapToAccountingTransactions(
																	List<
																		ImportedTransaction> importedTxns,
																	Account targetAccount,
																	ChartOfAccounts chartOfAccounts,
																	Ledger existingLedger)
	{
		
		List<AccountingTransaction> accountingTransactions = new ArrayList<>();
		Account needsCategorizationAccount =
			chartOfAccounts.getAccount(NEEDS_CATEGORIZATION_ACCOUNT_NUMBER);
		
		if (needsCategorizationAccount == null)
		{
			LOGGER.severe("Critical: 'Needs Categorization' account ('" +
				NEEDS_CATEGORIZATION_ACCOUNT_NUMBER + "') not found. Cannot map transactions.");
			throw new IllegalArgumentException("'Needs Categorization' account not found.");
		}
		
		List<ImportedTransaction> nonDuplicateImportedTxns = new ArrayList<>();
		List<ImportedTransaction> skippedDuplicateTxns = new ArrayList<>();
		
		for (ImportedTransaction impTxn : importedTxns)
		{
			
			if (isPotentialDuplicate(impTxn, targetAccount, existingLedger))
			{
				skippedDuplicateTxns.add(impTxn);
			}
			else
			{
				nonDuplicateImportedTxns.add(impTxn);
			}
			
		}
		
		if (!skippedDuplicateTxns.isEmpty())
		{
			LOGGER.info(
				"Skipped " + skippedDuplicateTxns.size() + " potential duplicate transactions.");
		}
		
		for (ImportedTransaction impTxn : nonDuplicateImportedTxns)
		{
			
			if (impTxn.getDatePosted() == null || impTxn.getAmount() == null)
			{
				LOGGER.warning(
					"Skipping imported transaction with missing date or amount: " + impTxn);
				continue;
			}
			
			long bookingDateTimestamp =
				impTxn.getDatePosted().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
			String memo =
				impTxn.getDescription() != null ? impTxn.getDescription() : impTxn.getMemo();
			if (memo == null || memo.trim().isEmpty())
				memo = "Imported Transaction";
			
			Map<String, String> info = new HashMap<>();
			
			if (impTxn.getTransactionId() != null && !impTxn.getTransactionId().isEmpty())
			{
				info.put(FITID_KEY, impTxn.getTransactionId());
			}
			
			if (impTxn.getOriginalAccountNumber() != null)
				info.put("ORIGINAL_ACCOUNT_NUMBER", impTxn.getOriginalAccountNumber());
			if (impTxn.getOriginalAccountType() != null)
				info.put("ORIGINAL_ACCOUNT_TYPE", impTxn.getOriginalAccountType());
			if (impTxn.getMemo() != null && !impTxn.getMemo().equals(memo))
				info.put("ORIGINAL_MEMO", impTxn.getMemo());
			
			Set<AccountingEntry> entries = new HashSet<>();
			BigDecimal entryAmount = impTxn.getAmount().abs();
			AccountSide targetAccountSide;
			AccountSide needsCatAccountSide;
			
			if (targetAccount.getIncreaseSide() == AccountSide.DEBIT)
			{
				
				if (impTxn.getAmount().compareTo(BigDecimal.ZERO) >= 0)
				{
					targetAccountSide = AccountSide.DEBIT;
					needsCatAccountSide = AccountSide.CREDIT;
				}
				else
				{
					targetAccountSide = AccountSide.CREDIT;
					needsCatAccountSide = AccountSide.DEBIT;
				}
				
			}
			else
			{
				
				if (impTxn.getAmount().compareTo(BigDecimal.ZERO) >= 0)
				{
					targetAccountSide = AccountSide.DEBIT;
					needsCatAccountSide = AccountSide.CREDIT;
				}
				else
				{
					targetAccountSide = AccountSide.CREDIT;
					needsCatAccountSide = AccountSide.DEBIT;
				}
				
			}
			
			entries.add(new AccountingEntry(entryAmount, targetAccount.getAccountNumber(),
				targetAccountSide));
			entries.add(new AccountingEntry(entryAmount,
				needsCategorizationAccount.getAccountNumber(), needsCatAccountSide));
			
			AccountingTransaction newAt = new AccountingTransaction(
				targetAccount,
				entries,
				info,
				bookingDateTimestamp);
			newAt.setMemo(memo);
			accountingTransactions.add(newAt);
		}
		
		return accountingTransactions;
	}
	
	private static boolean isPotentialDuplicate(ImportedTransaction impTxn, Account targetAccount,
												Ledger existingLedger)
	{
		
		if (existingLedger == null || existingLedger.getTransactions() == null)
		{
			return false;
		}
		
		// OFX Duplicate Check (Primary using FITID)
		if (impTxn.getTransactionId() != null && !impTxn.getTransactionId().isEmpty() &&
			(impTxn.getOriginalAccountType().equals("BANK") ||
				impTxn.getOriginalAccountType().equals("CREDITCARD")))
		{ // FITID is strong for OFX
			
			for (AccountingTransaction existingTx : existingLedger.getTransactions())
			{
				Map<String, String> info = existingTx.getInfo();
				
				if (info != null && impTxn.getTransactionId().equals(info.get(FITID_KEY)))
				{
					return true; // Found duplicate by FITID
				}
				
			}
			
		}
		
		// QIF/Fallback Heuristic Duplicate Check
		// Exact match on Date, abs(Amount), and Payee/Description (case-sensitive for
		// V1 simplicity)
		// for transactions associated with the targetAccount.
		for (AccountingTransaction existingTx : existingLedger.getTransactions())
		{
			boolean targetAccountMatchInExistingTx = false;
			
			if (existingTx.getEntries() != null)
			{
				
				for (AccountingEntry entry : existingTx.getEntries())
				{
					
					if (entry.getAccountNumber().equals(targetAccount.getAccountNumber()))
					{
						targetAccountMatchInExistingTx = true;
						break;
					}
					
				}
				
			}
			
			if (!targetAccountMatchInExistingTx)
				continue; // Skip if existing TX doesn't involve the target account
				
			LocalDate existingTxDate = Instant.ofEpochMilli(existingTx.getBookingDateTimestamp())
				.atZone(ZoneId.systemDefault()).toLocalDate();
			
			// Compare date
			if (!impTxn.getDatePosted().equals(existingTxDate))
			{
				continue;
			}
			
			// Compare amount (absolute value)
			// Need to determine the amount related to the targetAccount in existingTx
			BigDecimal existingTxAmountForTarget = BigDecimal.ZERO;
			
			for (AccountingEntry entry : existingTx.getEntries())
			{
				
				if (entry.getAccountNumber().equals(targetAccount.getAccountNumber()))
				{
					
					if (targetAccount.getIncreaseSide() == entry.getAccountSide())
					{ // Inflow-like for target
						existingTxAmountForTarget =
							existingTxAmountForTarget.add(entry.getAmount());
					}
					else
					{ // Outflow-like for target
						existingTxAmountForTarget =
							existingTxAmountForTarget.subtract(entry.getAmount());
					}
					
				}
				
			}
			
			// For comparison, we use the imported transaction's amount sign convention.
			// If target is DEBIT normal (Bank): positive impTxn.getAmount() is inflow
			// (DEBIT).
			// If target is CREDIT normal (CC): negative impTxn.getAmount() is inflow
			// (CREDIT to liability, but represents a charge).
			// This heuristic is tricky. Simpler: compare absolute values of
			// impTxn.getAmount() with transaction's total impact on target account.
			// For V1: We compare impTxn.getAmount() with how it *would* affect the target
			// account.
			// If target is DEBIT normal: impTxn.getAmount() is what we care about.
			// If target is CREDIT normal: impTxn.getAmount() (e.g. charge -100 for CC) is
			// what we care about.
			// The existingTxAmountForTarget above calculates the net change to
			// targetAccount.
			// So, compare impTxn.getAmount() with existingTxAmountForTarget.
			if (impTxn.getAmount().compareTo(existingTxAmountForTarget) != 0)
			{
				continue;
			}
			
			// Compare description (payee)
			String impDescription = impTxn.getDescription() != null ? impTxn.getDescription() : "";
			String existingDescription = existingTx.getMemo() != null ? existingTx.getMemo() : ""; // Or
																									// a
																									// specific
																									// entry's
																									// memo/description
			// For V1 heuristic, using existingTx.getMemo() as a general descriptor.
			// A more robust check might look at the description of the "Needs
			// Categorization" entry.
			
			if (!impDescription.equals(existingDescription))
			{
				
				if (existingTx.getEntries() != null)
				{ // Check entry memos if main memo doesn't match
					boolean entryMemoMatch = false;
					
					for (AccountingEntry entry : existingTx.getEntries())
					{
						
						if (!entry.getAccountNumber().equals(targetAccount.getAccountNumber()) && // check
																									// other
																									// side
							entry.getTransaction() != null &&
							entry.getTransaction().getMemo() != null &&
							entry.getTransaction().getMemo().equals(impDescription))
						{
							entryMemoMatch = true;
							break;
						}
						
					}
					
					if (!entryMemoMatch)
						continue;
				}
				else
				{
					continue;
				}
				
			}
			
			// If all match, it's a potential duplicate by heuristic
			LOGGER.info(
				"Potential QIF duplicate found: Imported=" + impTxn + " vs Existing=" + existingTx);
			return true;
		}
		
		return false;
	}
	
	private static List<AccountingTransaction> mapToAccountingTransactions1(
					List<ImportedTransaction> importedTxns,
					Account targetAccount,
					ChartOfAccounts chartOfAccounts,
					Ledger existingLedger)
	
	{ // Added existingLedger
		
		List<AccountingTransaction> accountingTransactions = new ArrayList<>();
		Account needsCategorizationAccount =
			chartOfAccounts.getAccount(NEEDS_CATEGORIZATION_ACCOUNT_NUMBER);
		
		if (needsCategorizationAccount == null)
		{
			LOGGER.severe("Critical: 'Needs Categorization' account ('" +
				NEEDS_CATEGORIZATION_ACCOUNT_NUMBER + "') not found. Cannot map transactions.");
			throw new IllegalArgumentException("'Needs Categorization' account not found.");
		}
		
		List<ImportedTransaction> nonDuplicateImportedTxns = new ArrayList<>();
		List<ImportedTransaction> skippedDuplicateTxns = new ArrayList<>();
		
		for (ImportedTransaction impTxn : importedTxns)
		{
			
			if (isPotentialDuplicate(impTxn, targetAccount, existingLedger))
			{
				skippedDuplicateTxns.add(impTxn);
			}
			else
			{
				nonDuplicateImportedTxns.add(impTxn);
			}
			
		}
		
		if (!skippedDuplicateTxns.isEmpty())
		{
			LOGGER.info(
				"Skipped " + skippedDuplicateTxns.size() + " potential duplicate transactions.");
			// Optionally log more details about skipped transactions
		}
		// Store skipped duplicates count somewhere accessible by ImportFileActionFX if
		// needed for UI.
		// For now, just logging. The return of this method is only non-duplicates.
		
		for (ImportedTransaction impTxn : nonDuplicateImportedTxns)
		{ // Process only non-duplicates
			
			if (impTxn.getDatePosted() == null || impTxn.getAmount() == null)
			{
				LOGGER.warning(
					"Skipping imported transaction with missing date or amount: " + impTxn);
				continue;
			}
			
			long bookingDateTimestamp =
				impTxn.getDatePosted().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
			String memo =
				impTxn.getDescription() != null ? impTxn.getDescription() : impTxn.getMemo();
			if (memo == null || memo.trim().isEmpty())
				memo = "Imported Transaction";
			
			Map<String, String> info = new HashMap<>();
			
			if (impTxn.getTransactionId() != null && !impTxn.getTransactionId().isEmpty())
			{
				info.put(FITID_KEY, impTxn.getTransactionId());
			}
			
			if (impTxn.getOriginalAccountNumber() != null)
				info.put("ORIGINAL_ACCOUNT_NUMBER", impTxn.getOriginalAccountNumber());
			if (impTxn.getOriginalAccountType() != null)
				info.put("ORIGINAL_ACCOUNT_TYPE", impTxn.getOriginalAccountType());
			if (impTxn.getMemo() != null && !impTxn.getMemo().equals(memo))
				info.put("ORIGINAL_MEMO", impTxn.getMemo());
			
			Set<AccountingEntry> entries = new HashSet<>();
			BigDecimal entryAmount = impTxn.getAmount().abs();
			AccountSide targetAccountSide;
			AccountSide needsCatAccountSide;
			
			if (targetAccount.getIncreaseSide() == AccountSide.DEBIT)
			{
				
				if (impTxn.getAmount().compareTo(BigDecimal.ZERO) >= 0)
				{
					targetAccountSide = AccountSide.DEBIT;
					needsCatAccountSide = AccountSide.CREDIT;
				}
				else
				{
					targetAccountSide = AccountSide.CREDIT;
					needsCatAccountSide = AccountSide.DEBIT;
				}
				
			}
			else
			{
				
				if (impTxn.getAmount().compareTo(BigDecimal.ZERO) >= 0)
				{
					targetAccountSide = AccountSide.DEBIT;
					needsCatAccountSide = AccountSide.CREDIT;
				}
				else
				{
					targetAccountSide = AccountSide.CREDIT;
					needsCatAccountSide = AccountSide.DEBIT;
				}
				
			}
			
			entries.add(new AccountingEntry(entryAmount, targetAccount.getAccountNumber(),
				targetAccountSide));
			entries.add(new AccountingEntry(entryAmount,
				needsCategorizationAccount.getAccountNumber(), needsCatAccountSide));
			
			AccountingTransaction newAt = new AccountingTransaction(
				targetAccount,
				entries,
				info,
				bookingDateTimestamp);
			newAt.setMemo(memo);
			accountingTransactions.add(newAt);
		}
		
		return accountingTransactions;
	}
	
	// Updated importFile method signature
	public static List<AccountingTransaction> importFile(	File file, Account targetAccountInCOA,
															ChartOfAccounts chartOfAccounts,
															Ledger ledger)
	{ // Added Ledger
		
		if (file == null || !file.exists())
		{
			LOGGER.warning("Import file is null or does not exist.");
			return Collections.emptyList();
		}
		
		if (targetAccountInCOA == null)
		{
			LOGGER.warning("Target account for import is null.");
			return Collections.emptyList();
		}
		
		if (chartOfAccounts == null)
		{
			LOGGER.warning("Chart of Accounts is null. Cannot process import mapping.");
			return Collections.emptyList();
		}
		
		if (ledger == null)
		{ // Added check for ledger
			LOGGER.warning("Ledger is null. Cannot check for duplicates or save transactions.");
			return Collections.emptyList();
		}
		
		String fileName = file.getName().toLowerCase();
		List<ImportedTransaction> importedTxns = new ArrayList<>();
		
		try (FileInputStream fis = new FileInputStream(file))
		{
			
			if (fileName.endsWith(".ofx") || fileName.endsWith(".qfx"))
			{
				importedTxns = parseOfx(fis);
				LOGGER.info("Successfully parsed " + importedTxns.size() +
					" transactions from OFX file: " + file.getAbsolutePath());
			}
			else if (fileName.endsWith(".qif"))
			{
				String accountTypeHint = targetAccountInCOA.getAccountType() != null ?
					targetAccountInCOA.getAccountType().toUpperCase() : "BANK";
				
				if (AccountType.LIABILITY.name().equals(accountTypeHint) ||
					"CREDITCARD".equals(accountTypeHint))
				{
					accountTypeHint = "CREDITCARD";
				}
				else
				{
					accountTypeHint = "BANK";
				}
				
				importedTxns = parseQif(fis, accountTypeHint);
				LOGGER.info("Successfully parsed " + importedTxns.size() +
					" transactions from QIF file: " + file.getAbsolutePath());
			}
			else
			{
				LOGGER.warning("Unsupported file type for import: " + file.getName());
				return Collections.emptyList();
			}
			
		}
		catch (OFXParseException e)
		{
			LOGGER.log(Level.SEVERE, "Failed to parse OFX file: " + file.getAbsolutePath(), e);
			return Collections.emptyList();
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE,
				"Error reading or parsing import file: " + file.getAbsolutePath(), e);
			return Collections.emptyList();
		}
		
		if (importedTxns.isEmpty())
		{
			return Collections.emptyList();
		}
		
		try
		{
			// Pass existingLedger to mapToAccountingTransactions
			return mapToAccountingTransactions(importedTxns, targetAccountInCOA, chartOfAccounts,
				ledger);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.log(Level.SEVERE, "Error mapping transactions: " + e.getMessage(), e);
			return Collections.emptyList();
		}
		
	}
	
}
