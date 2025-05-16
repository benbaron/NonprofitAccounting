
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
	
	public static void writeOfxFile(String filePath,
									OFX ofxRoot) throws Exception
	{
		
		JAXBContext context = JAXBContext.newInstance(OFX.class);
		Marshaller marshaller = context.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(ofxRoot, new File(filePath));
	}
	
	// Sample utility to build a Signon section
	public static SignonMsgs buildSignon(	String language, String dtServer, String org, String fid,
											String code, String severity, String message)
	{
		SignonMsgs signonMsgs = new SignonMsgs();
		SignonResponse response = new SignonResponse();
		response.language = language;
		response.dtServer = dtServer;
		
		Status status = new Status();
		status.code = Integer.parseInt(code);
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
	
	// Sample utility to build a basic banking section
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
