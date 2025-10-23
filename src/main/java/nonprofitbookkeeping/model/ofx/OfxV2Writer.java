
package nonprofitbookkeeping.model.ofx;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides utility methods to write OFX (Open Financial Exchange) version 2.0 compliant XML files.
 * This class uses StAX (Streaming API for XML) to generate the OFX content, allowing for
 * efficient writing of large OFX files. It supports writing sign-on information,
 * banking transactions, credit card transactions, and investment account details.
 */
public class OfxV2Writer
{
	
        private static final DateTimeFormatter OFX_DATE_TIME =
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'.000'"); // Standard OFX DateTime format
        private static final DateTimeFormatter OFX_DATE =
                DateTimeFormatter.ofPattern("yyyyMMdd");
	
	
	/**
	 * Writes a complete OFX 2.0 file, potentially including sign-on, banking, credit card, and investment sections.
	 *
	 * @param filePath The absolute path where the OFX file will be created or overwritten.
	 * @param signon The {@link SignonData} containing sign-on message details.
	 * @param bankTxns A list of {@link TransactionData} for bank account transactions. Can be null or empty if no banking section is needed.
	 * @param bankInfo The {@link BankAccountInfo} for the bank account section. Required if {@code bankTxns} is provided.
	 * @param bankLedgerBalance The ledger balance for the bank account. Required if {@code bankTxns} is provided.
	 * @param bankLedgerAsOf The 'as of' date string for the bank ledger balance. Required if {@code bankTxns} is provided.
	 * @param creditCardTxns A list of {@link TransactionData} for credit card transactions. Can be null or empty.
	 * @param ccInfo The {@link CreditCardInfo} for the credit card section. Required if {@code creditCardTxns} is provided.
	 * @param ccLedgerBalance The ledger balance for the credit card account. Required if {@code creditCardTxns} is provided.
	 * @param ccLedgerAsOf The 'as of' date string for the credit card ledger balance. Required if {@code creditCardTxns} is provided.
	 * @param investment The {@link InvestmentData} for the investment section. Can be null if no investment section is needed.
	 * @throws Exception If any IO error occurs during file writing or XML stream processing.
	 */
	public static void writeOfxFile(	String filePath,
								SignonData signon,
								List<TransactionData> bankTxns,
								BankAccountInfo bankInfo,
								BigDecimal bankLedgerBalance,
								String bankLedgerAsOf,
								List<TransactionData> creditCardTxns,
								CreditCardInfo ccInfo,
								BigDecimal ccLedgerBalance,
								String ccLedgerAsOf,
								InvestmentData investment) throws Exception
	{
		
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
			new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
		
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeProcessingInstruction("OFX",
			"OFXHEADER=\"200\" VERSION=\"202\" SECURITY=\"NONE\" OLDFILEUID=\"NONE\" NEWFILEUID=\"NONE\"");
		
		writer.writeStartElement("OFX");
		
		writeSignonSection(writer, signon);
		writeBankingSection(writer, bankTxns, bankInfo, bankLedgerBalance, bankLedgerAsOf);
		writeCreditCardSection(writer, creditCardTxns, ccInfo, ccLedgerBalance, ccLedgerAsOf);
		writeInvestmentSection(writer, investment);
		
		writer.writeEndElement(); // </OFX>
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}
	
	
	/**
	 * Writes the {@code <SIGNONMSGSRSV1>} (Sign-On Messages Response Version 1) section of the OFX file.
	 * This section contains status of the sign-on request, server date/time, user language, and financial institution details.
	 *
	 * @param writer The {@link XMLStreamWriter} to use for writing XML.
	 * @param signon The {@link SignonData} object containing the information for the sign-on section.
	 * @throws Exception If an error occurs during XML writing.
	 */
	private static void writeSignonSection(XMLStreamWriter writer, 
	                                       SignonData signon) throws Exception
	{
		writer.writeStartElement("SIGNONMSGSRSV1");
		writer.writeStartElement("SONRS"); // Sign-on Response
		
		writer.writeStartElement("STATUS");
		writer.writeStartElement("CODE");
		writer.writeCharacters(signon.statusCode);
		writer.writeEndElement();
		writer.writeStartElement("SEVERITY");
		writer.writeCharacters(signon.severity);
		writer.writeEndElement();
		writer.writeStartElement("MESSAGE");
		writer.writeCharacters(signon.message);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("DTSERVER");
		writer.writeCharacters(
			signon.dtServer != null ? signon.dtServer : OFX_DATE_TIME.format(LocalDateTime.now()));
		writer.writeEndElement();
		
		writer.writeStartElement("LANGUAGE");
		writer.writeCharacters(signon.language);
		writer.writeEndElement();
		
		writer.writeStartElement("FI");
		writer.writeStartElement("ORG");
		writer.writeCharacters(signon.org);
		writer.writeEndElement();
		writer.writeStartElement("FID");
		writer.writeCharacters(signon.fid);
		writer.writeEndElement();
		writer.writeEndElement(); // </FI>
		
		writer.writeEndElement(); // </SONRS>
		writer.writeEndElement(); // </SIGNONMSGSRSV1>
	}
	
	
	/**
	 * Writes the {@code <BANKMSGSRSV1>} (Bank Messages Response Version 1) section of the OFX file.
	 * This includes statement transaction responses, account information, transaction lists, and ledger balances for bank accounts.
	 *
	 * @param writer The {@link XMLStreamWriter} to use for writing XML.
	 * @param txns A list of {@link TransactionData} representing bank transactions. Can be null or empty.
	 * @param acctInfo The {@link BankAccountInfo} containing details of the bank account.
	 * @param ledgerBalance The ledger balance of the bank account.
	 * @param asOfDate The date string (OFX format) as of which the ledger balance is reported.
	 * @throws Exception If an error occurs during XML writing.
	 */
	private static void writeBankingSection(XMLStreamWriter writer, 
	                                        List<TransactionData> txns,
	                                        BankAccountInfo acctInfo, 
	                                        BigDecimal ledgerBalance,
	                                        String asOfDate) throws Exception
	{
		// Do not write section if no transactions and no account info provided (or handle as per specific OFX rules)
        if ((txns == null || txns.isEmpty()) && acctInfo == null) {
            return;
        }

		writer.writeStartElement("BANKMSGSRSV1");
		writer.writeStartElement("STMTTRNRS"); // Statement Transaction Response
		
		writer.writeStartElement("TRNUID");
		writer.writeCharacters(acctInfo.trnUid);
		writer.writeEndElement();
		
		writer.writeStartElement("STATUS");
		writer.writeStartElement("CODE");
		writer.writeCharacters(acctInfo.statusCode);
		writer.writeEndElement();
		writer.writeStartElement("SEVERITY");
		writer.writeCharacters(acctInfo.severity);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("STMTRS");
		writer.writeStartElement("CURDEF");
		writer.writeCharacters(acctInfo.currency);
		writer.writeEndElement();
		
		writer.writeStartElement("BANKACCTFROM");
		writer.writeStartElement("BANKID");
		writer.writeCharacters(acctInfo.bankId);
		writer.writeEndElement();
		writer.writeStartElement("ACCTID");
		writer.writeCharacters(acctInfo.accountId);
		writer.writeEndElement();
		writer.writeStartElement("ACCTTYPE");
		writer.writeCharacters(acctInfo.accountType);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("BANKTRANLIST");
		writer.writeStartElement("DTSTART");
		writer.writeCharacters(acctInfo.startDate);
		writer.writeEndElement();
		writer.writeStartElement("DTEND");
		writer.writeCharacters(acctInfo.endDate);
		writer.writeEndElement();
		
		int fitId = 1;
		
		for (TransactionData txn : txns)
		{
			writer.writeStartElement("STMTTRN");
			writer.writeStartElement("TRNTYPE");
			writer.writeCharacters(txn.type);
			writer.writeEndElement();
			writer.writeStartElement("DTPOSTED");
			writer.writeCharacters(txn.date + "T000000.000");
			writer.writeEndElement();
			writer.writeStartElement("TRNAMT");
			writer.writeCharacters(txn.amount);
			writer.writeEndElement();
			writer.writeStartElement("FITID");
			writer.writeCharacters("BNK" + txn.date + "-" + (fitId++));
			writer.writeEndElement();
			
			if (txn.checkNum != null)
			{
				writer.writeStartElement("CHECKNUM");
				writer.writeCharacters(txn.checkNum);
				writer.writeEndElement();
			}
			
			writer.writeStartElement("NAME");
			writer.writeCharacters(txn.name);
			writer.writeEndElement();
			writer.writeStartElement("MEMO");
			writer.writeCharacters(txn.memo);
			writer.writeEndElement();
			writer.writeEndElement(); // </STMTTRN>
		}
		
		writer.writeEndElement(); // </BANKTRANLIST>
		
		writer.writeStartElement("LEDGERBAL");
		writer.writeStartElement("BALAMT");
		writer.writeCharacters(ledgerBalance.toPlainString());
		writer.writeEndElement();
		writer.writeStartElement("DTASOF");
		writer.writeCharacters(asOfDate);
		writer.writeEndElement();
		writer.writeEndElement(); // </LEDGERBAL>
		
		writer.writeEndElement(); // </STMTRS>
		writer.writeEndElement(); // </STMTTRNRS>
		writer.writeEndElement(); // </BANKMSGSRSV1>
	}
	
	
	/**
	 * Writes the {@code <CREDITCARDMSGSRSV1>} (Credit Card Messages Response Version 1) section.
	 * This includes statement transaction responses for credit card accounts.
	 *
	 * @param writer The {@link XMLStreamWriter} to use for writing XML.
	 * @param txns A list of {@link TransactionData} representing credit card transactions. Can be null or empty.
	 * @param ccInfo The {@link CreditCardInfo} containing details of the credit card account.
	 * @param ledgerBalance The ledger balance of the credit card account.
	 * @param asOfDate The date string (OFX format) as of which the ledger balance is reported.
	 * @throws Exception If an error occurs during XML writing.
	 */
	private static void writeCreditCardSection(XMLStreamWriter writer, 
	                                           List<TransactionData> txns,
	                                           CreditCardInfo ccInfo, 
	                                           BigDecimal ledgerBalance,
	                                           String asOfDate) throws Exception
	{
		// Do not write section if no transactions and no account info provided
        if ((txns == null || txns.isEmpty()) && ccInfo == null) {
            return;
        }

		writer.writeStartElement("CREDITCARDMSGSRSV1");
		writer.writeStartElement("CCSTMTTRNRS"); // Credit Card Statement Transaction Response
		writer.writeStartElement("TRNUID");
		writer.writeCharacters(ccInfo.trnUid); // Transaction UID
		writer.writeEndElement();
		
		writer.writeStartElement("STATUS");
		writer.writeStartElement("CODE");
		writer.writeCharacters(ccInfo.statusCode);
		writer.writeEndElement();
		writer.writeStartElement("SEVERITY");
		writer.writeCharacters(ccInfo.severity);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("CCSTMTRS");
		writer.writeStartElement("CURDEF");
		writer.writeCharacters(ccInfo.currency);
		writer.writeEndElement();
		
		writer.writeStartElement("CCACCTFROM");
		writer.writeStartElement("ACCTID");
		writer.writeCharacters(ccInfo.accountId);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("BANKTRANLIST");
		writer.writeStartElement("DTSTART");
		writer.writeCharacters(ccInfo.startDate);
		writer.writeEndElement();
		writer.writeStartElement("DTEND");
		writer.writeCharacters(ccInfo.endDate);
		writer.writeEndElement();
		
		int fitId = 1;
		
		for (TransactionData txn : txns)
		{
			writer.writeStartElement("STMTTRN");
			writer.writeStartElement("TRNTYPE");
			writer.writeCharacters(txn.type);
			writer.writeEndElement();
			writer.writeStartElement("DTPOSTED");
			writer.writeCharacters(txn.date + "T000000.000");
			writer.writeEndElement();
			writer.writeStartElement("TRNAMT");
			writer.writeCharacters(txn.amount);
			writer.writeEndElement();
			writer.writeStartElement("FITID");
			writer.writeCharacters("CC" + txn.date + "-" + (fitId++));
			writer.writeEndElement();
			writer.writeStartElement("NAME");
			writer.writeCharacters(txn.name);
			writer.writeEndElement();
			writer.writeStartElement("MEMO");
			writer.writeCharacters(txn.memo);
			writer.writeEndElement();
			writer.writeEndElement(); // </STMTTRN>
		}
		
		writer.writeEndElement(); // </BANKTRANLIST>
		
		writer.writeStartElement("LEDGERBAL");
		writer.writeStartElement("BALAMT");
		writer.writeCharacters(ledgerBalance.toPlainString());
		writer.writeEndElement();
		writer.writeStartElement("DTASOF");
		writer.writeCharacters(asOfDate);
		writer.writeEndElement();
		writer.writeEndElement(); // </LEDGERBAL>
		
		writer.writeEndElement(); // </CCSTMTRS>
		writer.writeEndElement(); // </CCSTMTTRNRS>
		writer.writeEndElement(); // </CREDITCARDMSGSRSV1>
	}
	
	
	/**
	 * Writes the {@code <INVSTMTMSGSRSV1>} (Investment Statement Messages Response Version 1) section.
	 * This includes investment account statements, balances, and potentially positions and transactions (though not detailed in this stub).
	 *
	 * @param writer The {@link XMLStreamWriter} to use for writing XML.
	 * @param inv The {@link InvestmentData} object containing information for the investment section.
	 * @throws Exception If an error occurs during XML writing.
	 */
        private static void writeInvestmentSection(XMLStreamWriter writer, InvestmentData inv) throws Exception
        {
                if (inv == null)
                {
                        return;
                }

                writer.writeStartElement("INVSTMTMSGSRSV1");
                writer.writeStartElement("INVSTMTTRNRS");

                writeTextElement(writer, "TRNUID", inv.trnUid);

                writer.writeStartElement("STATUS");
                writeTextElement(writer, "CODE", inv.statusCode);
                writeTextElement(writer, "SEVERITY", inv.severity);
                writer.writeEndElement();

                writer.writeStartElement("INVSTMTRS");
                writeTextElement(writer, "DTASOF", inv.dtAsOf);
                writeTextElement(writer, "CURDEF", inv.currency);

                writer.writeStartElement("INVACCTFROM");
                writeTextElement(writer, "BROKERID", inv.brokerId);
                writeTextElement(writer, "ACCTID", inv.accountId);
                writer.writeEndElement();

                writeInvestmentBalances(writer, inv);
                writeInvestmentPositions(writer, inv);
                writeInvestmentTransactions(writer, inv);

                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
        }

        private static void writeInvestmentBalances(XMLStreamWriter writer, InvestmentData inv) throws Exception
        {
                boolean hasCash = inv.availableCash != null;
                boolean hasMarketValue = inv.marketValue != null;

                if (!hasCash && !hasMarketValue)
                {
                        return;
                }

                writer.writeStartElement("INVBAL");

                if (hasCash)
                {
                        writer.writeStartElement("AVAILCASH");
                        writeAmountElement(writer, "BALAMT", inv.availableCash);
                        writeTextElement(writer, "DTASOF", inv.dtAsOf);
                        writer.writeEndElement();
                }

                if (hasMarketValue)
                {
                        writer.writeStartElement("MKTVAL");
                        writeAmountElement(writer, "BALAMT", inv.marketValue);
                        writeTextElement(writer, "DTASOF", inv.dtAsOf);
                        writer.writeEndElement();
                }

                writer.writeEndElement();
        }

        private static void writeInvestmentPositions(XMLStreamWriter writer, InvestmentData inv) throws Exception
        {
                if (inv.positions == null || inv.positions.isEmpty())
                {
                        return;
                }

                writer.writeStartElement("INVPOSLIST");

                for (InvestmentPosition position : inv.positions)
                {
                        if (position == null)
                        {
                                continue;
                        }

                        String container = hasText(position.positionTypeTag)
                                ? position.positionTypeTag
                                : "POSSTOCK";

                        writer.writeStartElement(container);
                        writer.writeStartElement("INVPOS");

                        writeSecurity(writer, position.security);
                        writeAmountElement(writer, "UNITS", position.units);
                        writeAmountElement(writer, "UNITPRICE", position.unitPrice);
                        writeAmountElement(writer, "MKTVAL", position.marketValue);

                        String priceDate = hasText(position.priceAsOf) ? position.priceAsOf : inv.dtAsOf;
                        writeTextElement(writer, "DTPRICEASOF", priceDate);
                        writeTextElement(writer, "POSITIONTYPE", position.positionType);

                        String held = hasText(position.heldInAccount) ? position.heldInAccount : inv.subAccountType;
                        writeTextElement(writer, "HELDINACCT", hasText(held) ? held : "CASH");

                        writer.writeEndElement();
                        writer.writeEndElement();
                }

                writer.writeEndElement();
        }

        private static void writeInvestmentTransactions(XMLStreamWriter writer, InvestmentData inv) throws Exception
        {
                if (inv.transactions == null || inv.transactions.isEmpty())
                {
                        return;
                }

                writer.writeStartElement("INVTRANLIST");

                String[] range = determineTransactionRange(inv);
                writeTextElement(writer, "DTSTART", range[0]);
                writeTextElement(writer, "DTEND", range[1]);

                String subAccount = hasText(inv.subAccountType) ? inv.subAccountType : "CASH";

                for (InvestmentTransaction tx : inv.transactions)
                {
                        if (tx == null)
                        {
                                continue;
                        }

                        writer.writeStartElement(determineTransactionElement(tx.getTransactionType()));

                        writer.writeStartElement("INVTRAN");
                        writeTextElement(writer, "FITID", tx.getFitid());
                        writeTextElement(writer, "DTTRADE", tradeDateOf(tx));
                        writeTextElement(writer, "MEMO", tx.getMemo());
                        writeTextElement(writer, "NAME", tx.getPayee());
                        writer.writeEndElement();

                        writeSecurity(writer, tx.getSecurityNode());
                        writeAmountElement(writer, "UNITS", tx.getQuantity());
                        writeAmountElement(writer, "UNITPRICE", tx.getPrice());
                        writeAmountElement(writer, "TOTAL", tx.getTotal());

                        BigDecimal fees = tx.getFees();
                        if (fees != null && fees.compareTo(BigDecimal.ZERO) != 0)
                        {
                                writeAmountElement(writer, "FEES", fees);
                        }

                        writeTextElement(writer, "SUBACCTSEC", subAccount);
                        writeTextElement(writer, "SUBACCTFUND", subAccount);

                        writer.writeEndElement();
                }

                writer.writeEndElement();
        }

        private static void writeSecurity(XMLStreamWriter writer, SecurityNode security) throws Exception
        {
                if (security == null)
                {
                        return;
                }

                String isin = security.getISIN();
                String symbol = security.getSymbol();

                if (!hasText(isin) && !hasText(symbol))
                {
                        return;
                }

                writer.writeStartElement("SECID");

                if (hasText(isin))
                {
                        writeTextElement(writer, "UNIQUEID", isin);
                        writeTextElement(writer, "UNIQUEIDTYPE", "ISIN");
                }

                if (hasText(symbol))
                {
                        writeTextElement(writer, "TICKER", symbol);
                }

                writer.writeEndElement();
        }

        private static void writeTextElement(XMLStreamWriter writer, String element, String value) throws Exception
        {
                if (!hasText(value))
                {
                        return;
                }

                writer.writeStartElement(element);
                writer.writeCharacters(value);
                writer.writeEndElement();
        }

        private static void writeAmountElement(XMLStreamWriter writer, String element, BigDecimal value) throws Exception
        {
                if (value == null)
                {
                        return;
                }

                writer.writeStartElement(element);
                writer.writeCharacters(value.stripTrailingZeros().toPlainString());
                writer.writeEndElement();
        }

        private static String[] determineTransactionRange(InvestmentData inv)
        {
                String start = hasText(inv.transactionDateStart) ? inv.transactionDateStart : null;
                String end = hasText(inv.transactionDateEnd) ? inv.transactionDateEnd : null;

                if (start != null && end != null)
                {
                        return new String[] { start, end };
                }

                LocalDate min = null;
                LocalDate max = null;

                for (InvestmentTransaction tx : inv.transactions)
                {
                        LocalDate date = safeLocalDate(tx);

                        if (date == null)
                        {
                                continue;
                        }

                        if (min == null || date.isBefore(min))
                        {
                                min = date;
                        }

                        if (max == null || date.isAfter(max))
                        {
                                max = date;
                        }
                }

                if (start == null)
                {
                        start = min != null ? OFX_DATE.format(min) : inv.dtAsOf;
                }

                if (end == null)
                {
                        end = max != null ? OFX_DATE.format(max) : inv.dtAsOf;
                }

                return new String[] { start, end };
        }

        private static LocalDate safeLocalDate(InvestmentTransaction tx)
        {
                if (tx == null)
                {
                        return null;
                }

                try
                {
                        return tx.getLocalDate();
                }
                catch (Exception ex)
                {
                        return null;
                }
        }

        private static String tradeDateOf(InvestmentTransaction tx)
        {
                LocalDate date = safeLocalDate(tx);
                return date != null ? OFX_DATE.format(date) : null;
        }

        private static String determineTransactionElement(String transactionType)
        {
                if (!hasText(transactionType))
                {
                        return "INVTRAN";
                }

                String normalized = transactionType.trim().toUpperCase(Locale.ROOT);

                return switch (normalized)
                {
                        case "BUY", "BUYMF", "BUYSTOCK" -> "INVBUY";
                        case "SELL", "SELLMF", "SELLSTOCK" -> "INVSELL";
                        default -> "INVTRAN";
                };
        }

        private static boolean hasText(String value)
        {
                return value != null && !value.isBlank();
        }

        /**
         * Inner class to hold data for a single transaction, used by the OFX writer methods.
         */
	public static class TransactionData
	{
		/** Type of transaction (e.g., "CREDIT", "DEBIT", "CHECK"). */
		public String type;
		/** Date of the transaction (OFX format: YYYYMMDD). */
		public String date;
		/** Amount of the transaction as a string. */
		public String amount;
		/** Check number, if applicable. Can be null. */
		public String checkNum;
		/** Name or payee of the transaction. */
		public String name;
		/** Memo or description for the transaction. */
		public String memo;

		/**
		 * Constructs a TransactionData object.
		 * @param type The type of the transaction.
		 * @param date The date of the transaction.
		 * @param amount The amount of the transaction.
		 * @param checkNum The check number (can be null).
		 * @param name The name/payee of the transaction.
		 * @param memo The memo for the transaction.
		 */
		public TransactionData(String type, String date, 
		                       String amount, String checkNum,
		                       String name, String memo)
		{
			this.type = type;
			this.date = date;
			this.amount = amount;
			this.checkNum = checkNum;
			this.name = name;
			this.memo = memo;
		}
		
	}
	
	/**
	 * Inner class to hold data for the OFX sign-on section.
	 */
	public static class SignonData
	{
		/** Language code (e.g., "ENG"). */
		public String language;
		/** Organization name of the Financial Institution. */
		public String org;
		/** Financial Institution ID. */
		public String fid;
		/** Status message from the server. */
		public String message;
		/** Server date and time (OFX format). */
		public String dtServer;
		/** Status code from the server (e.g., "0" for success). */
		public String statusCode;
		/** Severity of the status (e.g., "INFO", "ERROR"). */
		public String severity;
		
	}
	
	/**
	 * Inner class to hold data for bank account information used in OFX sections.
	 */
	public static class BankAccountInfo
	{
		/** Transaction UID for the statement response. */
		public String trnUid;
		/** Bank ID (routing number). */
		public String bankId;
		/** Account ID (account number). */
		public String accountId;
		/** Type of account (e.g., "CHECKING", "SAVINGS"). */
		public String accountType;
		/** Default currency for the account (e.g., "USD"). */
		public String currency;
		/** Start date for the statement period (OFX format: YYYYMMDD). */
		public String startDate;
		/** End date for the statement period (OFX format: YYYYMMDD). */
		public String endDate;
		/** Status code for the statement response. */
		public String statusCode;
		/** Severity of the status for the statement response. */
		public String severity;
		
	}
	
	/**
	 * Inner class to hold data for credit card account information used in OFX sections.
	 */
	public static class CreditCardInfo
	{
		/** Transaction UID for the statement response. */
		public String trnUid;
		/** Account ID (credit card number). */
		public String accountId;
		/** Default currency for the account (e.g., "USD"). */
		public String currency;
		/** Start date for the statement period (OFX format: YYYYMMDD). */
		public String startDate;
		/** End date for the statement period (OFX format: YYYYMMDD). */
		public String endDate;
		/** Status code for the statement response. */
		public String statusCode;
		/** Severity of the status for the statement response. */
		public String severity;
		
	}
	
	/**
	 * Inner class to hold data for investment account information used in OFX sections.
	 */
        public static class InvestmentData
        {
                /** Transaction UID for the statement response. */
                public String trnUid;
                /** Broker ID for the investment account. */
		public String brokerId;
		/** Account ID for the investment account. */
		public String accountId;
		/** Default currency for the account (e.g., "USD"). */
		public String currency;
		/** 'As of' date for the investment statement data (OFX format). */
		public String dtAsOf;
		/** Status code for the statement response. */
                public String statusCode;
                /** Severity of the status for the statement response. */
                public String severity;
                /** Available cash balance in the investment account. */
                public BigDecimal availableCash;
                /** Market value of holdings in the investment account. */
                public BigDecimal marketValue;
                /** Explicit start date for investment transactions, if provided. */
                public String transactionDateStart;
                /** Explicit end date for investment transactions, if provided. */
                public String transactionDateEnd;
                /** Default sub-account classification for securities and cash movements. */
                public String subAccountType = "CASH";
                /** Positions held inside the account. */
                public List<InvestmentPosition> positions = new ArrayList<>();
                /** Investment transactions reported for the account. */
                public List<InvestmentTransaction> transactions = new ArrayList<>();

        }

        /**
         * Describes a single investment position included in an OFX investment
         * statement.
         */
        public static class InvestmentPosition
        {
                /** Security associated with the position. */
                public SecurityNode security;
                /** Units held of the security. */
                public BigDecimal units;
                /** Unit price applied to the security. */
                public BigDecimal unitPrice;
                /** Market value of the position. */
                public BigDecimal marketValue;
                /** Pricing date for the position. */
                public String priceAsOf;
                /** Position type such as LONG or SHORT. */
                public String positionType;
                /** Sub-account in which the position is held. */
                public String heldInAccount = "CASH";
                /** Optional override for the OFX element (e.g., POSSTOCK, POSMF). */
                public String positionTypeTag = "POSSTOCK";
        }

}
