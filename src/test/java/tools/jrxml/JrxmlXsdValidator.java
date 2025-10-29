/**
 * NonprofitAccounting JrxmlXsdValidator.java JrxmlXsdValidator
 */

package tools.jrxml;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * JRXML -> XSD validator (portable Java, no external deps)
 *
 * Features:
 * - Validate a single .jrxml (--file) or recursively a directory (--dir) against an XSD (--xsd)
 * - Optional conservative normalize pass (--normalize): drop DOCTYPE, add missing <band height="50">
 * - Optional write normalized XML (--write) and/or to a separate output dir (--out)
 * - CSV logging (--csv) with per-issue rows and embedded context lines
 * - Prints per-file issues with (line, col) and surrounding context
 *
 * Build:
 *   javac JrxmlXsdValidator.java
 *
 * Examples:
 *   java JrxmlXsdValidator --xsd jrxml_monolithic_v6_0_corpus_strict.xsd --dir ./jrxml
 *   java JrxmlXsdValidator --xsd schema.xsd --file report.jrxml --context 5 --csv results.csv
 *   java JrxmlXsdValidator --xsd schema.xsd --dir ./jrxml --normalize --write --out ./fixed
 *
 * Exit code: 0 when all files are valid; 2 if any file is invalid or on setup errors.
 */
public class JrxmlXsdValidator
{
	
	// ---------------- CLI ----------------
	
	private static final class Args
	{
		String file;
		String dir;
		String xsd;
		String csv;
		boolean normalize = false;
		boolean write = false;
		String out;
		int context = 3;
		
	}
	
	private static void usageAndExit()
	{
		System.err.println("Usage:");
		System.err.println(
			"  java JrxmlXsdValidator --xsd schema.xsd (--file path.jrxml | --dir dir) [--csv out.csv] [--normalize] [--write] [--out outDir] [--context N]");
		System.exit(2);
		
	}
	
	private static Args parseArgs(String[] argv)
	{
		Args a = new Args();
		
		for (int i = 0; i < argv.length; i++)
		{
			
			switch(argv[i])
			{
				case "--file":
					a.file = (++i < argv.length) ? argv[i] : null;
					break;
				
				case "--dir":
					a.dir = (++i < argv.length) ? argv[i] : null;
					break;
				
				case "--xsd":
					a.xsd = (++i < argv.length) ? argv[i] : null;
					break;
				
				case "--csv":
					a.csv = (++i < argv.length) ? argv[i] : null;
					break;
				
				case "--normalize":
					a.normalize = true;
					break;
				
				case "--write":
					a.write = true;
					break;
				
				case "--out":
					a.out = (++i < argv.length) ? argv[i] : null;
					break;
				
				case "--context":
					try
					{
						a.context = Integer.parseInt(argv[++i]);
					}
					catch (Exception ignore)
					{
					}
					break;
				
				default:
					System.err.println("Unknown arg: " + argv[i]);
					usageAndExit();
			}
			
		}
		
		if (a.xsd == null || (a.file == null && a.dir == null))
			usageAndExit();
		if (a.out != null)
			a.write = true; // --out implies --write
		return a;
		
	}
	
	// ---------------- Model ----------------
	
	private static final class Issue
	{
		final String severity; // ERROR | WARNING | INFO
		final String code; // e.g., XSD:SCHEMAV_CVC_... or XML, FIX, RUNTIME
		final String message;
		final Integer line;
		final Integer column;
		final List<String> context;
		
		Issue(String severity, String code, String message, Integer line,
			Integer column, List<String> context)
		{
			this.severity = severity;
			this.code = code;
			this.message = message;
			this.line = line;
			this.column = column;
			this.context = context == null ? Collections.emptyList() : context;
			
		}
		
	}
	
	private static final class FileReport
	{
		final Path path;
		final List<Issue> issues = new ArrayList<>();
		
		FileReport(Path p)
		{
			this.path = p;
			
		}
		
		void add(String sev, String code, String msg, Integer line, Integer col,
			List<String> ctx)
		{
			issues.add(new Issue(sev, code, msg, line, col, ctx));
			
		}
		
		boolean isValid()
		{
			for (Issue i : issues)
				if ("ERROR".equals(i.severity))
					return false;
			return true;
			
		}
		
	}
	
	// ---------------- Utilities ----------------
	
	private static List<Path> gatherFiles(String file, String dir)
		throws IOException
	{
		List<Path> out = new ArrayList<>();
		
		if (file != null)
		{
			Path p = Paths.get(file);
			
			if (Files.isRegularFile(p) &&
				p.toString().toLowerCase().endsWith(".jrxml"))
			{
				out.add(p.toAbsolutePath().normalize());
			}
			
		}
		else
		{
			Path root = Paths.get(dir);
			Files.walk(root)
				.filter(p -> Files.isRegularFile(p) &&
					p.toString().toLowerCase().endsWith(".jrxml"))
				.forEach(p -> out.add(p.toAbsolutePath().normalize()));
		}
		
		out.sort(Comparator.naturalOrder());
		return out;
		
	}
	
	private static List<String> readLines(Path p)
	{
		
		try
		{
			return Files.readAllLines(p, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			
			try
			{
				byte[] b = Files.readAllBytes(p);
				return Arrays.asList(
					new String(b, StandardCharsets.UTF_8).split("\\R", -1));
			}
			catch (Exception ex)
			{
				return Collections
					.singletonList("<unable to read file content>");
			}
			
		}
		
	}
	
	private static List<String> contextLines(List<String> lines, int line,
		int radius)
	{
		if (line <= 0)
			return Collections.emptyList();
		int start = Math.max(1, line - radius);
		int end = Math.min(lines.size(), line + radius);
		List<String> out = new ArrayList<>(end - start + 1);
		
		for (int i = start; i <= end; i++)
		{
			String prefix = (i == line) ? ">> " : "   ";
			out.add(String.format("%s%6d: %s", prefix, i, lines.get(i - 1)));
		}
		
		return out;
		
	}
	
	private static String localName(Node n)
	{
		String ln = n.getLocalName();
		return (ln != null) ? ln : n.getNodeName();
		
	}
	
	// ---------------- DOM Parse / Write ----------------
	
	private static DocumentBuilder newSecureDocumentBuilder() throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbf.setNamespaceAware(true);
		
		// prevent external entity resolution
		try
		{
			dbf.setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", false); // allow
																				// reading
																				// but
																				// we
																				// may
																				// drop
																				// in
																				// normalize
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		}
		catch (Throwable ignore)
		{
		}
		
		return dbf.newDocumentBuilder();
		
	}
	
	private static void writeDom(Document dom, Path target) throws Exception
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		
		try
		{
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
		catch (Throwable ignore)
		{
		}
		
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		t.transform(new DOMSource(dom),
			new StreamResult(Files.newOutputStream(target)));
		
	}
	
	// ---------------- Normalize ----------------
	
	private static List<String> normalizeDom(Document dom)
	{
		List<String> actions = new ArrayList<>();
		
		// 1) Drop DOCTYPE if present
		if (dom.getDoctype() != null)
		{
			
			try
			{
				dom.removeChild(dom.getDoctype());
				actions.add("removed DOCTYPE");
			}
			catch (Throwable ignore)
			{
				// If remove fails (implementation-specific), skip
			}
			
		}
		
		// 2) Ensure each <band> has @height (default 50)
		Element root = dom.getDocumentElement();
		
		if (root != null)
		{
			NodeList all = root.getElementsByTagNameNS("*", "band");
			
			if (all.getLength() == 0)
			{
				// try non-namespace
				all = root.getElementsByTagName("band");
			}
			
			for (int i = 0; i < all.getLength(); i++)
			{
				Element b = (Element) all.item(i);
				
				if (!b.hasAttribute("height"))
				{
					b.setAttribute("height", "50");
					actions.add("added default @height='50' on <band>");
				}
				
			}
			
		}
		
		return actions;
		
	}
	
	// ---------------- Schema ----------------
	
	private static Schema loadSchema(Path xsd) throws Exception
	{
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
		
		return sf.newSchema(xsd.toFile());
		
	}
	
	private static final class CollectingErrorHandler implements ErrorHandler
	{
		final List<SAXParseException> warnings = new ArrayList<>();
		final List<SAXParseException> errors = new ArrayList<>();
		final List<SAXParseException> fatals = new ArrayList<>();
		
		@Override
		public void warning(SAXParseException e)
		{
			warnings.add(e);
			
		}
		
		@Override
		public void error(SAXParseException e)
		{
			errors.add(e);
			
		}
		
		@Override
		public void fatalError(SAXParseException e)
		{
			fatals.add(e);
			
		}
		
		boolean hasErrors()
		{
			return !errors.isEmpty() || !fatals.isEmpty();
			
		}
		
	}
	
	private static FileReport validateOne(Path file, Schema schema,
		int contextRadius, boolean doNormalize,
		boolean write, Path outDir)
	{
		FileReport rep = new FileReport(file);
		List<String> lines = readLines(file);
		
		try
		{
			DocumentBuilder db = newSecureDocumentBuilder();
			Document dom = db.parse(file.toFile());
			
			if (doNormalize)
			{
				List<String> actions = normalizeDom(dom);
				
				if (!actions.isEmpty())
				{
					rep.add("INFO", "FIX", "Normalize actions: " + actions,
						null, null, Collections.emptyList());
				}
				
				if (write)
				{
					Path target =
						(outDir != null) ? outDir.resolve(file.getFileName())
							.toAbsolutePath().normalize() : file;
					if (outDir != null)
						Files.createDirectories(target.getParent());
					writeDom(dom, target);
					rep.add("INFO", "FIX", "Wrote normalized file: " + target,
						null, null, null);
				}
				
			}
			
			Validator v = schema.newValidator();
			v.setErrorHandler(new CollectingErrorHandler()); // We also catch
																// thrown
																// exceptions
																// below
			CollectingErrorHandler eh = new CollectingErrorHandler();
			v.setErrorHandler(eh);
			
			try
			{
				v.validate(new DOMSource(dom));
			}
			catch (SAXParseException ex)
			{
				// fatal errors may be thrown immediately; still collect from eh
				List<String> ctx =
					contextLines(lines, ex.getLineNumber(), contextRadius);
				String code = "XSD";
				rep.add("ERROR", code, ex.getMessage(), ex.getLineNumber(),
					ex.getColumnNumber(), ctx);
			}
			
			// Emit collected warnings/errors/fatals
			for (SAXParseException w : eh.warnings)
			{
				List<String> ctx =
					contextLines(lines, w.getLineNumber(), contextRadius);
				rep.add("WARNING", "XSD:" + safeType(w), w.getMessage(),
					w.getLineNumber(), w.getColumnNumber(), ctx);
			}
			
			for (SAXParseException e : eh.errors)
			{
				List<String> ctx =
					contextLines(lines, e.getLineNumber(), contextRadius);
				rep.add("ERROR", "XSD:" + safeType(e), e.getMessage(),
					e.getLineNumber(), e.getColumnNumber(), ctx);
			}
			
			for (SAXParseException f : eh.fatals)
			{
				List<String> ctx =
					contextLines(lines, f.getLineNumber(), contextRadius);
				rep.add("ERROR", "XSD:" + safeType(f), f.getMessage(),
					f.getLineNumber(), f.getColumnNumber(), ctx);
			}
			
			if (!eh.hasErrors() &&
				rep.issues.stream().noneMatch(i -> "ERROR".equals(i.severity)))
			{
				rep.add("INFO", "XSD", "Schema validation OK", null, null,
					null);
			}
			
		}
		catch (SAXParseException ex)
		{
			List<String> ctx =
				contextLines(lines, ex.getLineNumber(), contextRadius);
			rep.add("ERROR", "XML", ex.getMessage(), ex.getLineNumber(),
				ex.getColumnNumber(), ctx);
		}
		catch (Exception ex)
		{
			rep.add("ERROR", "RUNTIME", ex.getClass().getSimpleName() + ": " +
				String.valueOf(ex.getMessage()), null, null, null);
		}
		
		return rep;
		
	}
	
	private static String safeType(SAXParseException e)
	{
		// SAXParseException doesn't always expose type; keep minimal code label
		return "SCHEMA";
		
	}
	
	// ---------------- CSV ----------------
	
	private static void writeCsv(Path csvPath, List<FileReport> reports)
	{
		
		try
		{
			if (csvPath.getParent() != null)
				Files.createDirectories(csvPath.getParent());
			
			try (
				BufferedWriter w =
					Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8);
				PrintWriter pw = new PrintWriter(w))
			{
				pw.println("file,severity,code,line,column,message,context");
				
				for (FileReport rep : reports)
				{
					
					if (rep.issues.isEmpty())
					{
						pw.printf("%s,INFO,NONE,,,No issues,\n",
							escapeCsv(rep.path.toString()));
					}
					else
					{
						
						for (Issue i : rep.issues)
						{
							String ctx = String.join("\\n", i.context);
							pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
								escapeCsv(rep.path.toString()),
								escapeCsv(i.severity),
								escapeCsv(i.code),
								i.line == null ? "" : i.line,
								i.column == null ? "" : i.column,
								escapeCsv(i.message),
								escapeCsv(ctx));
						}
						
					}
					
				}
				
			}
			
			System.out.println("Wrote CSV: " + csvPath.toAbsolutePath());
		}
		catch (Exception e)
		{
			System.err.println("CSV write failed: " + e.getMessage());
		}
		
	}
	
	private static String escapeCsv(String s)
	{
		if (s == null)
			return "";
		
		boolean needsQuotes = s.contains(",") || s.contains("\"") ||
			s.contains("\n") || s.contains("\r");
		
		String t = s.replace("\"", "\"\"");
		return needsQuotes ? "\"" + t + "\"" : t;
		
	}
	
	// ---------------- Main ----------------
	
	public static void xsdmain(String[] args)
	{
		Args a = parseArgs(args);
		
		final Path xsdPath = Paths.get(a.xsd).toAbsolutePath().normalize();
		
		if (!Files.isRegularFile(xsdPath))
		{
			System.err.println("XSD not found: " + xsdPath);
			System.exit(2);
		}
		
		Schema schema;
		
		try
		{
			schema = loadSchema(xsdPath);
		}
		catch (Exception e)
		{
			System.err.println("Failed to load XSD: " + e.getMessage());
			System.exit(2);
			return;
		}
		
		final List<Path> files;
		
		try
		{
			files = gatherFiles(a.file, a.dir);
		}
		catch (IOException e)
		{
			System.err.println("Failed to gather files: " + e.getMessage());
			System.exit(2);
			return;
		}
		
		if (files.isEmpty())
		{
			System.err.println("No .jrxml files found.");
			System.exit(2);
		}
		
		Path outDir = (a.out != null) ?
			Paths.get(a.out).toAbsolutePath().normalize() : null;
		
		if (outDir != null)
		{
			
			try
			{
				Files.createDirectories(outDir);
			}
			catch (IOException ignore)
			{
			}
			
		}
		
		List<FileReport> reports = new ArrayList<>();
		int invalid = 0;
		
		for (Path p : files)
		{
			FileReport rep =
				validateOne(p, schema, a.context, a.normalize, a.write, outDir);
			reports.add(rep);
			
			System.out.println("\n=== " + p + " ===");
			
			if (rep.issues.isEmpty())
			{
				System.out.println("  Valid (no issues)");
			}
			else
			{
				
				for (Issue i : rep.issues)
				{
					String loc =
						(i.line != null) ? String.format(" (line %d, col %d)",
							i.line, (i.column == null ? 0 : i.column)) : "";
					System.out.println("  [" + i.severity + "] " + i.code +
						": " + i.message + loc);
					
					if (i.context != null && !i.context.isEmpty())
					{
						System.out.println("  -- context --");
						for (String ln : i.context)
							System.out.println("  " + ln);
					}
					
				}
				
			}
			
			if (!rep.isValid())
				invalid++;
		}
		
		if (a.csv != null)
			writeCsv(Paths.get(a.csv), reports);
		
		int total = reports.size();
		int ok = total - invalid;
		System.out.printf("%nSummary: %d/%d valid, %d invalid.%n", ok, total,
			invalid);
		System.exit(invalid == 0 ? 0 : 2);
		
	}
	
}
