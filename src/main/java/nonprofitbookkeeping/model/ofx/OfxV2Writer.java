
package nonprofitbookkeeping.model.ofx;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OfxV2Writer
{
	
	private static final DateTimeFormatter OFX_DATE_TIME =
		DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'.000'");
	
	
	/**
	 * Writes a complete OFX 2.0 file with banking, credit card, and investment sections.
	 *
	 * @param filePath           Output file path for the OFX file.
	 * @param signon             Signon metadata including FI info and timestamp.
	 * @param bankTxns           List of bank account transactions.
	 * @param bankInfo           Bank account details.
	 * @param bankLedgerBalance  Ledger balance for bank account.
	 * @param bankLedgerAsOf     As-of timestamp for bank ledger balance.
	 * @param creditCardTxns     List of credit card transactions.
	 * @param ccInfo             Credit card account details.
	 * @param ccLedgerBalance    Ledger balance for credit card account.
	 * @param ccLedgerAsOf       As-of timestamp for credit card balance.
	 * @param investment         Investment account data.
	 * @throws Exception If any IO or XML error occurs.
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
	 * Writes the SIGNONMSGSRSV1 section for authentication and server info.
	 *
	 * @param writer XML stream writer.
	 * @param signon Signon details including org, FID, language, and server time.
	 * @throws Exception If XML writing fails.
	 */
	private static void writeSignonSection(XMLStreamWriter writer, 
	                                       SignonData signon) throws Exception
	{
		writer.writeStartElement("SIGNONMSGSRSV1");
		writer.writeStartElement("SONRS");
		
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
	 * Writes the BANKMSGSRSV1 section including bank transactions and ledger balance.
	 *
	 * @param writer        XML stream writer.
	 * @param txns          List of bank transactions.
	 * @param acctInfo      Bank account information.
	 * @param ledgerBalance Ledger balance for bank account.
	 * @param asOfDate      Date of ledger balance.
	 * @throws Exception If XML writing fails.
	 */
	private static void writeBankingSection(XMLStreamWriter writer, 
	                                        List<TransactionData> txns,
	                                        BankAccountInfo acctInfo, 
	                                        BigDecimal ledgerBalance,
	                                        String asOfDate) throws Exception
	{
		writer.writeStartElement("BANKMSGSRSV1");
		writer.writeStartElement("STMTTRNRS");
		
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
	 * Writes the CREDITCARDMSGSRSV1 section including credit card transactions.
	 *
	 * @param writer        XML stream writer.
	 * @param txns          List of credit card transactions.
	 * @param ccInfo        Credit card account information.
	 * @param ledgerBalance Ledger balance of credit card.
	 * @param asOfDate      Date of ledger balance.
	 * @throws Exception If XML writing fails.
	 */
	private static void writeCreditCardSection(XMLStreamWriter writer, 
	                                           List<TransactionData> txns,
	                                           CreditCardInfo ccInfo, 
	                                           BigDecimal ledgerBalance,
	                                           String asOfDate) throws Exception
	{
		writer.writeStartElement("CREDITCARDMSGSRSV1");
		writer.writeStartElement("CCSTMTTRNRS");
		writer.writeStartElement("TRNUID");
		writer.writeCharacters(ccInfo.trnUid);
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
	 * Writes the INVSTMTMSGSRSV1 section containing 
	 * investment account info and cash balance.
	 *
	 * @param writer XML stream writer.
	 * @param inv    Investment account metadata and available cash.
	 * @throws Exception If XML writing fails.
	 */
	private static void writeInvestmentSection(XMLStreamWriter writer, InvestmentData inv) throws Exception
	{
		writer.writeStartElement("INVSTMTMSGSRSV1");
		writer.writeStartElement("INVSTMTTRNRS");
		
		writer.writeStartElement("TRNUID");
		writer.writeCharacters(inv.trnUid);
		writer.writeEndElement();
		
		writer.writeStartElement("STATUS");
		
		writer.writeStartElement("CODE");
		writer.writeCharacters(inv.statusCode);
		writer.writeEndElement();
		
		writer.writeStartElement("SEVERITY");
		writer.writeCharacters(inv.severity);
		writer.writeEndElement();
		
		writer.writeEndElement();
		
		writer.writeStartElement("INVSTMTRS");
		writer.writeStartElement("DTASOF");
		writer.writeCharacters(inv.dtAsOf);
		writer.writeEndElement();
		writer.writeStartElement("CURDEF");
		writer.writeCharacters(inv.currency);
		writer.writeEndElement();
		
		writer.writeStartElement("INVACCTFROM");
		writer.writeStartElement("BROKERID");
		writer.writeCharacters(inv.brokerId);
		writer.writeEndElement();
		writer.writeStartElement("ACCTID");
		writer.writeCharacters(inv.accountId);
		writer.writeEndElement();
		writer.writeEndElement();
		
		writer.writeStartElement("INVBAL");
		writer.writeStartElement("AVAILCASH");
		writer.writeStartElement("BALAMT");
		writer.writeCharacters(inv.availableCash.toPlainString());
		writer.writeEndElement();
		writer.writeStartElement("DTASOF");
		writer.writeCharacters(inv.dtAsOf);
		writer.writeEndElement();
		writer.writeEndElement(); // </AVAILCASH>
		writer.writeEndElement(); // </INVBAL>
		
		writer.writeEndElement(); // </INVSTMTRS>
		writer.writeEndElement(); // </INVSTMTTRNRS>
		writer.writeEndElement(); // </INVSTMTMSGSRSV1>
	}
	
	public static class TransactionData
	{
		public String type, date, amount, checkNum, name, memo;
		
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
	
	public static class SignonData
	{
		public String language, org, fid, message, dtServer, 
			statusCode, severity;
		
	}
	
	public static class BankAccountInfo
	{
		public String trnUid, bankId, 
		accountId, accountType, 
		currency, startDate, endDate,
		statusCode, severity;
		
	}
	
	public static class CreditCardInfo
	{
		public String trnUid, accountId, currency, startDate, 
			endDate, statusCode, severity;
		
	}
	
	public static class InvestmentData
	{
		public String trnUid, brokerId, accountId, currency, 
			dtAsOf, statusCode, severity;
		public BigDecimal availableCash;
		
	}
	
}
