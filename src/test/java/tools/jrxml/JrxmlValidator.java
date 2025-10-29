
package tools.jrxml;

import net.sf.jasperreports.charts.design.JRDesignChart;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.BandTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JRSubreportParameter;
import net.sf.jasperreports.engine.JRVariable;

// XSD validation
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JRXML validator (v5.1):
 * 1) XML well-formedness (DOM parse; no network)
 * 2) Optional: namespace & section-shape normalization BEFORE schema
 * 3) Optional: XSD validation against monolithic schema
 * 4) Optional: aggressive rewrites to fit schema (bands, root normalization)
 * 5) Jasper digestion via JRXmlLoader.load(...) to surface JRException causes
 * 6) Lints (duplicate names, illegal identifiers, empty bands, class_/CDATA pitfalls)
 * 7) Expression reference analysis: undeclared $F/$P/$V and unused declarations
 *
 * Usage:
 *   java tools.jrxml.JrxmlValidator --file path/to/report.jrxml [--fail-on-warn]
 *   java tools.jrxml.JrxmlValidator --dir  path/to/reports       [--fail-on-warn]
 *
 * Extra:
 *   --xsd /path/jrxml_monolithic_v5_1.xsd
 *   --normalize-namespace
 *   --normalize-banded   (wrap direct printables in sections into <band> with height)
 *   --normalize-bandless (lift <band> children up into bandless sections)
 *   --fix-aggressive --write [--out dir]
 *
 * Exit codes: 0 = OK, 2 = any failures (or warnings if --fail-on-warn)
 */
public class JrxmlValidator
{
	
	// ---------------------- Constants & simple helpers ----------------------
	
	private static final String JR_NS =
		"http://jasperreports.sourceforge.net/jasperreports";
	
	private static final Set<String> TOP_LEVEL_ALLOWED =
		new LinkedHashSet<>(Arrays.asList(
			"property", "import", "reportFont", "style", "subDataset",
			"parameter", "parameterGroup",
			"queryString", "field", "sortField", "filterExpression", "variable",
			"group",
			"background", "title", "pageHeader", "columnHeader", "detail",
			"columnFooter", "pageFooter",
			"lastPageFooter", "summary", "noData"
		));
	
	private static final Set<String> PRINTABLES =
		new LinkedHashSet<>(Arrays.asList(
			"textField", "staticText", "rectangle", "line", "chart", "image",
			"subreport",
			"componentElement", "crosstab", "frame", "break", "list", "map",
			"table"
		));
	
	private static final List<String> SECTIONS = Arrays.asList(
		"background", "title", "pageHeader", "columnHeader", "detail",
		"columnFooter", "pageFooter", "lastPageFooter", "summary", "noData"
	);
	
	private static String ln(Node n)
	{
		return (n == null) ? null : n.getLocalName();
		
	}
	
	// ---------------------- Aggressive (schema-fit) rewrites
	// ----------------------
	
	private static Element ensureDetailBand(Document dom, List<String> actions)
	{
		Element root = dom.getDocumentElement();
		Element detail = firstChildElement(root, "detail");
		
		if (detail == null)
		{
			detail = dom.createElementNS(JR_NS, "detail");
			root.appendChild(detail);
			actions.add("added <detail>");
		}
		
		Element band = firstChildElement(detail, "band");
		
		if (band == null)
		{
			band = dom.createElementNS(JR_NS, "band");
			band.setAttribute("height", "50");
			detail.appendChild(band);
			actions.add("added <detail><band height=\"50\">");
		}
		else if (!band.hasAttribute("height"))
		{
			band.setAttribute("height", "50");
			actions.add("set default band height in <detail>");
		}
		
		return band;
		
	}
	
	private static void ensureSingleBandAndMerge(Document dom, Element section,
		List<String> actions)
	{
		List<Element> bands = new ArrayList<>();
		NodeList kids = section.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			
			if (n.getNodeType() == Node.ELEMENT_NODE && "band".equals(ln(n)))
			{
				bands.add((Element) n);
			}
			
		}
		
		if (bands.isEmpty())
		{
			Element b = dom.createElementNS(JR_NS, "band");
			b.setAttribute("height", "50");
			section.appendChild(b);
			actions.add("added <band> in <" + ln(section) + ">");
			return;
		}
		
		Element first = bands.get(0);
		
		for (int i = 1; i < bands.size(); i++)
		{
			Element extra = bands.get(i);
			NodeList cs = extra.getChildNodes();
			while (cs.getLength() > 0)
				first.appendChild(cs.item(0));
			section.removeChild(extra);
		}
		
		if (!first.hasAttribute("height"))
		{
			first.setAttribute("height", "50");
			actions.add("set default band height in <" + ln(section) + ">");
		}
		
	}
	
	private static void moveStrayTopLevelPrintables(Document dom, Element root,
		Element detailBand, List<String> actions)
	{
		List<Node> toMove = new ArrayList<>();
		NodeList kids = root.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String name = ln(n);
			
			if (!TOP_LEVEL_ALLOWED.contains(name))
			{
				
				if (PRINTABLES.contains(name) || "textElement".equals(name) ||
					"paragraph".equals(name))
				{
					toMove.add(n);
				}
				
			}
			
		}
		
		for (Node n : toMove)
		{
			Node moved = n;
			String name = ln(n);
			
			if ("textElement".equals(name) || "paragraph".equals(name))
			{
				moved = wrapTextish(dom, (Element) n);
				root.replaceChild(moved, n);
			}
			else
			{
				root.removeChild(n);
			}
			
			detailBand.appendChild(moved);
		}
		
		if (!toMove.isEmpty())
			actions.add("moved " + toMove.size() +
				" stray top-level element(s) into <detail>/<band>");
		
	}
	
	private static Element wrapTextish(Document dom, Element inner)
	{
		Element st = dom.createElementNS(JR_NS, "staticText");
		Element re = dom.createElementNS(JR_NS, "reportElement");
		re.setAttribute("x", "0");
		re.setAttribute("y", "0");
		re.setAttribute("width", "100");
		re.setAttribute("height", "20");
		st.appendChild(re);
		st.appendChild(inner);
		Element text = dom.createElementNS(JR_NS, "text");
		text.appendChild(dom.createCDATASection(""));
		st.appendChild(text);
		return st;
		
	}
	
	private static void normalizeSections(Document dom, Element root,
		List<String> actions)
	{
		
		for (String s : SECTIONS)
		{
			
			for (Element el = firstChildElement(root, s); el != null;
				el = nextSiblingElement(el, s))
			{
				ensureSingleBandAndMerge(dom, el, actions);
			}
			
		}
		
	}
	
	private static void ensureBandHeights(Document dom, Element root,
		List<String> actions)
	{
		NodeList bands = root.getElementsByTagNameNS(JR_NS, "band");
		
		for (int i = 0; i < bands.getLength(); i++)
		{
			Element b = (Element) bands.item(i);
			
			if (!b.hasAttribute("height"))
			{
				b.setAttribute("height", "50");
				actions.add("set missing band height");
			}
			
		}
		
	}
	
	private static void normalizeToCoreNamespace(Document dom,
		List<String> actions)
	{
		Element root = dom.getDocumentElement();
		if (root == null)
			return;
		String local = ln(root);
		
		if (!"jasperReport".equals(local) ||
			!JR_NS.equals(root.getNamespaceURI()))
		{
			Element newRoot = dom.createElementNS(JR_NS, "jasperReport");
			
			if (root.hasAttributes())
			{
				
				for (int i = 0; i < root.getAttributes().getLength(); i++)
				{
					Node a = root.getAttributes().item(i);
					newRoot.setAttribute(a.getNodeName(), a.getNodeValue());
				}
				
			}
			
			while (root.getFirstChild() != null)
				newRoot.appendChild(root.getFirstChild());
			dom.replaceChild(newRoot, root);
		}
		
	}
	
	private static void dropDoctype(Document dom, List<String> actions)
	{
		
		if (dom.getDoctype() != null)
		{
			dom.removeChild(dom.getDoctype());
			actions.add("removed DOCTYPE");
		}
		
	}
	
	private static Element nextSiblingElement(Element el, String localName)
	{
		
		for (Node n = el.getNextSibling(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE && localName.equals(ln(n)))
				return (Element) n;
		}
		
		return null;
		
	}
	
	private static boolean validateAgainstSchema(Document dom, Schema schema,
		ValidationReport r)
	{
		if (schema == null)
			return true;
		
		try
		{
			Validator v = schema.newValidator();
			v.setErrorHandler(new org.xml.sax.ErrorHandler()
			{
				@Override
				public void warning(SAXParseException e)
				{
					r.warn("XSD", formatSax(e));
					
				}
				
				@Override
				public void error(SAXParseException e)
				{
					r.err("XSD", formatSax(e));
					
				}
				
				@Override
				public void fatalError(SAXParseException e)
				{
					r.err("XSD", formatSax(e));
					
				}
				
			});
			v.validate(new DOMSource(dom));
			r.info("XSD", "Schema validation OK.");
			return true;
		}
		catch (Exception ex)
		{
			r.err("XSD", "Schema validation failed: " + ex.getMessage());
			return false;
		}
		
	}
	
	private static void writeDom(Document dom, File out) throws Exception
	{
		javax.xml.transform.TransformerFactory tf =
			javax.xml.transform.TransformerFactory.newInstance();
		
		try
		{
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
		catch (Throwable ignore)
		{
		}
		
		javax.xml.transform.Transformer t = tf.newTransformer();
		t.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		t.transform(new javax.xml.transform.dom.DOMSource(dom),
			new javax.xml.transform.stream.StreamResult(out));
		
	}
	
	private static List<String> aggressiveRewrite(Document dom)
	{
		List<String> actions = new ArrayList<>();
		dropDoctype(dom, actions);
		normalizeToCoreNamespace(dom, actions);
		Element root = dom.getDocumentElement();
		Element detailBand = ensureDetailBand(dom, actions);
		moveStrayTopLevelPrintables(dom, root, detailBand, actions);
		normalizeSections(dom, root, actions);
		ensureBandHeights(dom, root, actions);
		return actions;
		
	}
	
	// ---------------------- Section-shape & namespace normalization
	// ----------------------
	
	private static final Set<String> SECTION_TAGS =
		new LinkedHashSet<>(Arrays.asList(
			"background", "title", "pageHeader", "columnHeader",
			"detail", "columnFooter", "pageFooter", "lastPageFooter", "summary",
			"noData"
		));
	
	private static final Set<String> PRINTABLES_TAGS =
		new LinkedHashSet<>(Arrays.asList(
			"staticText", "textField", "rectangle", "line", "image",
			"subreport",
			"componentElement", "crosstab", "frame", "break", "list", "map",
			"table", "chart"
		));
	
	private static boolean normalizeNamespaceToJR(Document dom)
	{
		Element root = dom.getDocumentElement();
		if (root == null)
			return false;
		
		if (JR_NS.equals(root.getNamespaceURI()))
		{
			
			if (!JR_NS.equals(root.lookupNamespaceURI(null)))
			{
				root.setAttribute("xmlns", JR_NS);
			}
			
			return false;
		}
		
		Element newRoot = reNsDeep(dom, root, JR_NS);
		dom.replaceChild(newRoot, root);
		return true;
		
	}
	
	private static Element reNsDeep(Document dom, Element oldEl, String ns)
	{
		String local = oldEl.getLocalName() != null ? oldEl.getLocalName() :
			oldEl.getNodeName();
		Element neo = dom.createElementNS(ns, local);
		
		// copy attributes except xmlns*
		if (oldEl.hasAttributes())
		{
			
			for (int i = 0; i < oldEl.getAttributes().getLength(); i++)
			{
				Node a = oldEl.getAttributes().item(i);
				String an = a.getNodeName();
				if ("xmlns".equals(an) || an.startsWith("xmlns:"))
					continue;
				
				if (a.getNamespaceURI() != null)
				{
					neo.setAttributeNS(a.getNamespaceURI(), a.getNodeName(),
						a.getNodeValue());
				}
				else
				{
					neo.setAttribute(a.getNodeName(), a.getNodeValue());
				}
				
			}
			
		}
		
		neo.setAttribute("xmlns", ns);
		
		// recursive children
		for (Node c = oldEl.getFirstChild(); c != null; c = c.getNextSibling())
		{
			
			if (c.getNodeType() == Node.ELEMENT_NODE)
			{
				neo.appendChild(reNsDeep(dom, (Element) c, ns));
			}
			else
			{
				neo.appendChild(c.cloneNode(true));
			}
			
		}
		
		return neo;
		
	}
	
	private static int normalizeSectionsToBanded(Document dom)
	{
		Element root = dom.getDocumentElement();
		int changed = 0;
		
		for (String sec : SECTION_TAGS)
		{
			
			for (Element section = firstChildElement(root, sec);
				section != null; section = nextSiblingElement(section, sec))
			{
				if (hasDirectBand(section))
					continue;
				List<Element> directPrintables =
					collectDirectChildren(section, PRINTABLES_TAGS);
				if (directPrintables.isEmpty())
					continue;
				Element band = dom.createElementNS(JR_NS, "band");
				String h = section.getAttribute("height");
				band.setAttribute("height",
					(h != null && !h.isEmpty()) ? h : "50");
				
				for (Element p : directPrintables)
				{
					band.appendChild(p);
				}
				
				section.appendChild(band);
				changed++;
			}
			
		}
		
		return changed;
		
	}
	
	private static int normalizeSectionsToBandless(Document dom)
	{
		Element root = dom.getDocumentElement();
		int changed = 0;
		
		for (String sec : SECTION_TAGS)
		{
			
			for (Element section = firstChildElement(root, sec);
				section != null; section = nextSiblingElement(section, sec))
			{
				List<Element> bands = collectDirectChildren(section,
					Collections.singleton("band"));
				if (bands.isEmpty())
					continue;
				
				for (int i = 0; i < bands.size(); i++)
				{
					Element b = bands.get(i);
					
					if (i == 0)
					{
						
						if (!section.hasAttribute("height") &&
							b.hasAttribute("height"))
						{
							section.setAttribute("height",
								b.getAttribute("height"));
						}
						
						moveAllChildren(b, section);
					}
					else
					{
						moveAllChildren(b, section);
					}
					
					section.removeChild(b);
					changed++;
				}
				
			}
			
		}
		
		return changed;
		
	}
	
	private static boolean hasDirectBand(Element section)
	{
		NodeList kids = section.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && "band".equals(ln(n)))
				return true;
		}
		
		return false;
		
	}
	
	private static List<Element> collectDirectChildren(Element parent,
		Set<String> locals)
	{
		List<Element> out = new ArrayList<>();
		NodeList kids = parent.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && locals.contains(ln(n)))
				out.add((Element) n);
		}
		
		return out;
		
	}
	
	private static void moveAllChildren(Element from, Element to)
	{
		List<Node> list = new ArrayList<>();
		NodeList kids = from.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
			list.add(kids.item(i));
		for (Node n : list)
			to.appendChild(n);
		
	}
	
	// ---------------------- CLI ----------------------
	
	public static void jrxmain(String[] args) throws Exception
	{
		Map<String, String> cli = parseArgs(args);
		
		if (cli.isEmpty() ||
			(!cli.containsKey("file") && !cli.containsKey("dir")))
		{
			printHelp();
			System.exit(1);
		}
		
		System.setProperty("net.sf.jasperreports.debug", "true");
		
		boolean failOnWarn = cli.containsKey("fail-on-warn");
		List<File> files = collectFiles(cli);
		
		int ok = 0, fail = 0;
		
		Schema schema = loadSchema(cli.get("xsd"));
		boolean doAggressive = cli.containsKey("fix-aggressive");
		boolean writeFixes = cli.containsKey("write");
		File outDir = cli.containsKey("out") ? new File(cli.get("out")) : null;
		
		boolean normNS = cli.containsKey("normalize-namespace");
		boolean normBanded = cli.containsKey("normalize-banded");
		boolean normBandless = cli.containsKey("normalize-bandless");
		
		for (File f : files)
		{
			
			try
			{
				ValidationReport r = validateOne(
					f, failOnWarn, schema, doAggressive, writeFixes, outDir,
					normNS, normBanded, normBandless
				);
				r.print();
				if (r.isValid())
					ok++;
				else
					fail++;
			}
			catch (Throwable t)
			{
				ValidationReport cr = new ValidationReport(f, failOnWarn);
				cr.err("CRASH", t.getClass().getName() + " - " +
					String.valueOf(t.getMessage()));
				
				for (Throwable c = t.getCause(); c != null; c = c.getCause())
				{
					cr.err("CAUSE", c.getClass().getName() + " - " +
						String.valueOf(c.getMessage()));
				}
				
				SAXParseException sax = findCause(t, SAXParseException.class);
				
				if (sax != null)
				{
					cr.err("XML", String.format("%s (line %d, col %d)",
						sax.getMessage(), sax.getLineNumber(),
						sax.getColumnNumber()));
					printContext(f, sax.getLineNumber(), 3, cr);
				}
				
				cr.print();
				fail++;
			}
			
		}
		
		System.out.printf("%nSummary: %d OK, %d FAIL%n", ok, fail);
		System.exit(fail > 0 ? 2 : 0);
		
	}
	
	private static Map<String, String> parseArgs(String[] args)
	{
		Map<String, String> m = new HashMap<>();
		
		for (int i = 0; i < args.length; i++)
		{
			
			switch(args[i])
			{
				case "--file":
					if (i + 1 < args.length)
						m.put("file", args[++i]);
					break;
				
				case "--dir":
					if (i + 1 < args.length)
						m.put("dir", args[++i]);
					break;
				
				case "--fail-on-warn":
					m.put("fail-on-warn", "true");
					break;
				
				case "--xsd":
					if (i + 1 < args.length)
						m.put("xsd", args[++i]);
					break;
				
				case "--fix-aggressive":
					m.put("fix-aggressive", "true");
					break;
				
				case "--write":
					m.put("write", "true");
					break;
				
				case "--out":
					if (i + 1 < args.length)
						m.put("out", args[++i]);
					break;
				
				case "--normalize-namespace":
					m.put("normalize-namespace", "true");
					break;
				
				case "--normalize-banded":
					m.put("normalize-banded", "true");
					break;
				
				case "--normalize-bandless":
					m.put("normalize-bandless", "true");
					break;
			}
			
		}
		
		return m;
		
	}
	
	private static void printHelp()
	{
		System.out.println("JRXML Validator v5.1");
		System.out.println("Usage:");
		System.out.println(
			"  java tools.jrxml.JrxmlValidator --file path/to/report.jrxml [--fail-on-warn]");
		System.out.println(
			"  java tools.jrxml.JrxmlValidator --dir  path/to/reports       [--fail-on-warn]");
		System.out.println("Options:");
		System.out.println(
			"  --xsd /path/schema.xsd        Validate against XSD before Jasper load");
		System.out.println(
			"  --normalize-namespace         Inject JR namespace recursively before XSD");
		System.out.println(
			"  --normalize-banded            Force banded sections (wrap direct printables into <band>)");
		System.out.println(
			"  --normalize-bandless          Force bandless sections (lift <band> children into section)");
		System.out.println(
			"  --fix-aggressive              Apply structural rewrites to match schema");
		System.out.println(
			"  --write                       Write fixes back to files (or --out)");
		System.out.println(
			"  --out /path/dir               Where to write fixed files (default: in-place)");
		System.out.println(usage);
		
	}
	
	static final String usage =
		"Usage\n" +
			"\n" +
			"Validate + optionally normalize + aggressively fix to OUT dir + revalidate + run Jasper checks\n" +
			"\n" +
			"java tools.jrxml.JrxmlValidator \\\n" +
			"  --dir reports/ \\\n" +
			"  --xsd /path/to/jrxml_monolithic_v5_1.xsd \\\n" +
			"  --normalize-namespace --normalize-banded \\\n" +
			"  --fix-aggressive --write --out fixed/\n" +
			"\n" +
			"Single file, in-place (keeps a .bak)\n" +
			"\n" +
			"java tools.jrxml.JrxmlValidator \\\n" +
			"  --file report.jrxml \\\n" +
			"  --xsd /path/to/jrxml_monolithic_v5_1.xsd \\\n" +
			"  --normalize-namespace --normalize-bandless \\\n" +
			"  --fix-aggressive --write\n";
	
	// ---------------------- Schema loader ----------------------
	
	private static Schema loadSchema(String xsdPath) throws Exception
	{
		if (xsdPath == null)
			return null;
		SchemaFactory sf =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		try
		{
			sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		}
		catch (Throwable ignore)
		{
		}
		
		return sf.newSchema(new File(xsdPath));
		
	}
	
	private static List<File> collectFiles(Map<String, String> cli)
		throws Exception
	{
		List<File> out = new ArrayList<>();
		
		if (cli.containsKey("file"))
		{
			File f = new File(cli.get("file"));
			if (f.isFile() && f.getName().toLowerCase().endsWith(".jrxml"))
				out.add(f);
		}
		else
		{
			File root = new File(cli.get("dir"));
			Files.walk(root.toPath()).forEach(p -> {
				if (p.toString().toLowerCase().endsWith(".jrxml"))
					out.add(p.toFile());
			});
		}
		
		return out;
		
	}
	
	// ---------------------- Validation ----------------------
	
	private static final Pattern ESCAPED_CDATA_START =
		Pattern.compile("(?s).*?&lt;!\\[CDATA\\[.*");
	private static final Pattern ESCAPED_CDATA_END =
		Pattern.compile("(?s).*?]]&gt;.*");
	private static final Pattern RAW_AMP_IN_TEXT =
		Pattern.compile("(?s).*<text>.*&[^#a-zA-Z].*</text>.*");
	
	private static final Pattern NAME_BAD_CHARS = Pattern.compile(".*[\\s-].*");
	private static final Pattern NAME_LEADING_DIGIT =
		Pattern.compile("^[0-9].*");
	
	private static final Pattern REF_FIELD = Pattern.compile("\\$F\\{([^}]+)}");
	private static final Pattern REF_PARAM = Pattern.compile("\\$P\\{([^}]+)}");
	private static final Pattern REF_VAR = Pattern.compile("\\$V\\{([^}]+)}");
	
	private static final Set<String> BUILTIN_VARS = new HashSet<>(Arrays.asList(
		"PAGE_NUMBER", "COLUMN_NUMBER", "REPORT_COUNT", "PAGE_COUNT",
		"COLUMN_COUNT",
		"MASTER_CURRENT_PAGE", "MASTER_TOTAL_PAGES", "MASTER_CURRENT_COLUMN",
		"MASTER_TOTAL_COLUMNS",
		"IS_IGNORE_PAGINATION"
	));
	
	private static ValidationReport validateOne(
		File jrxml, boolean failOnWarn, Schema schema,
		boolean doAggressive, boolean writeFixes, File outDir,
		boolean normalizeNamespace, boolean normalizeBanded,
		boolean normalizeBandless
	)
	{
		ValidationReport report = new ValidationReport(jrxml, failOnWarn);
		
		// -------- 0) Raw file text heuristics --------
		String xmlText;
		
		try
		{
			xmlText = Files.readString(jrxml.toPath(), StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			report.err("IO", "Read error: " + e.getMessage());
			return report;
		}
		
		if (xmlText.contains("class_="))
		{
			report.err("LINT",
				"Found attribute 'class_=' (typo). Use 'class='.");
		}
		
		if (ESCAPED_CDATA_START.matcher(xmlText).matches() ||
			ESCAPED_CDATA_END.matcher(xmlText).matches())
		{
			report.err("LINT",
				"Found escaped CDATA markers (&lt;![CDATA[ or ]]&gt;). Use real CDATA blocks.");
		}
		
		if (RAW_AMP_IN_TEXT.matcher(xmlText).matches())
		{
			report.warn("LINT",
				"Possible raw '&' inside <text>. Escape as &amp; or wrap the content in CDATA.");
		}
		
		// -------- 1) XML well-formedness (DOM, no network) --------
		Document dom = parseDom(jrxml, report);
		if (dom == null)
			return report;
		
		// -------- 1.0) Optional normalization BEFORE XSD --------
		if (normalizeNamespace)
		{
			if (normalizeNamespaceToJR(dom))
				report.info("NORM", "Injected JR namespace recursively.");
		}
		
		if (normalizeBanded && normalizeBandless)
		{
			report.warn("NORM",
				"Both banded and bandless normalization requested; preferring banded.");
			normalizeBandless = false;
		}
		
		if (normalizeBanded)
		{
			int n = normalizeSectionsToBanded(dom);
			if (n > 0)
				report.info("NORM",
					"Wrapped " + n + " bandless section(s) into <band>.");
		}
		
		if (normalizeBandless)
		{
			int n = normalizeSectionsToBandless(dom);
			if (n > 0)
				report.info("NORM",
					"Lifted " + n + " band(s) to bandless sections.");
		}
		
		// -------- 1.1) Optional XSD validation + aggressive rewrite --------
		boolean xsdOk = true;
		if (schema != null)
			xsdOk = validateAgainstSchema(dom, schema, report);
		
		boolean rewrote = false;
		
		if (schema != null && doAggressive && !xsdOk)
		{
			List<String> actions = aggressiveRewrite(dom);
			
			if (!actions.isEmpty())
			{
				rewrote = true;
				report.info("FIX", "Aggressive rewrite: " + actions);
			}
			
			xsdOk = validateAgainstSchema(dom, schema, report);
		}
		
		// -------- 1.2) Root sanity after any rewrites --------
		Element root = dom.getDocumentElement();
		
		if (root == null || !"jasperReport".equals(root.getLocalName()))
		{
			report.err("STRUCT", "Root element must be <jasperReport>.");
			return report;
		}
		
		if (!root.hasAttribute("uuid"))
		{
			report.warn("STRUCT",
				"Root <jasperReport> missing 'uuid' attribute (recommended).");
		}
		
		// -------- 1.3) DOM-only structural scan --------
		preflightStructuralChecks(dom, xmlText, report);
		
		// -------- 1.4) Persist fixes if requested --------
		File jasperInputFile = jrxml;
		byte[] jasperInputBytes = null;
		
		if (rewrote || normalizeNamespace || normalizeBanded ||
			normalizeBandless)
		{
			
			if (writeFixes)
			{
				
				try
				{
					File target = (outDir != null) ?
						new File(outDir, jrxml.getName()) : jrxml;
					if (outDir != null && !outDir.exists())
						outDir.mkdirs();
					
					if (outDir == null)
					{
						Files.copy(jrxml.toPath(),
							new File(jrxml.getParentFile(),
								jrxml.getName() + ".bak").toPath(),
							java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					}
					
					writeDom(dom, target);
					report.info("FIX", "Wrote: " + target.getAbsolutePath());
					jasperInputFile = target;
				}
				catch (Exception e)
				{
					report.err("FIX", "Write failed: " + e.getMessage());
				}
				
			}
			
			if (!writeFixes || (writeFixes && !jasperInputFile.exists()))
			{
				
				try
				{
					javax.xml.transform.TransformerFactory tf =
						javax.xml.transform.TransformerFactory.newInstance();
					
					try
					{
						tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
						tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,
							"");
					}
					catch (Throwable ignore)
					{
					}
					
					javax.xml.transform.Transformer t = tf.newTransformer();
					t.setOutputProperty(javax.xml.transform.OutputKeys.INDENT,
						"yes");
					t.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "2");
					java.io.ByteArrayOutputStream baos =
						new java.io.ByteArrayOutputStream();
					t.transform(new javax.xml.transform.dom.DOMSource(dom),
						new javax.xml.transform.stream.StreamResult(baos));
					jasperInputBytes = baos.toByteArray();
				}
				catch (Exception e)
				{
					report.err("FIX", "Serialize failed: " + e.getMessage());
					jasperInputBytes = null;
				}
				
			}
			
		}
		
		// -------- 2) Jasper digestion (JRXmlLoader.load) --------
		JasperDesign design;
		
		try
		{
			
			if (jasperInputBytes != null)
			{
				design = JRXmlLoader
					.load(new java.io.ByteArrayInputStream(jasperInputBytes));
			}
			else
			{
				design = JRXmlLoader.load(jasperInputFile);
			}
			
			report.info("JASPER",
				"JRXmlLoader.load OK (JasperDesign created).");
		}
		catch (JRException e)
		{
			report.err("JASPER", "JRXmlLoader.load failed: " + e.getMessage());
			SAXParseException sax = findCause(e, SAXParseException.class);
			
			if (sax != null)
			{
				report.err("XML", String.format("%s (line %d, col %d)",
					sax.getMessage(), sax.getLineNumber(),
					sax.getColumnNumber()));
				printContext(jrxml, sax.getLineNumber(), 3, report);
				addTrace(report, e);
				return report;
			}
			
			int[] lc = extractLineColFromMessages(e);
			
			if (lc != null)
			{
				report.err("XML", "Reported at line " + lc[0] +
					" col " + lc[1] + " (from exception message)");
				printContext(jrxml, lc[0], 3, report);
			}
			
			String hint = detectJacksonUnrecognizedFieldHint(e);
			if (hint != null)
			{
				report.err("HINT", hint);
			}
			dumpCauseChain(report, e);
			addTrace(report, e);
			return report;
		}
		
		// -------- 3) Lints --------
		lintNamedDeclarations(design, report);
		lintEmptyBands(design, report);
		
		// -------- 4) Expression reference analysis --------
		analyzeExpressionReferences(design, report);
		
		return report;
		
	}
	
	private static Document parseDom(File f, ValidationReport report)
	{
		
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setNamespaceAware(true);
			
			try
			{
				dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			}
			catch (Throwable ignore)
			{
			}
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new org.xml.sax.ErrorHandler()
			{
				@Override
				public void warning(SAXParseException e)
				{
					report.warn("XML", formatSax(e));
					
				}
				
				@Override
				public void error(SAXParseException e)
				{
					report.err("XML", formatSax(e));
					
				}
				
				@Override
				public void fatalError(SAXParseException e)
				{
					report.err("XML", formatSax(e));
					
				}
				
			});
			return db.parse(f);
		}
		catch (SAXParseException e)
		{
			report.err("XML", formatSax(e));
		}
		catch (Exception e)
		{
			report.err("XML", "Parse failure: " + e.getClass().getSimpleName() +
				" - " + e.getMessage());
		}
		
		return null;
		
	}
	
	private static String formatSax(SAXParseException e)
	{
		return String.format("%s at line %d, col %d", e.getMessage(),
			e.getLineNumber(), e.getColumnNumber());
		
	}
	
	// ---------------------- DOM-only preflight (no Jasper)
	// ----------------------
	
	private static void preflightStructuralChecks(Document dom, String xmlText,
		ValidationReport r)
	{
		Element root = dom.getDocumentElement();
		
		String[] singleBandSections =
		{
			"background", "title", "pageHeader", "columnHeader",
			"columnFooter", "pageFooter", "lastPageFooter", "summary", "noData"
		};
		
		for (String secName : singleBandSections)
		{
			NodeList secs = root.getElementsByTagName(secName);
			
			for (int i = 0; i < secs.getLength(); i++)
			{
				Element sec = (Element) secs.item(i);
				int directBands = countDirectChildren(sec, "band");
				
				// Don't hard-error on band count; warn if 0 and no direct
				// printables
				if (directBands == 0)
				{
					List<Element> directPrint =
						collectDirectChildren(sec, PRINTABLES_TAGS);
					
					if (directPrint.isEmpty())
					{
						r.err("STRUCT", "<" + secName +
							"> has neither <band> nor direct printables.");
					}
					
				}
				else if (directBands > 1)
				{
					r.warn("STRUCT", "<" + secName + "> has " + directBands +
						" <band> children (will be merged in aggressive mode).");
				}
				
				// check nested bands
				NodeList bands = sec.getElementsByTagName("band");
				
				for (int b = 0; b < bands.getLength(); b++)
				{
					Element band = (Element) bands.item(b);
					
					if (hasDescendantTag(band, "band"))
					{
						r.err("STRUCT",
							"Nested <band> found under <" + secName +
								">; bands cannot contain <band> descendants.");
					}
					
				}
				
			}
			
		}
		
		NodeList details = root.getElementsByTagName("detail");
		
		for (int i = 0; i < details.getLength(); i++)
		{
			Element det = (Element) details.item(i);
			NodeList bands = det.getElementsByTagName("band");
			
			if (bands.getLength() == 0)
			{
				List<Element> directPrint =
					collectDirectChildren(det, PRINTABLES_TAGS);
				
				if (directPrint.isEmpty())
				{
					r.err("STRUCT",
						"<detail> has neither <band> nor direct printables.");
				}
				
			}
			
			for (int b = 0; b < bands.getLength(); b++)
			{
				Element band = (Element) bands.item(b);
				
				if (hasDescendantTag(band, "band"))
				{
					r.err("STRUCT",
						"Nested <band> found under <detail>; bands cannot contain <band> descendants.");
				}
				
			}
			
		}
		
		NodeList groups = root.getElementsByTagName("group");
		
		for (int i = 0; i < groups.getLength(); i++)
		{
			Element g = (Element) groups.item(i);
			Element gh = firstChildElement(g, "groupHeader");
			if (gh != null)
				checkNoNestedBandsInSection("groupHeader", gh, r);
			Element gf = firstChildElement(g, "groupFooter");
			if (gf != null)
				checkNoNestedBandsInSection("groupFooter", gf, r);
		}
		
		if (xmlText.contains("&lt;![CDATA[") || xmlText.contains("]]&gt;"))
		{
			r.err("LINT",
				"Escaped CDATA markers (&lt;![CDATA[ or ]]&gt;) found. Replace with real CDATA blocks.");
		}
		
		Pattern rawAmp = Pattern.compile(
			"(?s)<(text|[A-Za-z0-9:_-]*Expression)(?:[^>]*)>[^<]*&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9a-fA-F]+;)[^<]*</\\1>");
		
		if (rawAmp.matcher(xmlText).find())
		{
			r.warn("LINT",
				"Likely unescaped '&' in <text> or *Expression content. Escape as &amp; or wrap in CDATA.");
		}
		
	}
	
	private static void checkNoNestedBandsInSection(String secName,
		Element section, ValidationReport r)
	{
		NodeList bands = section.getElementsByTagName("band");
		
		for (int b = 0; b < bands.getLength(); b++)
		{
			Element band = (Element) bands.item(b);
			
			if (hasDescendantTag(band, "band"))
			{
				r.err("STRUCT", "Nested <band> found under <" + secName +
					">; bands cannot contain <band> descendants.");
			}
			
		}
		
	}
	
	private static int countDirectChildren(Element parent, String tag)
	{
		int count = 0;
		NodeList kids = parent.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			
			if (n.getNodeType() == Node.ELEMENT_NODE &&
				tag.equals(n.getNodeName()))
			{
				count++;
			}
			
		}
		
		return count;
		
	}
	
	private static boolean hasDescendantTag(Element parent, String tag)
	{
		return parent.getElementsByTagName(tag).getLength() > 0;
		
	}
	
	private static Element firstChildElement(Element parent, String tag)
	{
		NodeList kids = parent.getChildNodes();
		
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			
			if (n.getNodeType() == Node.ELEMENT_NODE &&
				tag.equals(n.getNodeName()))
			{
				return (Element) n;
			}
			
		}
		
		return null;
		
	}
	
	// ---------------------- Lints ----------------------
	
	private static void lintNamedDeclarations(JasperDesign d,
		ValidationReport r)
	{
		Set<String> fields = new LinkedHashSet<>();
		
		for (JRField f : listFromArray(d.getFields()))
		{
			checkName("field", f.getName(), r);
			if (!fields.add(f.getName()))
				r.err("LINT", "Duplicate field: " + f.getName());
			
			if (((JRDesignField) f).getValueClassName() == null)
			{
				r.warn("LINT", "<field name=\"" + f.getName() +
					"\"> missing class (defaults to java.lang.String).");
			}
			
		}
		
		Set<String> params = new LinkedHashSet<>();
		
		for (JRParameter p : listFromArray(d.getParameters()))
		{
			if (p.isSystemDefined())
				continue;
			checkName("parameter", p.getName(), r);
			if (!params.add(p.getName()))
				r.err("LINT", "Duplicate parameter: " + p.getName());
		}
		
		Set<String> vars = new LinkedHashSet<>();
		
		for (JRVariable v : listFromArray(d.getVariables()))
		{
			if (v.isSystemDefined())
				continue;
			checkName("variable", v.getName(), r);
			if (!vars.add(v.getName()))
				r.err("LINT", "Duplicate variable: " + v.getName());
		}
		
		Set<String> styles = new LinkedHashSet<>();
		
		for (JRStyle s : listFromArray(d.getStyles()))
		{
			checkName("style", s.getName(), r);
			if (!styles.add(s.getName()))
				r.err("LINT", "Duplicate style: " + s.getName());
		}
		
	}
	
	private static void checkName(String kind, String name, ValidationReport r)
	{
		
		if (name == null || name.isEmpty())
		{
			r.err("LINT", "<" + kind + "> missing name.");
			return;
		}
		
		if (NAME_BAD_CHARS.matcher(name).matches())
		{
			r.err("LINT",
				"<" + kind + "> name has space or hyphen: '" + name + "'");
		}
		
		if (NAME_LEADING_DIGIT.matcher(name).matches())
		{
			r.err("LINT", "<" + kind + "> name starts with digit: '" + name +
				"' (prefix with underscore).");
		}
		
	}
	
	private static void lintEmptyBands(JasperDesign d, ValidationReport r)
	{
		Map<BandTypeEnum, JRBand> bands = getAllBands(d);
		
		if (bands.isEmpty())
		{
			r.err("STRUCT", "No bands found (title/detail/pageHeader/etc.).");
			return;
		}
		
		for (Map.Entry<BandTypeEnum, JRBand> e : bands.entrySet())
		{
			JRBand band = e.getValue();
			
			if (band instanceof JRDesignBand)
			{
				JRDesignBand db = (JRDesignBand) band;
				List<JRChild> kids = db.getChildren();
				
				if (kids == null || kids.isEmpty())
				{
					r.warn("STRUCT", "Empty band: " + e.getKey() + " (height=" +
						db.getHeight() + ")");
				}
				
			}
			
		}
		
	}
	
	private static Map<BandTypeEnum, JRBand> getAllBands(JasperDesign d)
	{
		Map<BandTypeEnum, JRBand> out = new LinkedHashMap<>();
		if (d.getTitle() != null)
			out.put(BandTypeEnum.TITLE, d.getTitle());
		if (d.getPageHeader() != null)
			out.put(BandTypeEnum.PAGE_HEADER, d.getPageHeader());
		if (d.getColumnHeader() != null)
			out.put(BandTypeEnum.COLUMN_HEADER, d.getColumnHeader());
		
		if (d.getDetailSection() != null &&
			d.getDetailSection().getBands() != null)
		{
			for (JRBand b : d.getDetailSection().getBands())
				out.put(BandTypeEnum.DETAIL, b);
		}
		
		if (d.getColumnFooter() != null)
			out.put(BandTypeEnum.COLUMN_FOOTER, d.getColumnFooter());
		if (d.getPageFooter() != null)
			out.put(BandTypeEnum.PAGE_FOOTER, d.getPageFooter());
		if (d.getLastPageFooter() != null)
			out.put(BandTypeEnum.LAST_PAGE_FOOTER, d.getLastPageFooter());
		if (d.getSummary() != null)
			out.put(BandTypeEnum.SUMMARY, d.getSummary());
		if (d.getBackground() != null)
			out.put(BandTypeEnum.BACKGROUND, d.getBackground());
		if (d.getNoData() != null)
			out.put(BandTypeEnum.NO_DATA, d.getNoData());
		return out;
		
	}
	
	// ---------------------- Expressions ----------------------
	
	private static void analyzeExpressionReferences(JasperDesign d,
		ValidationReport r)
	{
		Set<String> declaredFields = new LinkedHashSet<>();
		for (JRField f : listFromArray(d.getFields()))
			declaredFields.add(f.getName());
		
		Set<String> declaredParams = new LinkedHashSet<>();
		for (JRParameter p : listFromArray(d.getParameters()))
			if (!p.isSystemDefined())
				declaredParams.add(p.getName());
			
		Set<String> declaredVars = new LinkedHashSet<>();
		for (JRVariable v : listFromArray(d.getVariables()))
			if (!v.isSystemDefined())
				declaredVars.add(v.getName());
			
		Set<String> usedFields = new LinkedHashSet<>();
		Set<String> usedParams = new LinkedHashSet<>();
		Set<String> usedVars = new LinkedHashSet<>();
		
		for (JRExpression expr : collectAllExpressions(d))
		{
			String txt = (expr == null) ? null : expr.getText();
			if (txt == null)
				continue;
			findAll(REF_FIELD, txt, usedFields);
			findAll(REF_PARAM, txt, usedParams);
			findAll(REF_VAR, txt, usedVars);
		}
		
		for (String f : usedFields)
			if (!declaredFields.contains(f))
				r.err("REF", "Field used but not declared: " + f);
		for (String p : usedParams)
			if (!declaredParams.contains(p))
				r.err("REF", "Parameter used but not declared: " + p);
		for (String v : usedVars)
			if (!declaredVars.contains(v) && !BUILTIN_VARS.contains(v))
				r.err("REF", "Variable used but not declared: " + v);
			
		Set<String> unusedF = new LinkedHashSet<>(declaredFields);
		unusedF.removeAll(usedFields);
		Set<String> unusedP = new LinkedHashSet<>(declaredParams);
		unusedP.removeAll(usedParams);
		Set<String> unusedV = new LinkedHashSet<>(declaredVars);
		unusedV.removeAll(usedVars);
		
		if (!unusedF.isEmpty())
			r.info("REF", "Unused fields: " + unusedF);
		if (!unusedP.isEmpty())
			r.info("REF", "Unused parameters: " + unusedP);
		if (!unusedV.isEmpty())
			r.info("REF", "Unused variables: " + unusedV);
		
	}
	
	private static void findAll(Pattern p, String text, Set<String> sink)
	{
		Matcher m = p.matcher(text);
		while (m.find())
			sink.add(m.group(1));
		
	}
	
	private static List<JRExpression> collectAllExpressions(JasperDesign d)
	{
		List<JRExpression> out = new ArrayList<>();
		
		for (JRParameter p : listFromArray(d.getParameters()))
		{
			
			if (p instanceof JRDesignParameter)
			{
				JRDesignParameter dp = (JRDesignParameter) p;
				if (dp.getDefaultValueExpression() != null)
					out.add(dp.getDefaultValueExpression());
			}
			
		}
		
		for (JRVariable v : listFromArray(d.getVariables()))
		{
			
			if (v instanceof JRDesignVariable)
			{
				JRDesignVariable dv = (JRDesignVariable) v;
				if (dv.getExpression() != null)
					out.add(dv.getExpression());
				if (dv.getInitialValueExpression() != null)
					out.add(dv.getInitialValueExpression());
			}
			
		}
		
		Map<BandTypeEnum, JRBand> bands = getAllBands(d);
		
		for (JRBand band : bands.values())
		{
			
			if (band instanceof JRDesignBand)
			{
				JRDesignBand db = (JRDesignBand) band;
				List<JRChild> kids = db.getChildren();
				
				if (kids != null)
				{
					for (JRChild child : kids)
						collectExpressionsDeep(child, out);
				}
				
			}
			
		}
		
		for (JRGroup g : listFromArray(d.getGroups()))
		{
			
			if (g instanceof JRDesignGroup)
			{
				JRDesignGroup dg = (JRDesignGroup) g;
				if (dg.getExpression() != null)
					out.add(dg.getExpression());
				JRSection gh = dg.getGroupHeaderSection();
				
				if (gh != null && gh.getBands() != null)
				{
					
					for (JRBand b : gh.getBands())
					{
						
						if (b instanceof JRDesignBand)
						{
							for (JRChild child : ((JRDesignBand) b)
								.getChildren())
								collectExpressionsDeep(child, out);
						}
						
					}
					
				}
				
				JRSection gf = dg.getGroupFooterSection();
				
				if (gf != null && gf.getBands() != null)
				{
					
					for (JRBand b : gf.getBands())
					{
						
						if (b instanceof JRDesignBand)
						{
							for (JRChild child : ((JRDesignBand) b)
								.getChildren())
								collectExpressionsDeep(child, out);
						}
						
					}
					
				}
				
			}
			
		}
		
		if (d.getQuery() != null && d.getQuery().getText() != null)
		{
			out.add(exprOf(d.getQuery().getText()));
		}
		
		for (JRDataset ds : listFromArray(d.getDatasets()))
		{
			
			if (ds instanceof JRDesignDataset)
			{
				JRDesignDataset dsd = (JRDesignDataset) ds;
				
				if (dsd.getQuery() != null && dsd.getQuery().getText() != null)
				{
					out.add(exprOf(dsd.getQuery().getText()));
				}
				
				for (JRParameter p : listFromArray(dsd.getParameters()))
				{
					
					if (p instanceof JRDesignParameter)
					{
						JRDesignParameter dp = (JRDesignParameter) p;
						if (dp.getDefaultValueExpression() != null)
							out.add(dp.getDefaultValueExpression());
					}
					
				}
				
				for (JRVariable v : listFromArray(dsd.getVariables()))
				{
					
					if (v instanceof JRDesignVariable)
					{
						JRDesignVariable dv = (JRDesignVariable) v;
						if (dv.getExpression() != null)
							out.add(dv.getExpression());
						if (dv.getInitialValueExpression() != null)
							out.add(dv.getInitialValueExpression());
					}
					
				}
				
			}
			
		}
		
		return out;
		
	}
	
	private static void collectExpressionsDeep(JRChild child,
		List<JRExpression> sink)
	{
		if (child == null)
			return;
		
		if (child instanceof JRDesignTextField)
		{
			JRDesignTextField tf = (JRDesignTextField) child;
			if (tf.getExpression() != null)
				sink.add(tf.getExpression());
			if (tf.getPatternExpression() != null)
				sink.add(tf.getPatternExpression());
			return;
		}
		
		if (child instanceof JRDesignImage)
		{
			JRDesignImage img = (JRDesignImage) child;
			if (img.getExpression() != null)
				sink.add(img.getExpression());
			if (img.getAnchorNameExpression() != null)
				sink.add(img.getAnchorNameExpression());
			if (img.getHyperlinkReferenceExpression() != null)
				sink.add(img.getHyperlinkReferenceExpression());
			if (img.getHyperlinkAnchorExpression() != null)
				sink.add(img.getHyperlinkAnchorExpression());
			if (img.getHyperlinkPageExpression() != null)
				sink.add(img.getHyperlinkPageExpression());
			if (img.getHyperlinkTooltipExpression() != null)
				sink.add(img.getHyperlinkTooltipExpression());
			return;
		}
		
		if (child instanceof JRDesignSubreport)
		{
			JRDesignSubreport sr = (JRDesignSubreport) child;
			if (sr.getExpression() != null)
				sink.add(sr.getExpression());
			JRExpression maybe =
				getExpressionReflective(sr, "getUsingCacheExpression");
			if (maybe != null)
				sink.add(maybe);
			if (sr.getConnectionExpression() != null)
				sink.add(sr.getConnectionExpression());
			if (sr.getDataSourceExpression() != null)
				sink.add(sr.getDataSourceExpression());
			JRSubreportParameter[] ps = sr.getParameters();
			
			if (ps != null)
			{
				
				for (JRSubreportParameter p : ps)
				{
					
					if (p instanceof JRDesignSubreportParameter)
					{
						JRDesignSubreportParameter dp =
							(JRDesignSubreportParameter) p;
						if (dp.getExpression() != null)
							sink.add(dp.getExpression());
					}
					
				}
				
			}
			
			return;
		}
		
		if (child instanceof JRDesignChart)
		{
			JRDesignChart ch = (JRDesignChart) child;
			if (ch.getTitleExpression() != null)
				sink.add(ch.getTitleExpression());
			if (ch.getSubtitleExpression() != null)
				sink.add(ch.getSubtitleExpression());
			if (ch.getAnchorNameExpression() != null)
				sink.add(ch.getAnchorNameExpression());
			if (ch.getHyperlinkReferenceExpression() != null)
				sink.add(ch.getHyperlinkReferenceExpression());
			if (ch.getHyperlinkAnchorExpression() != null)
				sink.add(ch.getHyperlinkAnchorExpression());
			if (ch.getHyperlinkPageExpression() != null)
				sink.add(ch.getHyperlinkPageExpression());
			if (ch.getHyperlinkTooltipExpression() != null)
				sink.add(ch.getHyperlinkTooltipExpression());
			return;
		}
		
		if (child instanceof JRDesignComponentElement)
		{
			return;
		}
		
	}
	
	private static JRExpression exprOf(String text)
	{
		JRDesignExpression e = new JRDesignExpression();
		e.setText(text);
		return e;
		
	}
	
	// ---------------------- Helpers ----------------------
	
	private static <T extends Throwable> T findCause(Throwable t, Class<T> cls)
	{
		
		while (t != null)
		{
			if (cls.isInstance(t))
				return cls.cast(t);
			t = t.getCause();
		}
		
		return null;
		
	}
	
	private static void printContext(File f, int line, int radius,
		ValidationReport r)
	{
		
		try
		{
			List<String> lines =
				Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
			int start = Math.max(1, line - radius);
			int end = Math.min(lines.size(), line + radius);
			
			for (int i = start; i <= end; i++)
			{
				String prefix = (i == line ? ">> " : "   ");
				r.err("CONTEXT", prefix + i + ": " + lines.get(i - 1));
			}
			
		}
		catch (Exception ignore)
		{
		}
		
	}
	
	private static JRExpression getExpressionReflective(Object obj,
		String methodName)
	{
		
		try
		{
			Method m = obj.getClass().getMethod(methodName);
			Object res = m.invoke(obj);
			return (res instanceof JRExpression) ? (JRExpression) res : null;
		}
		catch (Exception ignore)
		{
			return null;
		}
		
	}
	
	private static <T> List<T> listFromArray(T[] arr)
	{
		if (arr == null || arr.length == 0)
			return java.util.Collections.emptyList();
		return Arrays.asList(arr);
		
	}
	
	private static <T> List<T> listFromCollection(Collection<? extends T> c)
	{
		if (c == null || c.isEmpty())
			return java.util.Collections.emptyList();
		return new ArrayList<>(c);
		
	}
	
	private static int[] extractLineColFromMessages(Throwable t)
	{
		Pattern p = Pattern.compile(
			"line\\s*[:=]\\s*(\\d+)\\s*,\\s*col(?:umn)?\\s*[:=]\\s*(\\d+)",
			Pattern.CASE_INSENSITIVE);
		
		for (Throwable x = t; x != null; x = x.getCause())
		{
			String msg = String.valueOf(x.getMessage());
			if (msg == null)
				continue;
			java.util.regex.Matcher m = p.matcher(msg);
			
			if (m.find())
			{
				
				try
				{
					return new int[]
					{ Integer.parseInt(m.group(1)),
						Integer.parseInt(m.group(2)) };
				}
				catch (NumberFormatException ignore)
				{
				}
				
			}
			
		}
		
		return null;
		
	}
	
	private static String detectJacksonUnrecognizedFieldHint(Throwable t)
	{
		String all = collectMessages(t);
		if (all == null)
			return null;
		java.util.regex.Matcher m = Pattern.compile(
			"Unrecognized field\\s+\"([^\"]+)\".*?\\(through reference chain:\\s*([^\\)]+)\\)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
		).matcher(all);
		
		if (m.find())
		{
			String field = m.group(1);
			String chain = m.group(2).replaceAll("\\s+", " ").trim();
			return "Unrecognized XML element/attribute \"" + field +
				"\" near " + chain +
				". Check for a stray <band>, misspelled tag/attribute, or an invalid node in this section.";
		}
		
		m = Pattern.compile("Unrecognized field\\s+\"([^\"]+)\"",
			Pattern.CASE_INSENSITIVE).matcher(all);
		
		if (m.find())
		{
			String field = m.group(1);
			return "Unrecognized XML element/attribute \"" + field +
				"\". Check tag/attribute spelling and section layout.";
		}
		
		return null;
		
	}
	
	private static String collectMessages(Throwable t)
	{
		StringBuilder sb = new StringBuilder();
		
		for (Throwable x = t; x != null; x = x.getCause())
		{
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(x.getClass().getSimpleName()).append(": ")
				.append(String.valueOf(x.getMessage()));
		}
		
		return sb.toString();
		
	}
	
	private static void dumpCauseChain(ValidationReport r, Throwable t)
	{
		
		for (Throwable x = t.getCause(); x != null; x = x.getCause())
		{
			r.err("CAUSE", x.getClass().getName() + " - " +
				String.valueOf(x.getMessage()));
		}
		
	}
	
	private static void addTrace(ValidationReport r, Throwable t)
	{
		StringBuilder sb = new StringBuilder();
		int max = 12, i = 0;
		
		for (StackTraceElement el : t.getStackTrace())
		{
			sb.append("    at ").append(el.toString()).append('\n');
			if (++i >= max)
				break;
		}
		
		r.err("TRACE", sb.toString().trim());
		
	}
	
	// ---------------------- Reporting ----------------------
	
	private static final class ValidationReport
	{
		private final File file;
		private final boolean failOnWarn;
		private final List<String> infos = new ArrayList<>();
		private final List<String> warns = new ArrayList<>();
		private final List<String> errs = new ArrayList<>();
		
		ValidationReport(File f, boolean failOnWarn)
		{
			this.file = f;
			this.failOnWarn = failOnWarn;
			
		}
		
		void info(String tag, String msg)
		{
			infos.add("[" + tag + "] " + msg);
			
		}
		
		void warn(String tag, String msg)
		{
			warns.add("[" + tag + "] " + msg);
			
		}
		
		void err(String tag, String msg)
		{
			errs.add("[" + tag + "] " + msg);
			
		}
		
		boolean isValid()
		{
			return errs.isEmpty() && (!failOnWarn || warns.isEmpty());
			
		}
		
		void print()
		{
			System.out.println("\n=== " + file.getAbsolutePath() + " ===");
			
			if (!infos.isEmpty())
			{
				System.out.println("Info:");
				infos.forEach(s -> System.out.println("  " + s));
			}
			
			if (!warns.isEmpty())
			{
				System.out.println("Warnings:");
				warns.forEach(s -> System.out.println("  " + s));
			}
			
			if (!errs.isEmpty())
			{
				System.out.println("Errors:");
				errs.forEach(s -> System.out.println("  " + s));
			}
			else if (failOnWarn && !warns.isEmpty())
			{
				System.out.println(
					" No errors, but warnings present (fail-on-warn enabled).");
			}
			else
			{
				System.out.println("Valid");
			}
			
		}
		
	}
	
}
