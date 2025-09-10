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
 * JRXML validator:
 * 1) XML well-formedness (DOM parse; no network)
 * 2) Jasper digestion via JRXmlLoader.load(...) to surface JRException causes
 * 3) Lints (duplicate names, illegal identifiers, empty bands, class_/CDATA pitfalls)
 * 4) Expression reference analysis: finds undeclared $F/$P/$V and unused declarations
 *
 * Usage:
 *   java tools.jrxml.JrxmlValidator --file path/to/report.jrxml [--fail-on-warn]
 *   java tools.jrxml.JrxmlValidator --dir  path/to/reports       [--fail-on-warn]
 *
 * Exit codes: 0 = OK, 2 = any failures (or warnings if --fail-on-warn)
 */
public class JrxmlValidator
{

	// ---------------------- CLI ----------------------

	public static void main(String[] args) throws Exception
	{
		Map<String, String> cli = parseArgs(args);

		if (cli.isEmpty() ||
			(!cli.containsKey("file") && !cli.containsKey("dir")))
		{
			printHelp();
			System.exit(1);
		}

		boolean failOnWarn = cli.containsKey("fail-on-warn");
		List<File> files = collectFiles(cli);

		int ok = 0, fail = 0;

		// === PATCH: per-file crash logging & continue ===
		for (File f : files)
		{
			try
			{
				ValidationReport r = validateOne(f, failOnWarn);
				r.print();
				if (r.isValid())
					ok++;
				else
					fail++;
			}
			catch (Throwable t)
			{
				ValidationReport cr = new ValidationReport(f, failOnWarn);
				cr.err("CRASH", t.getClass().getName() + " - " + String.valueOf(t.getMessage()));

				for (Throwable c = t.getCause(); c != null; c = c.getCause())
				{
					cr.err("CAUSE", c.getClass().getName() + " - " + String.valueOf(c.getMessage()));
				}

				SAXParseException sax = findCause(t, SAXParseException.class);
				if (sax != null)
				{
					cr.err("XML", String.format("%s (line %d, col %d)",
						sax.getMessage(), sax.getLineNumber(), sax.getColumnNumber()));
					printContext(f, sax.getLineNumber(), 3, cr);
				}

				cr.print();
				fail++;
				// continue to next file
			}
		}
		// === END PATCH ===

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
			}

		}

		return m;

	}

	private static void printHelp()
	{
		System.out.println("JRXML Validator");
		System.out.println("Usage:");
		System.out.println(
			"  java tools.jrxml.JrxmlValidator --file path/to/report.jrxml [--fail-on-warn]");
		System.out.println(
			"  java tools.jrxml.JrxmlValidator --dir  path/to/reports       [--fail-on-warn]");

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

	// Expressions: $F{name}, $P{name}, $V{name}
	private static final Pattern REF_FIELD = Pattern.compile("\\$F\\{([^}]+)}");
	private static final Pattern REF_PARAM = Pattern.compile("\\$P\\{([^}]+)}");
	private static final Pattern REF_VAR = Pattern.compile("\\$V\\{([^}]+)}");

	// Jasper built-in variables (partial set; extend if needed)
	private static final Set<String> BUILTIN_VARS = new HashSet<>(Arrays.asList(
		"PAGE_NUMBER", "COLUMN_NUMBER", "REPORT_COUNT", "PAGE_COUNT",
		"COLUMN_COUNT",
		"MASTER_CURRENT_PAGE", "MASTER_TOTAL_PAGES", "MASTER_CURRENT_COLUMN",
		"MASTER_TOTAL_COLUMNS",
		"IS_IGNORE_PAGINATION"
	));

	private static ValidationReport validateOne(File jrxml, boolean failOnWarn)
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

		// -------- 1.5) DOM-only structural scan BEFORE Jasper load --------
		preflightStructuralChecks(dom, xmlText, report);

		// -------- 2) Jasper digestion (JRXmlLoader.load) --------
		JasperDesign design;

		try
		{
			design = JRXmlLoader.load(jrxml);
			report.info("JASPER",
				"JRXmlLoader.load OK (JasperDesign created).");
		}
		catch (JRException e)
		{
			report.err("JASPER", "JRXmlLoader.load failed: " + e.getMessage());

			// 1) Pull the real XML parser location if available
			SAXParseException sax = findCause(e, SAXParseException.class);

			if (sax != null)
			{
				report.err("XML",
					String.format("%s (line %d, col %d)",
						sax.getMessage(), sax.getLineNumber(),
						sax.getColumnNumber()));
				// 2) Show ±3 lines of context so you can see the bad char/entity
				printContext(jrxml, sax.getLineNumber(), 3, report);
				return report;
			}

			// 3) Otherwise dump the cause chain so we see the concrete reason
			for (Throwable t = e.getCause(); t != null; t = t.getCause())
			{
				report.err("CAUSE", t.getClass().getName() + " - " +
					String.valueOf(t.getMessage()));
			}

			return report;
		}

		// -------- 3) Lint: duplicate / illegal names; empty bands --------
		lintNamedDeclarations(design, report);
		lintEmptyBands(design, report);

		// -------- 4) Expression reference analysis ($F, $P, $V) --------
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

	// ---------------------- DOM-only preflight (no Jasper) ----------------------

	private static void preflightStructuralChecks(Document dom, String xmlText, ValidationReport r)
	{
		Element root = dom.getDocumentElement();

		// Sections that must have exactly one direct <band> if present
		String[] singleBandSections = {
			"background","title","pageHeader","columnHeader",
			"columnFooter","pageFooter","lastPageFooter","summary","noData"
		};
		for (String secName : singleBandSections)
		{
			NodeList secs = root.getElementsByTagName(secName);
			for (int i = 0; i < secs.getLength(); i++)
			{
				Element sec = (Element) secs.item(i);
				int directBands = countDirectChildren(sec, "band");
				if (directBands == 0)
				{
					r.err("STRUCT", "<" + secName + "> has 0 <band> children (expected 1).");
				}
				else if (directBands > 1)
				{
					r.err("STRUCT", "<" + secName + "> has " + directBands + " <band> children (expected 1).");
				}
				// check nested bands
				NodeList bands = sec.getElementsByTagName("band");
				for (int b = 0; b < bands.getLength(); b++)
				{
					Element band = (Element) bands.item(b);
					if (hasDescendantTag(band, "band"))
					{
						r.err("STRUCT", "Nested <band> found under <" + secName + ">; bands cannot contain <band> descendants.");
					}
				}
			}
		}

		// <detail>: allow multiple direct <band>, but no nested <band> inside a band
		NodeList details = root.getElementsByTagName("detail");
		for (int i = 0; i < details.getLength(); i++)
		{
			Element det = (Element) details.item(i);
			NodeList bands = det.getElementsByTagName("band");
			for (int b = 0; b < bands.getLength(); b++)
			{
				Element band = (Element) bands.item(b);
				if (hasDescendantTag(band, "band"))
				{
					r.err("STRUCT", "Nested <band> found under <detail>; bands cannot contain <band> descendants.");
				}
			}
		}

		// Groups header/footer sections: check for nested bands
		NodeList groups = root.getElementsByTagName("group");
		for (int i = 0; i < groups.getLength(); i++)
		{
			Element g = (Element) groups.item(i);
			Element gh = firstChildElement(g, "groupHeader");
			if (gh != null) checkNoNestedBandsInSection("groupHeader", gh, r);
			Element gf = firstChildElement(g, "groupFooter");
			if (gf != null) checkNoNestedBandsInSection("groupFooter", gf, r);
		}

		// Escaped CDATA markers
		if (xmlText.contains("&lt;![CDATA[") || xmlText.contains("]]&gt;"))
		{
			r.err("LINT", "Escaped CDATA markers (&lt;![CDATA[ or ]]&gt;) found. Replace with real CDATA blocks.");
		}

		// Aggressive scan for unescaped '&' in <text> or *Expression nodes
		Pattern rawAmp = Pattern.compile(
			"(?s)<(text|[A-Za-z0-9:_-]*Expression)(?:[^>]*)>[^<]*&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9a-fA-F]+;)[^<]*</\\1>"
		);
		if (rawAmp.matcher(xmlText).find())
		{
			r.warn("LINT", "Likely unescaped '&' in <text> or *Expression content. Escape as &amp; or wrap in CDATA.");
		}
	}

	private static void checkNoNestedBandsInSection(String secName, Element section, ValidationReport r)
	{
		NodeList bands = section.getElementsByTagName("band");
		for (int b = 0; b < bands.getLength(); b++)
		{
			Element band = (Element) bands.item(b);
			if (hasDescendantTag(band, "band"))
			{
				r.err("STRUCT", "Nested <band> found under <" + secName + ">; bands cannot contain <band> descendants.");
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
			if (n.getNodeType() == Node.ELEMENT_NODE && tag.equals(n.getNodeName()))
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
			if (n.getNodeType() == Node.ELEMENT_NODE && tag.equals(n.getNodeName()))
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
		// Fields
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

		// Parameters
		Set<String> params = new LinkedHashSet<>();

		for (JRParameter p : listFromArray(d.getParameters()))
		{
			if (p.isSystemDefined())
				continue;
			checkName("parameter", p.getName(), r);
			if (!params.add(p.getName()))
				r.err("LINT", "Duplicate parameter: " + p.getName());
		}

		// Variables
		Set<String> vars = new LinkedHashSet<>();

		for (JRVariable v : listFromArray(d.getVariables()))
		{
			if (v.isSystemDefined())
				continue;
			checkName("variable", v.getName(), r);
			if (!vars.add(v.getName()))
				r.err("LINT", "Duplicate variable: " + v.getName());
		}

		// Styles
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
				List<JRChild> kids = db.getChildren(); // JR 7.x API

				if (kids == null || kids.isEmpty())
				{
					r.warn("STRUCT", "Empty band: " + e.getKey() + " (height=" +
						db.getHeight() + ")");
				}

			}
			else
			{
				// Non-design implementations may not expose children; skip
				// warning.
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
		// Declared sets
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

		// Referenced sets (found in expressions)
		Set<String> usedFields = new LinkedHashSet<>();
		Set<String> usedParams = new LinkedHashSet<>();
		Set<String> usedVars = new LinkedHashSet<>();

		// Walk all expressions in the design
		for (JRExpression expr : collectAllExpressions(d))
		{
			String txt = (expr == null) ? null : expr.getText();
			if (txt == null)
				continue;
			findAll(REF_FIELD, txt, usedFields);
			findAll(REF_PARAM, txt, usedParams);
			findAll(REF_VAR, txt, usedVars);
		}

		// Missing declarations
		for (String f : usedFields)
			if (!declaredFields.contains(f))
				r.err("REF", "Field used but not declared: " + f);
		for (String p : usedParams)
			if (!declaredParams.contains(p))
				r.err("REF", "Parameter used but not declared: " + p);

		for (String v : usedVars)
		{

			if (!declaredVars.contains(v) && !BUILTIN_VARS.contains(v))
			{
				r.err("REF", "Variable used but not declared: " + v);
			}

		}

		// Unused declarations (informational)
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

		// Report-level parameters / variables
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

		// Bands & elements
		Map<BandTypeEnum, JRBand> bands = getAllBands(d);

		for (JRBand band : bands.values())
		{

			if (band instanceof JRDesignBand)
			{
				JRDesignBand db = (JRDesignBand) band;
				List<JRChild> kids = db.getChildren(); // JR 7.x API

				if (kids != null)
				{
					for (JRChild child : kids)
						collectExpressionsDeep(child, out);
				}

			}

		}

		// Groups (sections contain bands)
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

		// Main query
		if (d.getQuery() != null && d.getQuery().getText() != null)
		{
			out.add(exprOf(d.getQuery().getText()));
		}

		// Subdatasets
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

	/**
	 * Collect expressions from common elements (extend as needed).
	 * Accepts JRChild because JR 7.x bands expose List<JRChild>.
	 */
	private static void collectExpressionsDeep(JRChild child,
		List<JRExpression> sink)
	{
		if (child == null)
			return;

		// Most design-time elements also implement JRChild
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

			// JR 6.x had getUsingCacheExpression(); JR 7.x typically doesn't.
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
			// Many components carry expressions internally; not all exposed
			// directly.
			// Let JR compile-time handle specifics; no-op here.
			return;
		}

		// Other child types (lines, rectangles, breaks, static texts) either
		// don't carry expressions
		// or aren't relevant to reference analysis.
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
			// best-effort; ignore failures reading context
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
