package nonprofitbookkeeping.reports.jasper.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses JRXML templates to extract field and parameter definitions.
 */
public final class JrxmlFieldParser
{
    private JrxmlFieldParser()
    {
    }

    public static JrxmlFields parse(InputStream input) throws IOException
    {
        if (input == null)
        {
            return new JrxmlFields(Collections.emptyMap(),
                Collections.emptySet());
        }

        try
        {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);

            Map<String, String> fields = new LinkedHashMap<>();
            Set<String> parameters = new LinkedHashSet<>();

            NodeList fieldNodes = document.getElementsByTagNameNS("*",
                "field");
            for (int i = 0; i < fieldNodes.getLength(); i++)
            {
                Node node = fieldNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element element = (Element) node;
                String name = element.getAttribute("name");
                if (name == null || name.isBlank())
                {
                    continue;
                }
                String clazz = element.getAttribute("class");
                fields.putIfAbsent(name, clazz);
            }

            NodeList paramNodes = document.getElementsByTagNameNS("*",
                "parameter");
            for (int i = 0; i < paramNodes.getLength(); i++)
            {
                Node node = paramNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element element = (Element) node;
                String name = element.getAttribute("name");
                if (name == null || name.isBlank())
                {
                    continue;
                }
                parameters.add(name);
            }

            return new JrxmlFields(fields, parameters);
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new IOException("Failed to parse JRXML", e);
        }
    }

    public static final class JrxmlFields
    {
        private final Map<String, String> fields;
        private final Set<String> parameters;

        public JrxmlFields(Map<String, String> fields,
            Set<String> parameters)
        {
            this.fields = Collections.unmodifiableMap(
                new LinkedHashMap<>(fields));
            this.parameters = Collections.unmodifiableSet(
                new LinkedHashSet<>(parameters));
        }

        public Map<String, String> fields()
        {
            return this.fields;
        }

        public Set<String> parameters()
        {
            return this.parameters;
        }
    }
}
