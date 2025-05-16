
package nonprofitbookkeeping.model.ofx;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * Ofx20Writer generates an OFX 2.0 compliant XML file for a list of banking transactions.
 * <p>
 * It constructs an XML document with the required OFX elements, including BANKMSGSRSV2,
 * STMTTRNRS, STMTRS, and BANKTRANLIST, and then writes the result to a specified file.
 * </p>
 */
public class Ofx20Writer
{
	
	/**
	 * Writes the given list of transactions to an OFX 2.0 XML file.
	 * 
	 * @param transactions the list of transactions to export.
	 * @param outputFile the file to which the OFX XML should be written.
	 * @return the output file.
	 * @throws Exception if an error occurs during XML generation or file writing.
	 */
	public static File writeTransactions(	List<Transaction> transactions,
											File outputFile) throws Exception
	{
		// Create a new XML document.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		
		// Create the root element <OFX>
		Element ofx = doc.createElement("OFX");
		doc.appendChild(ofx);
		
		// Create BANKMSGSRSV2 section.
		Element bankMsgs = doc.createElement("BANKMSGSRSV2");
		ofx.appendChild(bankMsgs);
		
		// Create STMTTRNRS section.
		Element stmtTrnrs = doc.createElement("STMTTRNRS");
		bankMsgs.appendChild(stmtTrnrs);
		
		// Create STMTRS section.
		Element stmtRs = doc.createElement("STMTRS");
		stmtTrnrs.appendChild(stmtRs);
		
		// Create BANKTRANLIST section.
		Element bankTranList = doc.createElement("BANKTRANLIST");
		stmtRs.appendChild(bankTranList);
		
		// Formatter for date in OFX format (yyyyMMdd).
		DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		// For each transaction, create a STMTTRN element and fill it with the
		// transaction details.
		for (Transaction txn : transactions)
		{
			Element stmtTrn = doc.createElement("STMTTRN");
			bankTranList.appendChild(stmtTrn);
			
			Element trnType = doc.createElement("TRNTYPE");
			trnType.setTextContent(txn.getTransactionType());
			stmtTrn.appendChild(trnType);
			
			Element dtPosted = doc.createElement("DTPOSTED");
			dtPosted.setTextContent(txn.getLocalDate().format(dtFormatter));
			stmtTrn.appendChild(dtPosted);
			
			Element trnAmt = doc.createElement("TRNAMT");
			trnAmt.setTextContent(txn.getAmount().toPlainString());
			stmtTrn.appendChild(trnAmt);
			
			Element fitId = doc.createElement("FITID");
			fitId.setTextContent(txn.getFitid());
			stmtTrn.appendChild(fitId);
			
			Element name = doc.createElement("NAME");
			name.setTextContent(txn.getPayee());
			stmtTrn.appendChild(name);
			
			Element memo = doc.createElement("MEMO");
			memo.setTextContent(txn.getMemo());
			stmtTrn.appendChild(memo);
			
			if (txn.getNumber() != null && !txn.getNumber().isEmpty())
			{
				Element checkNum = doc.createElement("CHECKNUM");
				checkNum.setTextContent(txn.getNumber());
				stmtTrn.appendChild(checkNum);
			}
			
		}
		
		// Write the XML document to the output file.
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		
		// Enable pretty-printing.
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		
		try (FileOutputStream fos = new FileOutputStream(outputFile))
		{
			StreamResult result = new StreamResult(fos);
			transformer.transform(source, result);
		}
		
		return outputFile;
	}
	
}
