
package nonprofitbookkeeping.service;

import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessage;
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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class for importing financial transactions from various file formats like OFX and QIF.
 * It handles parsing these files, mapping the data to intermediate {@link ImportedTransaction} objects,
 * detecting potential duplicates against existing ledger data, and finally converting them into
 * the application's standard {@link AccountingTransaction} format.
 * Transactions are typically mapped to a target account and a "needs categorization" suspense account.
 */
public class FileImportService
{
	/** Logger for this class. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(FileImportService.class);
	/**
	 * The account number designated for transactions that need further categorization after import.
	 * This is a suspense account.
	 */
	public static final String NEEDS_CATEGORIZATION_ACCOUNT_NUMBER =
		"SUSPENSE-UNCATEGORIZED";
	/** Key used in transaction info map to store the original Financial Institution Transaction ID (FITID). */
	private static final String FITID_KEY = "IMPORT_ID";
	
	/**
	 * Searches the entire {@link ChartOfAccounts} for an account whose name
	 * matches the provided value, ignoring case and trimming whitespace. Returns
	 * {@code null} if no matching account is found or if inputs are invalid.
	 *
	 * @param chart The chart of accounts to search.
	 * @param name  The account name to look up.
	 * @return The matching {@link Account} or {@code null} if none found.
	 */
	public static Account findAccountIgnoreCase(ChartOfAccounts chart,
		String name)
	{
		
		if (chart == null || name == null)
		{
			return null;
		}
		
		String target = name.trim();
		
		for (Account a : chart.getAccounts())
		{
			
			if (target.equalsIgnoreCase(a.getName().trim()))
			{
				return a;
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * Parses an OFX (Open Financial Exchange) file from an input stream and converts its transactions
	 * into a list of {@link ImportedTransaction} objects.
	 * It handles both banking (BankStatementResponse) and credit card (CreditCardStatementResponse) messages.
	 *
	 * @param inputStream The input stream of the OFX file.
	 * @return A list of {@link ImportedTransaction} objects parsed from the OFX data.
	 * @throws IOException If an I/O error occurs while reading the stream.
	 * @throws OFXParseException If an error occurs during the parsing of the OFX content.
	 */
	static List<ImportedTransaction> parseOfx(InputStream inputStream)
		throws IOException,
		OFXParseException
	{
		List<ImportedTransaction> importedTransactions = new ArrayList<>();
		String sanitizedOfx = sanitizeOfxPayload(inputStream);
		validateOfxStructure(sanitizedOfx);
		AggregateUnmarshaller<ResponseEnvelope> unmarshaller =
			new AggregateUnmarshaller<>(ResponseEnvelope.class);
		
		try (InputStream sanitizedStream =
			new ByteArrayInputStream(
				sanitizedOfx.getBytes(StandardCharsets.UTF_8)))
		{
			ResponseEnvelope envelope = unmarshaller.unmarshal(sanitizedStream);
			
			ResponseMessageSet bankMessageSet =
				envelope.getMessageSet(MessageSetType.banking);
			
			if (bankMessageSet != null)
			{
				List<ResponseMessage> bankingResponses =
					bankMessageSet.getResponseMessages();
				
				if (bankingResponses != null)
				{
					
					for (ResponseMessage responseMessage : bankingResponses)
					{
						BankStatementResponseTransaction bankResponse =
							(BankStatementResponseTransaction) responseMessage;
						
						if (bankResponse != null &&
							bankResponse.getWrappedMessage() != null)
						{
							BankStatementResponse statement =
								bankResponse.getWrappedMessage();
							BankAccountDetails bankAccount =
								statement.getAccount();
							String accountNumber =
								bankAccount.getAccountNumber();
							String accountType =
								bankAccount.getAccountType() != null ?
									bankAccount.getAccountType().toString() :
									"BANK";
							String currencyCode = statement.getCurrencyCode();
							TransactionList transactionList =
								statement.getTransactionList();
							
							if (transactionList != null &&
								transactionList.getTransactions() != null)
							{
								
								for (Transaction ofxTransaction : transactionList
									.getTransactions())
								{
									importedTransactions.add(
										mapToImportedTransaction(ofxTransaction,
											accountNumber, accountType,
											currencyCode));
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
			ResponseMessageSet creditMessageSet =
				envelope.getMessageSet(MessageSetType.creditcard);
			
			if (creditMessageSet != null)
			{
				List<ResponseMessage> creditResponses =
					creditMessageSet.getResponseMessages();
				
				if (creditResponses != null)
				{
					
					for (ResponseMessage responseMessage : creditResponses)
					{
						CreditCardStatementResponseTransaction ccResponse =
							(CreditCardStatementResponseTransaction) responseMessage;
						
						// Using getMessage() as suggested by OFX4J patterns for
						// TransactionWrapper
						if (ccResponse != null &&
							ccResponse.getMessage() != null)
						{
							CreditCardStatementResponse statement =
								ccResponse.getMessage();
							CreditCardAccountDetails ccAccount =
								statement.getAccount();
							String accountNumber = ccAccount.getAccountNumber();
							String accountType = "CREDITCARD";
							String currencyCode = statement.getCurrencyCode();
							TransactionList transactionList =
								statement.getTransactionList();
							
							if (transactionList != null &&
								transactionList.getTransactions() != null)
							{
								
								for (Transaction ofxTransaction : transactionList
									.getTransactions())
								{
									importedTransactions.add(
										mapToImportedTransaction(ofxTransaction,
											accountNumber, accountType,
											currencyCode));
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		return importedTransactions;
		
	}
	
	private static String sanitizeOfxPayload(InputStream rawInputStream)
		throws IOException
	{
		String ofxContent =
			new String(rawInputStream.readAllBytes(), StandardCharsets.UTF_8);
		
		Matcher malformedMatcher =
			MALFORMED_COMMENT_PATTERN.matcher(ofxContent);
		StringBuffer sanitizedBuffer = new StringBuffer();
		
		while (malformedMatcher.find())
		{
			malformedMatcher.appendReplacement(sanitizedBuffer, "");
		}
		
		malformedMatcher.appendTail(sanitizedBuffer);
		
		return sanitizedBuffer.toString();
		
	}
	
	private static void validateOfxStructure(String sanitizedOfx)
		throws OFXParseException
	{
		String normalized = sanitizedOfx.toUpperCase();
		
		if (normalized.contains("<ACCTTYPE>") &&
			!normalized.contains("</ACCTTYPE>"))
		{
			throw new OFXParseException(
				"Malformed OFX content: missing closing </ACCTTYPE> tag.");
		}
		
		if (normalized.contains("<TRNAMT>") &&
			!normalized.contains("</TRNAMT>"))
		{
			throw new OFXParseException(
				"Malformed OFX content: missing closing </TRNAMT> tag.");
		}
		
	}
	
	private static final Pattern MALFORMED_COMMENT_PATTERN =
		Pattern.compile("<!-(?!-)(.*?)->", Pattern.DOTALL);
	
	/**
	 * Maps an OFX4J {@link Transaction} object to this application's {@link ImportedTransaction} format.
	 *
	 * @param ofxTransaction The source OFX transaction object from ofx4j library.
	 * @param accountNumber The account number associated with this transaction in the OFX file.
	 * @param accountType The type of account (e.g., "BANK", "CREDITCARD") from the OFX file.
	 * @param currencyCode The default currency code from the OFX statement, used if transaction has no specific currency.
	 * @return A new {@link ImportedTransaction} object populated with data from the OFX transaction.
	 */
	private static ImportedTransaction mapToImportedTransaction(
		Transaction ofxTransaction,
		String accountNumber,
		String accountType,
		String currencyCode)
	{
		ImportedTransaction importedTx = new ImportedTransaction();
		Date datePosted = ofxTransaction.getDatePosted();
		
		if (datePosted != null)
		{
			importedTx
				.setDatePosted(datePosted.toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDate());
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
	
	/**
	 * Parses a QIF (Quicken Interchange Format) file from an input stream.
	 * It processes standard QIF fields (Date, Amount, Payee/Description, Memo, Check Number, Category/Transfer)
	 * and converts them into a list of {@link ImportedTransaction} objects.
	 * The method attempts to determine the account type from the QIF header (e.g., "!Type:Bank")
	 * or uses the provided {@code targetAccountTypeHint}.
	 *
	 * @param inputStream The input stream of the QIF file.
	 * @param targetAccountTypeHint A hint for the account type (e.g., "BANK", "CREDITCARD") if not specified
	 *                              or ambiguously specified in the QIF header.
	 * @return A list of {@link ImportedTransaction} objects parsed from the QIF data.
	 * @throws IOException If an I/O error occurs while reading the stream.
	 */
	static List<ImportedTransaction> parseQif(InputStream inputStream,
		String targetAccountTypeHint) throws IOException
	{
		List<ImportedTransaction> importedTransactions = new ArrayList<>();
		ImportedTransaction currentTx = null;
		String line;
		String qifAccountType = targetAccountTypeHint;
		
		try (BufferedReader reader =
			new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
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
						LOGGER.warn(
							"Unsupported QIF account type in header: {}. Using hint: {}",
							typeHeader,
							targetAccountTypeHint);
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
							LOGGER.warn("Could not parse QIF date '{}': {}",
								value,
								e.getMessage());
						}
						break;
					
					case 'T':
						try
						{
							currentTx.setAmount(
								new BigDecimal(value.replace(",", "")));
						}
						catch (NumberFormatException e)
						{
							LOGGER.warn("Could not parse QIF amount '{}': {}",
								value,
								e.getMessage());
						}
						break;
					
					case 'P':
						currentTx.setDescription(value);
						break;
					
					case 'M':
						currentTx.setMemo(
							(currentTx.getMemo() == null ? "" :
								currentTx.getMemo() + " ") + value);
						break;
					
					case 'N':
						currentTx.setTransactionId(value);
						break;
					
					case 'L':
						currentTx.setMemo((currentTx.getMemo() == null ? "" :
							currentTx.getMemo() + " [Category/Transfer: " +
								value + "]"));
						break;
					
					case '^':
						if (isValidTransaction(currentTx))
							importedTransactions.add(currentTx);
						else
							LOGGER.warn(
								"Skipping incomplete QIF transaction: {}",
								currentTx);
						currentTx = null;
						break;
					
					default:
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
	
	/** Date formatter for QIF dates like "M/d/yy", defaulting to current year if year is ambiguous (e.g. "1/15/98" vs "1/15/20"). */
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MDY_YY =
		new DateTimeFormatterBuilder().appendPattern("M/d/yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()) // Sensible
																					// default
																					// for
																					// yy
			.toFormatter();
	/** Date formatter for QIF dates like "M/d''yy" (e.g., "1/15''98"). */
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MD_YY_APOSTROPHE =
		new DateTimeFormatterBuilder().appendPattern("M/d''yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()) // Sensible
																					// default
																					// for
																					// yy
			.toFormatter();
	/** Date formatter for QIF dates like "M/d/yyyy". */
	private static final DateTimeFormatter QIF_DATE_FORMATTER_MDYYYY =
		DateTimeFormatter.ofPattern("M/d/yyyy");
	/** Date formatter for QIF dates like "d/M/yy". */
	private static final DateTimeFormatter QIF_DATE_FORMATTER_DMY_YY =
		new DateTimeFormatterBuilder().appendPattern("d/M/yy")
			.parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()) // Sensible
																					// default
																					// for
																					// yy
			.toFormatter();
	/** Date formatter for QIF dates like "d/M/yyyy". */
	private static final DateTimeFormatter QIF_DATE_FORMATTER_DMYYYY =
		DateTimeFormatter.ofPattern("d/M/yyyy");
	
	/**
	 * Parses a date string commonly found in QIF files into a {@link LocalDate}.
	 * It tries multiple common QIF date formats (M/d/yyyy, M/d/yy, M/d''yy, d/M/yyyy, d/M/yy).
	 * If all known formats fail, it attempts a direct parse using {@link LocalDate#parse(CharSequence)},
	 * which expects ISO-8601 format by default.
	 *
	 * @param dateStr The date string to parse.
	 * @return The parsed {@link LocalDate}.
	 * @throws DateTimeParseException if the date string cannot be parsed by any of the attempted formats.
	 */
	private static LocalDate parseQifDate(String dateStr)
		throws DateTimeParseException
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
			return LocalDate.parse(dateStr,
				QIF_DATE_FORMATTER_MD_YY_APOSTROPHE);
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
		
		// Fallback to default ISO parse if other formats fail
		return LocalDate.parse(dateStr);
		
	}
	
	/**
	 * Validates if an {@link ImportedTransaction} has the minimum required data (date and amount)
	 * to be considered a valid transaction for further processing.
	 *
	 * @param tx The {@link ImportedTransaction} to validate.
	 * @return {@code true} if the transaction has both date posted and amount set, {@code false} otherwise.
	 */
	private static boolean isValidTransaction(ImportedTransaction tx)
	{
		return tx != null && tx.getDatePosted() != null &&
			tx.getAmount() != null;
		
	}
	
	/**
	 * Maps a list of {@link ImportedTransaction} objects to a list of {@link AccountingTransaction} objects.
	 * This process involves:
	 * 1. Filtering out potential duplicate transactions by comparing against an existing ledger.
	 * 2. For non-duplicate, valid transactions:
	 *    a. Determining booking date, memo, and informational details.
	 *    b. Creating balanced accounting entries: one for the {@code targetAccount} and one for the
	 *       "needs categorization" suspense account (identified by {@link #NEEDS_CATEGORIZATION_ACCOUNT_NUMBER}).
	 *    c. Constructing new {@link AccountingTransaction} objects.
	 *
	 * @param importedTxns The list of {@link ImportedTransaction}s to map.
	 * @param targetAccount The primary {@link Account} to which these transactions should be posted.
	 * @param chartOfAccounts The {@link ChartOfAccounts} used to find the "needs categorization" account.
	 * @param existingLedger The {@link Ledger} containing existing transactions, used for duplicate detection.
	 * @return A list of newly created {@link AccountingTransaction}s.
	 * @throws IllegalArgumentException if the "needs categorization" account is not found in the chart of accounts.
	 */
	static
		List<AccountingTransaction>
		mapToAccountingTransactions(List<ImportedTransaction> importedTxns,
			Account targetAccount, ChartOfAccounts chartOfAccounts,
			Ledger existingLedger)
	{		
		List<AccountingTransaction> accountingTransactions = new ArrayList<>();
		Account needsCategorizationAccount =
			chartOfAccounts.getAccount(NEEDS_CATEGORIZATION_ACCOUNT_NUMBER);
		
		if (needsCategorizationAccount == null)
		{
			LOGGER.error(
				"Critical: 'Needs Categorization' account ('{}') not found. Cannot map transactions.",
				NEEDS_CATEGORIZATION_ACCOUNT_NUMBER);
			throw new IllegalArgumentException(
				"'Needs Categorization' account not found.");
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
			LOGGER.info("Skipped {} potential duplicate transactions.",
				skippedDuplicateTxns.size());
		}
		
		for (ImportedTransaction impTxn : nonDuplicateImportedTxns)
		{
			
			if (impTxn.getDatePosted() == null || impTxn.getAmount() == null)
			{
				LOGGER.warn(
					"Skipping imported transaction with missing date or amount: {}",
					impTxn);
				continue;
			}
			
			long bookingDateTimestamp =
				impTxn.getDatePosted().atStartOfDay(ZoneOffset.UTC).toInstant()
					.toEpochMilli();
			String memo =
				impTxn.getDescription() != null ? impTxn.getDescription() :
					impTxn.getMemo();
			if (memo == null || memo.trim().isEmpty())
				memo = "Imported Transaction";
			
			Map<String, String> info = new HashMap<>();
			
			if (impTxn.getTransactionId() != null &&
				!impTxn.getTransactionId().isEmpty())
			{
				info.put(FITID_KEY, impTxn.getTransactionId());
			}
			
			if (impTxn.getOriginalAccountNumber() != null)
				info.put("ORIGINAL_ACCOUNT_NUMBER",
					impTxn.getOriginalAccountNumber());
			if (impTxn.getOriginalAccountType() != null)
				info.put("ORIGINAL_ACCOUNT_TYPE",
					impTxn.getOriginalAccountType());
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
			
			entries.add(new AccountingEntry(entryAmount,
				targetAccount.getAccountNumber(),
				targetAccountSide, targetAccount.getName()));
			entries
				.add(new AccountingEntry(entryAmount,
					needsCategorizationAccount.getAccountNumber(),
					needsCatAccountSide, needsCategorizationAccount.getName()));
			
			AccountingTransaction newAt =
				new AccountingTransaction(targetAccount, entries, info,
					bookingDateTimestamp);
			newAt.setMemo(memo);
			accountingTransactions.add(newAt);
		}
		
		return accountingTransactions;
		
	}
	
	/**
	 * Checks if an imported transaction is a potential duplicate of an existing transaction in the ledger.
	 * This method uses two main strategies for duplicate detection:
	 * <ol>
	 *   <li><b>OFX FITID Check:</b> If the imported transaction has a non-empty transaction ID (FITID)
	 *       and its original account type is "BANK" or "CREDITCARD", it checks if any existing
	 *       transaction in the ledger has the same FITID stored in its info map (under the key {@link #FITID_KEY}).
	 *       This is considered a strong match for OFX imports.</li>
	 *   <li><b>QIF/Fallback Heuristic Check:</b> If the FITID check doesn't find a duplicate (or isn't applicable),
	 *       this heuristic compares the imported transaction's date, amount (net effect on target account),
	 *       and description/memo against existing transactions associated with the {@code targetAccount}.
	 *       This is a more complex comparison involving checking entry details.</li>
	 * </ol>
	 *
	 * @param impTxn The {@link ImportedTransaction} to check for duplication.
	 * @param targetAccount The {@link Account} against which the transaction is being imported.
	 *                      This is used to find relevant existing transactions and determine amount signs.
	 * @param existingLedger The {@link Ledger} containing existing transactions to check against.
	 * @return {@code true} if the imported transaction is deemed a potential duplicate, {@code false} otherwise.
	 */
	private static boolean isPotentialDuplicate(ImportedTransaction impTxn,
		Account targetAccount,
		Ledger existingLedger)
	{
		
		if (existingLedger == null || existingLedger.getTransactions() == null)
		{
			return false; // No existing transactions to compare against.
		}
		
		// OFX Duplicate Check (Primary using FITID)
		if (impTxn.getTransactionId() != null &&
			!impTxn.getTransactionId().isEmpty() &&
			impTxn.getOriginalAccountType() != null && // Ensure
														// originalAccountType
														// is not null
			(impTxn.getOriginalAccountType().equals("BANK") ||
				impTxn.getOriginalAccountType().equals("CREDITCARD")))
		{ // FITID is strong for OFX
			
			for (AccountingTransaction existingTx : existingLedger
				.getTransactions())
			{
				Map<String, String> info = existingTx.getInfo();
				
				if (info != null &&
					impTxn.getTransactionId().equals(info.get(FITID_KEY)))
				{
					LOGGER.info(
						"Potential OFX duplicate (FITID match): Imported FITID={}, Existing Tx BookingDate={}",
						impTxn.getTransactionId(),
						Instant.ofEpochMilli(
							existingTx.getBookingDateTimestamp()));
					return true; // Found duplicate by FITID
				}
				
			}
			
		}
		
		// QIF/Fallback Heuristic Duplicate Check
		// Exact match on Date, net Amount on targetAccount, and
		// Payee/Description.
		if (impTxn.getDatePosted() == null || impTxn.getAmount() == null)
		{
			return false; // Cannot perform heuristic check without date or
							// amount
		}
		
		for (AccountingTransaction existingTx : existingLedger
			.getTransactions())
		{
			boolean targetAccountMatchInExistingTx = false;
			BigDecimal existingTxAmountForTarget = BigDecimal.ZERO;
			
			if (existingTx.getEntries() != null)
			{
				
				for (AccountingEntry entry : existingTx.getEntries())
				{
					
					if (entry.getAccountNumber()
						.equals(targetAccount.getAccountNumber()))
					{
						targetAccountMatchInExistingTx = true;
						
						// Calculate net effect on target account based on entry
						// side relative to
						// account's normal increase side
						if (targetAccount.getIncreaseSide() ==
							entry.getAccountSide())
						{ // Matches normal increase (e.g., Debit for Asset,
							// Credit for Liability)
							existingTxAmountForTarget =
								existingTxAmountForTarget
									.add(entry.getAmount().abs());
						}
						else
						{ // Opposite of normal increase
							existingTxAmountForTarget =
								existingTxAmountForTarget
									.subtract(entry.getAmount().abs());
						}
						
					}
					
				}
				
			}
			
			if (!targetAccountMatchInExistingTx)
			{
				continue; // Skip if existing TX doesn't involve the target
							// account
			}
			
			LocalDate existingTxDate =
				Instant.ofEpochMilli(existingTx.getBookingDateTimestamp())
					.atZone(ZoneId.systemDefault()).toLocalDate();
			
			// Compare date
			if (!impTxn.getDatePosted().equals(existingTxDate))
			{
				continue;
			}
			
			// Compare amount:
			// For bank accounts (DEBIT normal balance): positive imported
			// amount is DEBIT,
			// negative is CREDIT.
			// For credit cards (CREDIT normal balance): positive imported
			// amount (payment)
			// is DEBIT, negative (charge) is CREDIT.
			// The existingTxAmountForTarget represents the net change.
			// We need to align the sign of impTxn.getAmount() with how it would
			// affect the
			// target account's balance.
			BigDecimal comparableImportedAmount = impTxn.getAmount();
			
			if (targetAccount.getAccountType() == AccountType.CREDITCARD ||
				targetAccount.getAccountType() == AccountType.LIABILITY)
			{
				// For credit-normal accounts, OFX often has charges as
				// negative.
				// If our existingTxAmountForTarget reflects increase in
				// liability as positive,
				// then flip sign of imported.
				// This logic is complex and depends on how amounts are signed
				// in QIF/OFX vs.
				// internal representation.
				// A simpler heuristic for now: compare impTxn.getAmount()
				// directly with
				// existingTxAmountForTarget,
				// ASSUMING that existingTxAmountForTarget correctly reflects
				// the signed impact
				// on the account.
			}
			
			if (comparableImportedAmount.compareTo(existingTxAmountForTarget) !=
				0)
			{
				continue;
			}
			
			// Compare description (payee/memo)
			String impDescription =
				(impTxn.getDescription() != null ? impTxn.getDescription() : "")
					.trim();
			String existingTxMemo =
				(existingTx.getMemo() != null ? existingTx.getMemo() : "")
					.trim();
			
			// Simple direct comparison for V1 heuristic. Could be more
			// sophisticated (fuzzy
			// match, check other entry descriptions).
			if (!impDescription.equalsIgnoreCase(existingTxMemo)) // Using
																	// equalsIgnoreCase
																	// for more
																	// lenient
																	// matching
			{
				// Fallback: check if imported description matches the "other
				// side" entry's memo
				// in existing transaction
				boolean otherSideMatch = false;
				
				if (existingTx.getEntries() != null)
				{
					
					for (AccountingEntry entry : existingTx.getEntries())
					{
						
						if (!entry.getAccountNumber()
							.equals(targetAccount.getAccountNumber()))
						{ // The "other" leg
							String otherEntryMemo =
								entry.getTransaction() != null &&
									entry.getTransaction().getMemo() != null ?
										entry.getTransaction().getMemo()
											.trim() :
										"";
							
							if (impDescription.equalsIgnoreCase(otherEntryMemo))
							{
								otherSideMatch = true;
								break;
							}
							
						}
						
					}
					
				}
				
				if (!otherSideMatch)
					continue;
			}
			
			// If all heuristic checks pass
			LOGGER.info(
				"Potential heuristic duplicate found: Imported={}|{}|{} vs Existing={}|{}|{}",
				impTxn.getDatePosted(),
				impTxn.getAmount(),
				impDescription,
				existingTxDate,
				existingTxAmountForTarget,
				existingTxMemo);
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * Imports transactions from a specified file (OFX or QIF) and maps them to accounting transactions.
	 * The method determines the file type based on its extension, parses it,
	 * checks for potential duplicates against the existing ledger, and then maps valid,
	 * non-duplicate imported transactions to {@link AccountingTransaction} objects.
	 * These transactions are typically balanced against a "needs categorization" suspense account.
	 *
	 * @param file The financial data file to import (OFX or QIF). Must not be null and must exist.
	 * @param targetAccountInCOA The {@link Account} in the Chart of Accounts to which these transactions primarily relate. Must not be null.
	 * @param chartOfAccounts The complete {@link ChartOfAccounts}, used to find the suspense account. Must not be null.
	 * @param ledger The current {@link Ledger} containing existing transactions, used for duplicate detection. Must not be null.
	 * @return A list of {@link AccountingTransaction}s generated from the imported file.
	 *         Returns an empty list if the file is invalid, unsupported, cannot be parsed,
	 *         or if critical setup (like the suspense account) is missing, or if all transactions are duplicates or invalid.
	 */
	public static List<AccountingTransaction> importOFXorQIFFile(File file,
		Account targetAccountInCOA,
		ChartOfAccounts chartOfAccounts,
		Ledger ledger)
	{
		
		if (file == null || !file.exists())
		{
			LOGGER.warn("Import file is null or does not exist. Path: {}",
				(file != null ? file.getAbsolutePath() : "null"));
			return Collections.emptyList();
		}
		
		if (targetAccountInCOA == null)
		{
			LOGGER.warn("Target account for import is null.");
			return Collections.emptyList();
		}
		
		if (chartOfAccounts == null)
		{
			LOGGER.warn(
				"Chart of Accounts is null. Cannot process import mapping.");
			return Collections.emptyList();
		}
		
		if (ledger == null)
		{
			LOGGER.warn(
				"Ledger is null. Cannot check for duplicates or save transactions.");
			return Collections.emptyList();
		}
		
		String fileName = file.getName().toLowerCase();
		List<ImportedTransaction> importedTxns = new ArrayList<>();
		
		try (FileInputStream fis = new FileInputStream(file))
		{
			
			if (fileName.endsWith(".ofx") || fileName.endsWith(".qfx"))
			{
				importedTxns = parseOfx(fis);
				LOGGER.info(
					"Successfully parsed {} transactions from OFX file: {}",
					importedTxns.size(),
					file.getAbsolutePath());
			}
			else if (fileName.endsWith(".qif"))
			{
				String accountTypeHint =
					targetAccountInCOA.getAccountType() != null ?
						targetAccountInCOA.getAccountType().toUpperCase() :
						"BANK";
				
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
				LOGGER.info(
					"Successfully parsed {} transactions from QIF file: {}",
					importedTxns.size(),
					file.getAbsolutePath());
			}
			else
			{
				LOGGER.warn("Unsupported file type for import: {}",
					file.getName());
				return Collections.emptyList();
			}
			
		}
		catch (OFXParseException e)
		{
			LOGGER.error("Failed to parse OFX file: {}", file.getAbsolutePath(),
				e);
			return Collections.emptyList();
		}
		catch (IOException e)
		{
			LOGGER.error("Error reading or parsing import file: {}",
				file.getAbsolutePath(), e);
			return Collections.emptyList();
		}
		
		if (importedTxns.isEmpty())
		{
			return Collections.emptyList();
		}
		
		try
		{
			// Pass existingLedger to mapToAccountingTransactions
			return mapToAccountingTransactions(importedTxns, targetAccountInCOA,
				chartOfAccounts,
				ledger);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.error("Error mapping transactions: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
		
	}
	
}
