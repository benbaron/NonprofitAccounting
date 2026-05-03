/**
 * NonprofitAccounting JrxmlValidatorSimple.java JrxmlValidatorSimple
 */

package tools.jrxml;


import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.File;
import java.io.IOException;



/**
 * The Class JrxmlValidatorSimple.
 */
public class JrxmlValidatorSimple
{
	
	/**
	 * Jvsmain.
	 *
	 * @param args the args
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void jvsmain(String[] args) throws SAXException, IOException
	{
		
		if (args.length < 2)
		{
			System.err.println("Usage: JrxmlValidator <jrxml-file> <xsd-file>");
			return;
		}
		
		File jrxml = new File(args[0]);
		File xsd = new File(args[1]);
		
		SchemaFactory sf =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(xsd);
		Validator validator = schema.newValidator();
		
		try
		{
			validator.validate(new StreamSource(jrxml));
			System.out.println("VALID: " + jrxml);
		}
		catch (SAXException e)
		{
			System.err.println("INVALID: " + jrxml);
			System.err.println("Reason: " + e.getMessage());
		}
		
	}
	
}
