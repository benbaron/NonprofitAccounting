/* JasperReports - Free Java Reporting Library. Copyright (C) 2001 - 2025 Cloud
 * Software Group, Inc. All rights reserved. http://www.jaspersoft.com Unless
 * you have purchased a commercial license agreement from Jaspersoft, the
 * following license terms apply: This program is part of JasperReports.
 * JasperReports is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. JasperReports is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with JasperReports. If not, see
 * <http://www.gnu.org/licenses/>. */

package net.sf.jasperreports.engine.xml;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSimpleTemplate;
import net.sf.jasperreports.engine.JRTemplate;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.jackson.util.JacksonUtil;

/**
 * Report loader that inspects serialized JasperReports XML and dispatches to the
 * Jackson-based loader when the root element matches a report or template type.
 *
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class JacksonReportLoader implements ReportLoader
{
	
	private static final Log log = LogFactory.getLog(JacksonReportLoader.class);
	
	private static final JacksonReportLoader INSTANCE =
		new JacksonReportLoader();
	
	/**
	 * Returns the shared singleton instance.
	 *
	 * @return loader instance
	 */
	public static JacksonReportLoader instance()
	{
		return INSTANCE;
		
	}
	
	/**
	 * Attempts to load a {@link JasperDesign} from the provided XML bytes when the
	 * root element indicates a Jasper report. The underlying Jackson loader handles
	 * the XML-to-object conversion.
	 *
	 * @param context JasperReports context used for parsing
	 * @param data serialized report XML content
	 * @return populated design when a report root element is detected; otherwise empty
	 * @throws JRException if the XML cannot be parsed into a Jasper design
	 */
	@Override
	public Optional<JasperDesign> loadReport(JasperReportsContext context,
		byte[] data) throws JRException
	{
		boolean detectedReport = detectReportXML(data);
		
		if (detectedReport)
		{
			JasperDesign report = null;
			ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
			JasperDesign.setThreadJasperReportsContext(context);
			
			try
			{
				report = JacksonUtil.getInstance(context).loadXml(dataStream,
					JasperDesign.class);
			}
			finally
			{
				JasperDesign.removeThreadJasperReportsContext();
				JasperDesign.removeThreadInstance();
			}
			
			return Optional.of(report);
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Checks if the supplied XML represents a Jasper report.
	 *
	 * @param data serialized XML
	 * @return {@code true} when the root element is {@code jasperReport}
	 */
	protected boolean detectReportXML(byte[] data)
	{
		return detectRootElement(data, JRXmlConstants.ELEMENT_jasperReport);
		
	}
	
	/**
	 * Attempts to load a {@link JRTemplate} when the XML payload represents a
	 * template root element.
	 *
	 * @param context JasperReports context used for parsing
	 * @param data serialized template XML content
	 * @return populated template when a template root is detected; otherwise empty
	 */
	@Override
	public Optional<JRTemplate> loadTemplate(JasperReportsContext context,
		byte[] data)
	{
		boolean detectedReport = detectTemplateXML(data);
		
		if (detectedReport)
		{
			ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
			JRSimpleTemplate template =
				JacksonUtil.getInstance(context).loadXml(
					dataStream, JRSimpleTemplate.class);
			return Optional.of(template);
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Checks if the XML payload represents a Jasper template by verifying the
	 * expected root element.
	 *
	 * @param data serialized XML
	 * @return {@code true} when the root element matches the template name
	 */
	private boolean detectTemplateXML(byte[] data)
	{
		return detectRootElement(data, JRXmlConstants.TEMPLATE_ELEMENT_ROOT);
		
	}
	
	/**
	 * Streams the XML bytes and inspects the first start element to see if it matches
	 * the provided element name. The reader is namespace aware but only checks the
	 * local name to keep compatibility with different namespace declarations.
	 *
	 * @param data        serialized XML to inspect
	 * @param elementName expected root element local name
	 * @return {@code true} when the first start element matches
	 */
	protected boolean detectRootElement(byte[] data, String elementName)
	{
		XMLInputFactory f = XMLInputFactory.newInstance();
		f.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
			Boolean.FALSE);
		f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
			Boolean.FALSE);
		
		XMLStreamReader r = null;
		
		try
		{
			r = f.createXMLStreamReader(new ByteArrayInputStream(data));
			
			while (r.hasNext())
			{
				
				if (r.next() == XMLEvent.START_ELEMENT)
				{
					return elementName.equals(r.getLocalName()); // ignore
																	// namespace
				}
				
			}
			
			return false;
		}
		catch (XMLStreamException e)
		{
			log.debug("failed to load xml", e);
			return false;
		}
		finally
		{
			
			if (r != null)
			{
				
				try
				{
					r.close();
				}
				catch (XMLStreamException ignored)
				{
				}
				
			}
			
		}
		
	}
	
	
}
