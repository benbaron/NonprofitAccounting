package nonprofitbookkeeping.reports.excel;

import nonprofitbookkeeping.reports.jasper.runtime.FieldMap;
import nonprofitbookkeeping.reports.jasper.runtime.FieldMapLoader;
import nonprofitbookkeeping.reports.jasper.runtime.ReportDataFetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a single workbook page using a field translation map and report data.
 */
public class ExcelWorkbookPageReportService
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(ExcelWorkbookPageReportService.class);

	/**
	 * Opens the template workbook, queries data for a page, fills a bean, writes
	 * the mapped cells, and saves the output workbook.
	 *
	 * @param templateFile the input XLSX template
	 * @param outputFile the destination XLSX file
	 * @param sheetName the worksheet to populate
	 * @param fieldMapResource classpath resource for the field map CSV
	 * @param beanClass bean class matching the worksheet
	 * @param sql SQL used to fetch data for the bean
	 * @return the output file
	 * @param <B> bean type
	 * @throws IOException when reading or writing fails
	 */
	public <B> File populateWorkbookPage(
		File templateFile,
		File outputFile,
		String sheetName,
		String fieldMapResource,
		Class<B> beanClass,
		String sql
	) throws IOException
	{
		return populateWorkbookPage(
			templateFile,
			outputFile,
			sheetName,
			fieldMapResource,
			beanClass,
			sql,
			null
		);
	}

	/**
	 * Opens the template workbook, queries data for a page, fills a bean, writes
	 * the mapped cells, and saves the output workbook.
	 *
	 * @param templateFile the input XLSX template
	 * @param outputFile the destination XLSX file
	 * @param sheetName the worksheet to populate
	 * @param fieldMapResource classpath resource for the field map CSV
	 * @param beanClass bean class matching the worksheet
	 * @param sql SQL used to fetch data for the bean
	 * @param emptyBeanSupplier optional supplier for an empty bean if no rows are returned
	 * @return the output file
	 * @param <B> bean type
	 * @throws IOException when reading or writing fails
	 */
	public <B> File populateWorkbookPage(
		File templateFile,
		File outputFile,
		String sheetName,
		String fieldMapResource,
		Class<B> beanClass,
		String sql,
		Supplier<B> emptyBeanSupplier
	) throws IOException
	{
		if (templateFile == null || !templateFile.exists())
		{
			throw new IOException("Template file does not exist: " + templateFile);
		}
		if (outputFile == null)
		{
			throw new IOException("Output file is required.");
		}
		if (sheetName == null || sheetName.isBlank())
		{
			throw new IOException("Sheet name is required.");
		}
		if (fieldMapResource == null || fieldMapResource.isBlank())
		{
			throw new IOException("Field map resource is required.");
		}
		if (beanClass == null)
		{
			throw new IOException("Bean class is required.");
		}
		if (sql == null || sql.isBlank())
		{
			throw new IOException("SQL is required.");
		}

		LOGGER.info("Opening workbook template {}.", templateFile);
		FieldMap fieldMap = FieldMapLoader.loadFromResource(fieldMapResource);

		LOGGER.info("Querying report data for sheet {}.", sheetName);
		List<B> beans = ReportDataFetcher.queryRowBasedBeans(beanClass, sql);
		B bean = resolveBean(beanClass, beans, emptyBeanSupplier);

		try (FileInputStream in = new FileInputStream(templateFile);
			Workbook workbook = WorkbookFactory.create(in))
		{
			LOGGER.info("Populating sheet {} with mapped values.", sheetName);
			ExcelTemplateWriter writer = new ExcelTemplateWriter();
			writer.writeTemplate(workbook, fieldMap, bean, sheetName, false);
			try (FileOutputStream out = new FileOutputStream(outputFile))
			{
				workbook.write(out);
			}
		}

		LOGGER.info("Workbook page report complete: {}", outputFile);
		return outputFile;
	}

	private <B> B resolveBean(Class<B> beanClass, List<B> beans,
		Supplier<B> emptyBeanSupplier)
	{
		if (beans != null && !beans.isEmpty())
		{
			return beans.get(0);
		}

		if (emptyBeanSupplier != null)
		{
			return emptyBeanSupplier.get();
		}

		try
		{
			LOGGER.info(
				"No rows returned for {}. Creating empty bean instance.",
				beanClass.getName());
			return beanClass.getDeclaredConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException
			| InvocationTargetException | NoSuchMethodException ex)
		{
			LOGGER.warn(
				"Unable to instantiate bean for {}. Skipping template writes.",
				beanClass.getName(), ex);
			return null;
		}
	}
}
