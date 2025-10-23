
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


        private static boolean hasText(String value)
        {
                return value != null && !value.isBlank();
        }


        private static String withDefault(String value, String fallback)
        {
                return hasText(value) ? value : fallback;
        }


        private static void writeElement(XMLStreamWriter writer,
                                         String elementName,
                                         String value) throws Exception
        {
                if (!hasText(value))
                {
                        return;
                }

                writer.writeStartElement(elementName);
                writer.writeCharacters(value);
                writer.writeEndElement();
        }


        private static void writeDecimalElement(XMLStreamWriter writer,
                                                 String elementName,
                                                 BigDecimal value) throws Exception
        {
                if (value == null)
                {
                        return;
                }

                writer.writeStartElement(elementName);
                writer.writeCharacters(formatDecimal(value));
                writer.writeEndElement();
        }


        private static String formatDecimal(BigDecimal value)
        {
                BigDecimal normalized = value.stripTrailingZeros();

                if (normalized.scale() < 0)
                {
                        normalized = normalized.setScale(0);
                }

                return normalized.toPlainString();
        }


        private static void writeSecurityElements(XMLStreamWriter writer,
                                                  SecurityData security) throws Exception
        {
                if (security == null)
                {
                        return;
                }

                if (hasText(security.uniqueId) || hasText(security.uniqueIdType))
                {
                        writer.writeStartElement("SECID");
                        writeElement(writer, "UNIQUEID", security.uniqueId);
                        writeElement(writer, "UNIQUEIDTYPE", withDefault(security.uniqueIdType, "CUSIP"));
                        writer.writeEndElement();
                }

                writeElement(writer, "SECNAME", security.name);
                writeElement(writer, "TICKER", security.ticker);
        }


        private static String resolveDate(String candidate,
                                          String defaultDate)
        {
                return hasText(candidate) ? candidate : defaultDate;
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
                writer.writeStartElement("INVSTMTTRNRS"); // Investment Statement Transaction Response

                writeElement(writer, "TRNUID", withDefault(inv.trnUid, "0"));

                writer.writeStartElement("STATUS");
                writeElement(writer, "CODE", withDefault(inv.statusCode, "0"));
                writeElement(writer, "SEVERITY", withDefault(inv.severity, "INFO"));
                writeElement(writer, "MESSAGE", inv.statusMessage);
                writer.writeEndElement();

                writer.writeStartElement("INVSTMTRS");

                String asOf = withDefault(inv.dtAsOf, OFX_DATE_TIME.format(LocalDateTime.now()));
                writeElement(writer, "DTASOF", asOf);
                writeElement(writer, "CURDEF", withDefault(inv.currency, "USD"));

                writer.writeStartElement("INVACCTFROM");
                writeElement(writer, "BROKERID", withDefault(inv.brokerId, "BROKER"));
                writeElement(writer, "ACCTID", withDefault(inv.accountId, "0000"));
                writer.writeEndElement();

                if (inv.availableCash != null || inv.marketValue != null)
                {
                        writer.writeStartElement("INVBAL");

                        if (inv.availableCash != null)
                        {
                                writer.writeStartElement("AVAILCASH");
                                writeDecimalElement(writer, "BALAMT", inv.availableCash);
                                writeElement(writer, "DTASOF", resolveDate(inv.cashAsOf, asOf));
                                writer.writeEndElement();
                        }

                        if (inv.marketValue != null)
                        {
                                writeDecimalElement(writer, "MKTVAL", inv.marketValue);
                                writeElement(writer, "DTASOF", resolveDate(inv.marketValueAsOf, asOf));
                        }

                        writer.writeEndElement();
                }

                if (!inv.positions.isEmpty())
                {
                        writer.writeStartElement("INVPOSLIST");

                        for (InvestmentPosition pos : inv.positions)
                        {
                                String positionElement = withDefault(pos.positionType, "POSSTOCK");
                                writer.writeStartElement(positionElement);

                                writeSecurityElements(writer, pos.security);

                                writer.writeStartElement("INVPOS");
                                writeElement(writer, "HELDINACCT", pos.heldInAccount);
                                writeElement(writer, "POSTYPE", pos.positionTypeIndicator);
                                writeDecimalElement(writer, "UNITS", pos.units);
                                writeDecimalElement(writer, "UNITPRICE", pos.unitPrice);
                                writeDecimalElement(writer, "MKTVAL", pos.marketValue);
                                writeDecimalElement(writer, "COSTBASIS", pos.costBasis);
                                writeElement(writer, "DTPRICEASOF", resolveDate(pos.priceAsOf, asOf));
                                writeElement(writer, "MEMO", pos.memo);
                                writer.writeEndElement();

                                writer.writeEndElement();
                        }

                        writer.writeEndElement();
                }

                if (!inv.transactions.isEmpty())
                {
                        writer.writeStartElement("INVTRANLIST");
                        writeElement(writer, "DTSTART", resolveDate(inv.transactionStartDate, asOf));
                        writeElement(writer, "DTEND", resolveDate(inv.transactionEndDate, asOf));

                        for (InvestmentTransactionData txn : inv.transactions)
                        {
                                String transactionElement = withDefault(txn.transactionType, "BUYOTHER");
                                writer.writeStartElement(transactionElement);

                                String containerElement = determineInvestmentContainer(transactionElement);
                                if (containerElement != null)
                                {
                                        writer.writeStartElement(containerElement);
                                }

                                writer.writeStartElement("INVTRAN");
                                writeElement(writer, "FITID", txn.fitId);
                                writeElement(writer, "DTTRADE", resolveDate(txn.tradeDate, asOf));
                                writeElement(writer, "DTSETTLE", txn.settleDate);
                                writeElement(writer, "MEMO", txn.memo);
                                writer.writeEndElement();

                                writeSecurityElements(writer, txn.security);
                                writeElement(writer, "SUBACCTSEC", txn.subAccountSecurity);
                                writeElement(writer, "SUBACCTFUND", txn.subAccountFund);
                                writeDecimalElement(writer, "UNITS", txn.units);
                                writeDecimalElement(writer, "UNITPRICE", txn.unitPrice);
                                writeDecimalElement(writer, "TOTAL", txn.total);
                                writeDecimalElement(writer, "COMMISSION", txn.commission);
                                writeDecimalElement(writer, "FEES", txn.fees);
                                writeDecimalElement(writer, "TAXES", txn.taxes);

                                if (containerElement != null)
                                {
                                        writer.writeEndElement();
                                }

                                writer.writeEndElement();
                        }

                        writer.writeEndElement();
                }

                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
        }

        private static String determineInvestmentContainer(String transactionElement)
        {
                if (!hasText(transactionElement))
                {
                        return null;
                }

                String normalized = transactionElement.toUpperCase(Locale.ROOT);

                if (normalized.startsWith("BUY"))
                {
                        return "INVBUY";
                }

                if (normalized.startsWith("SELL"))
                {
                        return "INVSELL";
                }

                return null;
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
                /** Optional human readable status message. */
                public String statusMessage;
                /** Reported cash balance date (defaults to {@link #dtAsOf} when omitted). */
                public String cashAsOf;
                /** Current market value of the investment account. */
                public BigDecimal marketValue;
                /** Date for the reported market value (defaults to {@link #dtAsOf}). */
                public String marketValueAsOf;
                /** Start date for investment transactions. */
                public String transactionStartDate;
                /** End date for investment transactions. */
                public String transactionEndDate;
                /** Detailed investment positions to include in the export. */
                public final List<InvestmentPosition> positions = new ArrayList<>();
                /** Investment transactions to include in the export. */
                public final List<InvestmentTransactionData> transactions = new ArrayList<>();

        }

        /**
         * Describes a single investment holding, such as a stock or mutual fund position.
         */
        public static class InvestmentPosition
        {
                /** The OFX position element to use (e.g., {@code POSSTOCK}). */
                public String positionType;
                /** Security information for the position. */
                public SecurityData security;
                /** Indicates which sub-account the position is held in. */
                public String heldInAccount;
                /** Position side such as LONG or SHORT. */
                public String positionTypeIndicator;
                /** Units currently held. */
                public BigDecimal units;
                /** Price per unit. */
                public BigDecimal unitPrice;
                /** Market value of the position. */
                public BigDecimal marketValue;
                /** Cost basis for the holding. */
                public BigDecimal costBasis;
                /** Price as-of date for the security. */
                public String priceAsOf;
                /** Optional memo for the position. */
                public String memo;
        }

        /**
         * Describes a single investment transaction entry within the statement.
         */
        public static class InvestmentTransactionData
        {
                /** The OFX transaction element to use (e.g., {@code BUYSTOCK}, {@code SELLSTOCK}). */
                public String transactionType;
                /** Unique identifier for the transaction. */
                public String fitId;
                /** Trade date for the transaction. */
                public String tradeDate;
                /** Settlement date for the transaction. */
                public String settleDate;
                /** Memo or description for the transaction. */
                public String memo;
                /** Security information referenced by the transaction. */
                public SecurityData security;
                /** Indicates which security sub-account is affected. */
                public String subAccountSecurity;
                /** Indicates which fund sub-account is affected. */
                public String subAccountFund;
                /** Number of units purchased or sold. */
                public BigDecimal units;
                /** Price per unit for the transaction. */
                public BigDecimal unitPrice;
                /** Total amount of the transaction. */
                public BigDecimal total;
                /** Commission paid for the transaction. */
                public BigDecimal commission;
                /** Additional fees for the transaction. */
                public BigDecimal fees;
                /** Taxes associated with the transaction. */
                public BigDecimal taxes;
        }

        /**
         * Identifies a security referenced by investment transactions and positions.
         */
        public static class SecurityData
        {
                /** Primary unique identifier for the security (CUSIP, ISIN, etc.). */
                public String uniqueId;
                /** Type of the unique identifier (e.g., {@code CUSIP}, {@code ISIN}). */
                public String uniqueIdType;
                /** Display name for the security. */
                public String name;
                /** Optional ticker symbol. */
                public String ticker;
        }

}
