package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMap;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapLoader;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for report generators that load field maps and JRXML metadata.
 */
public abstract class FieldMappedReportGenerator extends AbstractReportGenerator
{
	private static final Logger LOGGER =
		Logger.getLogger(FieldMappedReportGenerator.class.getName());

	protected FieldMap loadFieldMap(String fieldMapPath)
	{
		if (fieldMapPath == null || fieldMapPath.isBlank())
		{
			return null;
		}

		try
		{
			Path path = Path.of(fieldMapPath);

			if (Files.exists(path))
			{
				return FieldMapLoader.load(path);
			}

			return FieldMapLoader.loadFromResource(getClass(), fieldMapPath);
		}
		catch (Exception ex)
		{
			LOGGER.log(Level.WARNING,
				"Report {0} failed to load field map at {1}: {2}",
				new Object[] { getBaseName(), fieldMapPath, ex.getMessage() });
			return null;
		}
	}

	protected List<JRField> loadJrxmlFields(String jrxmlPath)
	{
		if (jrxmlPath == null || jrxmlPath.isBlank())
		{
			return Collections.emptyList();
		}

		try (InputStream input = openJrxmlStream(jrxmlPath))
		{
			JasperDesign design = JRXmlLoader.load(input);
			return design.getFieldsList();
		}
		catch (IOException | JRException ex)
		{
			LOGGER.log(Level.WARNING,
				"Report {0} failed to load JRXML fields from {1}: {2}",
				new Object[] { getBaseName(), jrxmlPath, ex.getMessage() });
			return Collections.emptyList();
		}
	}

	private InputStream openJrxmlStream(String jrxmlPath) throws IOException
	{
		String resourcePath = jrxmlPath.startsWith("/") ?
			jrxmlPath.substring(1) : jrxmlPath;
		InputStream input =
			getClass().getClassLoader().getResourceAsStream(resourcePath);

		if (input != null)
		{
			return input;
		}

		return new FileInputStream(jrxmlPath);
	}
}
