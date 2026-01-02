
package nonprofitbookkeeping.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContextAware;
import nonprofitbookkeeping.reports.jasper.BundledTemplateJasperGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Utility that instantiates and interacts with Jasper report generator classes using reflection.
 * The report generators are discovered at runtime by class name, allowing the service to function
 * even when the generator implementations live outside the current compilation unit.
 */
final class ReportGeneratorLoader
{
	private static final Logger LOGGER =
		Logger.getLogger(ReportGeneratorLoader.class.getName());

	private ReportGeneratorLoader()
	{
		
	}
	
	/**
	 * Instantiates a report generator by trying common constructor signatures. If the class
	 * cannot be found on the classpath, the method falls back to loading a bundled Jasper
	 * template generator using {@link ReportBundles#bundleForGenerator(String)}.
	 *
	 * @param className fully qualified generator class name or bundled identifier
	 * @param context   report context to pass into the generator when supported
	 * @param service   calling {@link ReportService}; supplied to constructors that accept it
	 * @return a new generator instance capable of producing reports
	 * @throws IllegalStateException when no usable constructor exists or instantiation fails
	 */
	static Object instantiate(String className,
		ReportContext context,
		ReportService service)
	{
		Objects.requireNonNull(className, "className");
		LOGGER.fine(() -> "Resolving report generator for class: " +
			className);
		
		try
		{
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = findConstructor(clazz,
				ReportContext.class, ReportService.class);
			
			if (ctor != null)
			{
				LOGGER.fine(() -> "Instantiating report generator " +
					className +
					" using (ReportContext, ReportService) constructor.");
				Object instance = ctor.newInstance(context, service);
				assignContext(instance, context);
				return instance;
			}
			
			ctor = findConstructor(clazz, ReportContext.class);
			
			if (ctor != null)
			{
				LOGGER.fine(() -> "Instantiating report generator " +
					className + " using (ReportContext) constructor.");
				Object instance = ctor.newInstance(context);
				assignContext(instance, context);
				return instance;
			}
			
			ctor = findConstructor(clazz, ReportService.class);
			
			if (ctor != null)
			{
				LOGGER.fine(() -> "Instantiating report generator " +
					className + " using (ReportService) constructor.");
				Object instance = ctor.newInstance(service);
				assignContext(instance, context);
				return instance;
			}
			
			ctor = findConstructor(clazz);
			
			if (ctor != null)
			{
				LOGGER.fine(() -> "Instantiating report generator " +
					className + " using no-arg constructor.");
				Object instance = ctor.newInstance();
				assignContext(instance, context);
				return instance;
			}
			
			for (Constructor<?> candidate : clazz.getDeclaredConstructors())
			{
				
				if (hasPrimitiveParameters(candidate))
				{
					continue;
				}
				
				candidate.setAccessible(true);
				Object[] args = new Object[candidate.getParameterCount()];
				Arrays.fill(args, null);
				
				try
				{
					LOGGER.fine(() -> "Instantiating report generator " +
						className +
						" using fallback constructor with nulls.");
					Object instance = candidate.newInstance(args);
					assignContext(instance, context);
					return instance;
				}
				catch (InvocationTargetException | InstantiationException |
					IllegalAccessException e)
				{
					// try next constructor
				}
				
			}
			
			throw new IllegalStateException(
				"No suitable constructor for " + className);
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.fine(() -> "Report generator class not found for " +
				className +
				"; attempting bundled template fallback.");
			ReportBundles.Bundle bundle = ReportBundles
				.bundleForGenerator(className);
			
			Object instance =
				new BundledTemplateJasperGenerator(bundle, context);
			assignContext(instance, context);
			return instance;
		}
		catch (InvocationTargetException | InstantiationException |
			IllegalAccessException e)
		{
			throw new IllegalStateException(
				"Unable to instantiate generator: " + className, e);
		}
		
	}
	
	private static void assignContext(Object instance, ReportContext context)
	{
		
		if (instance instanceof ReportContextAware aware)
		{
			aware.setReportContext(context);
		}
		
	}
	
	/**
	 * Attempts to retrieve a declared constructor matching the provided signature and makes it
	 * accessible for reflective invocation.
	 *
	 * @param clazz      class to inspect
	 * @param signature  ordered parameter types describing the desired constructor
	 * @return matching {@link Constructor} or {@code null} when none exists
	 */
	private static Constructor<?> findConstructor(Class<?> clazz,
		Class<?>... signature)
	{
		
		try
		{
			Constructor<?> ctor = clazz.getDeclaredConstructor(signature);
			ctor.setAccessible(true);
			return ctor;
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
		
	}
	
	/**
	 * Determines whether the constructor requires primitive arguments. Primitive parameters
	 * cannot be satisfied with {@code null} values during the best-effort instantiation pass
	 * and therefore disqualify the constructor from consideration.
	 *
	 * @param ctor constructor to inspect
	 * @return {@code true} when any parameter is primitive; {@code false} otherwise
	 */
	private static boolean hasPrimitiveParameters(Constructor<?> ctor)
	{
		
		for (Class<?> param : ctor.getParameterTypes())
		{
			
			if (param.isPrimitive())
			{
				return true;
			}
			
		}
		
		return false;
		
	}
	
	/**
	 * Passes the provided bean collection to a generator that advertises a
	 * {@code setReportData(List)} method. Generators that do not implement the method are
	 * silently ignored.
	 *
	 * @param generator generator instance created by {@link #instantiate(String, ReportContext, ReportService)}
	 * @param beans     domain objects to supply as report data; ignored when {@code null} or empty
	 * @throws IllegalStateException if reflection errors prevent invoking the method
	 */
	static void setReportData(Object generator, List<?> beans)
	{
		
		if (beans == null || beans.isEmpty())
		{
			LOGGER.fine(() -> "No report data provided for generator " +
				generator.getClass().getName() + "; skipping override.");
			return;
		}
		
		try
		{
			Method method = generator.getClass()
				.getMethod("setReportData", List.class);
			method.setAccessible(true);
			LOGGER.fine(() -> "Supplying " + beans.size() +
				" report data rows to generator " +
				generator.getClass().getName() + ".");
			method.invoke(generator, beans);
		}
		catch (NoSuchMethodException e)
		{
			// Optional API; ignore if not present.
			LOGGER.fine(() -> "Generator " + generator.getClass().getName() +
				" does not implement setReportData; skipping.");
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalStateException(
				"Failed to set report data on generator", e);
		}
		
	}
	
	/**
	 * Invokes the generator's {@code generatePrint()} method to produce a {@link JasperPrint}
	 * that can be written to disk. Exceptions thrown by the generator are unwrapped to preserve
	 * their original {@link JRException} or {@link IOException} types.
	 *
	 * @param generator generator instance created by {@link #instantiate(String, ReportContext, ReportService)}
	 * @return rendered {@link JasperPrint} ready for export
	 * @throws JRException when the generator is missing the method or it fails
	 */
	static JasperPrint generatePrint(Object generator) throws JRException
	{
		
		try
		{
			LOGGER.fine(() -> "Generating JasperPrint with generator " +
				generator.getClass().getName() + ".");
			Method method = generator.getClass().getMethod("generatePrint");
			method.setAccessible(true);
			return (JasperPrint) method.invoke(generator);
		}
		catch (NoSuchMethodException e)
		{
			throw new JRException("Generator missing generatePrint()", e);
		}
		catch (IllegalAccessException e)
		{
			throw new JRException("Unable to call generatePrint()", e);
		}
		catch (InvocationTargetException e)
		{
			Throwable cause = e.getCause();
			
			if (cause instanceof JRException jr)
			{
				throw jr;
			}
			
			throw new JRException("Generator generatePrint() failed", cause);
		}
		
	}
	
	/**
	 * Retrieves a human-friendly base name from the generator. Implementations that declare a
	 * {@code getBaseName()} method can control the returned name; otherwise the generator's
	 * simple class name is used.
	 *
	 * @param generator generator instance created by {@link #instantiate(String, ReportContext, ReportService)}
	 * @return base name used when constructing output filenames
	 * @throws IllegalStateException if the optional method exists but cannot be invoked
	 */
	static String getBaseName(Object generator)
	{
		
		try
		{
			Method method = generator.getClass().getMethod("getBaseName");
			method.setAccessible(true);
			Object result = method.invoke(generator);
			
			if (result instanceof String name && !name.isBlank())
			{
				LOGGER.fine(() -> "Resolved report base name '" + name +
					"' from generator " + generator.getClass().getName() +
					".");
				return name;
			}
			
		}
		catch (NoSuchMethodException e)
		{
			// fall back to simple class name
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalStateException(
				"Unable to determine generator base name", e);
		}
		
		return generator.getClass().getSimpleName();
		
	}
	
	/**
	 * Delegates to the generator's {@code writeJasperOutput(format, print, baseName)} method to
	 * export the rendered report to disk. The method ensures a {@link File} is returned and
	 * unwraps checked and runtime exceptions thrown by the generator for clearer diagnostics.
	 *
	 * @param generator generator instance created by {@link #instantiate(String, ReportContext, ReportService)}
	 * @param format    output format such as {@code pdf} or {@code html}
	 * @param print     prepared {@link JasperPrint} to render
	 * @param baseName  base filename to pass through to the generator
	 * @return file produced by the generator
	 * @throws JRException when the generator is missing the required method or signals a Jasper
	 *                     failure
	 * @throws IOException if the generator wraps an {@link IOException}
	 * @throws IllegalStateException if reflection fails or the method returns a non-file result
	 */
	static File writeOutput(Object generator,
		String format,
		JasperPrint print,
		String baseName) throws JRException, IOException
	{
		
		try
		{
			Method method = generator.getClass()
				.getMethod("writeJasperOutput", String.class, JasperPrint.class,
					String.class);
			method.setAccessible(true);
			LOGGER.fine(() -> "Writing report output for generator " +
				generator.getClass().getName() + " with format '" +
				format + "' and baseName '" + baseName + "'.");
			Object result = method.invoke(generator, format, print, baseName);
			
			if (result instanceof File file)
			{
				return file;
			}
			
			throw new IllegalStateException(
				"writeJasperOutput did not return a File for " +
					generator.getClass().getName());
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalStateException(
				"Generator missing writeJasperOutput()", e);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalStateException(
				"Unable to call writeJasperOutput()", e);
		}
		catch (InvocationTargetException e)
		{
			Throwable cause = e.getCause();
			
			if (cause instanceof JRException jr)
			{
				throw jr;
			}
			
			if (cause instanceof IOException io)
			{
				throw io;
			}
			
			if (cause instanceof RuntimeException runtime)
			{
				throw runtime;
			}
			
			throw new JRException("writeJasperOutput failed", cause);
		}
		
	}
	
}
