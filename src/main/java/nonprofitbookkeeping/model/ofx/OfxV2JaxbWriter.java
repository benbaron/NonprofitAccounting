
package nonprofitbookkeeping.model.ofx;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Generates OFX 2.0 XML using JAXB-based model classes.
 */
public class OfxV2JaxbWriter
{
	
	/**
	 * Writes an OFX object structure (rooted by {@link OFX}) to a specified file path using JAXB.
	 * The output XML will be formatted for readability.
	 *
	 * @param filePath The absolute path to the file where the OFX XML should be saved.
	 *                 The file will be created or overwritten.
	 * @param ofxRoot The root {@link OFX} object containing the data to be marshalled.
	 * @throws Exception if any error occurs during JAXB context creation, marshalling, or file writing.
	 *                   This can include {@link jakarta.xml.bind.JAXBException}.
	 */
	public static void writeOfxFile(String filePath,
									OFX ofxRoot) throws Exception
	{
		
		JAXBContext context = JAXBContext.newInstance(OFX.class);
		Marshaller marshaller = context.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(ofxRoot, new File(filePath));
	}
	
	/**
	 * Utility method to construct a {@link SignonMsgs} object, representing the OFX sign-on response.
	 * This is typically part of an OFX file header, indicating server status and FI details.
	 *
	 * @param language The language of the response (e.g., "ENG").
	 * @param dtServer The server's date and time string.
	 * @param org The organization name of the financial institution.
	 * @param fid The financial institution ID.
	 * @param code The status code of the sign-on (e.g., "0" for success).
	 * @param severity The severity of the status (e.g., "INFO", "ERROR").
	 * @param message A descriptive message for the status.
	 * @return A populated {@link SignonMsgs} object.
	 */
	public static SignonMsgs buildSignon(	String language, String dtServer, String org, String fid,
											String code, String severity, String message)
	{
		SignonMsgs signonMsgs = new SignonMsgs();
		SignonResponse response = new SignonResponse();
		response.language = language;
		response.dtServer = dtServer;
		
		Status status = new Status();
		status.code = Integer.parseInt(code); // Assumes code is a valid integer string
		status.severity = severity;
		status.message = message;
		response.status = status;
		
		FinancialInstitution fi = new FinancialInstitution();
		fi.org = org;
		fi.fid = fid;
		response.fi = fi;
		
		signonMsgs.sonrs = response;
		return signonMsgs;
	}
	
	/**
	 * Utility method to construct a {@link BankMsgs} object, representing the OFX banking messages section.
	 * This section typically contains account statements and transaction lists.
	 * This method creates a sample structure; specific start/end dates for the transaction list are hardcoded.
	 *
	 * @param transactions A list of {@link Transaction} objects to include in the statement.
	 * @param currency The default currency for the statement (e.g., "USD").
	 * @param bankId The bank ID (routing number).
	 * @param acctId The account ID (account number).
	 * @param acctType The type of account (e.g., "CHECKING").
	 * @param balance The ledger balance of the account.
	 * @param dtAsOf The date as of which the balance is reported.
	 * @return A populated {@link BankMsgs} object.
	 */
	public static BankMsgs buildBankMsgs(List<Transaction> transactions, String currency,
										String bankId, String acctId, String acctType,
										BigDecimal balance, String dtAsOf)
	{
		
		BankMsgs bankMsgs = new BankMsgs();
		StatementTransactionResponse trnRs = new StatementTransactionResponse();
		
		Status status = new Status();
		status.code = 0;
		status.severity = "INFO";
		trnRs.status = status;
		trnRs.trnUid = "10001";
		
		StatementResponse stmtRs = new StatementResponse();
		stmtRs.curDef = currency;
		
		BankAccountInfo acct = new BankAccountInfo();
		acct.bankId = bankId;
		acct.acctId = acctId;
		acct.acctType = acctType;
		stmtRs.bankAcctFrom = acct;
		
		BankTransactionList list = new BankTransactionList();
		list.stmtTrns = transactions;
		list.dtStart = "20250301";
		list.dtEnd = "20250331";
		stmtRs.bankTranList = list;
		
		Balance ledgerBal = new Balance();
		ledgerBal.balAmt = balance;
		ledgerBal.dtAsOf = dtAsOf;
		stmtRs.ledgerBal = ledgerBal;
		
		trnRs.stmtRs = stmtRs;
		bankMsgs.stmtTrnrs = trnRs;
		return bankMsgs;
	}
	
}
