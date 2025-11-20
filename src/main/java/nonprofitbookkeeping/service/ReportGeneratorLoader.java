package nonprofitbookkeeping.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportBundles;
import nonprofitbookkeeping.reports.jasper.BundledTemplateJasperGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility that instantiates and interacts with Jasper report generator classes using reflection.
 * The report generators are discovered at runtime by class name, allowing the service to function
 * even when the generator implementations live outside the current compilation unit.
 */
final class ReportGeneratorLoader
{
        private ReportGeneratorLoader()
        {
        }

        static Object instantiate(String className,
                ReportContext context,
                ReportService service)
        {
                Objects.requireNonNull(className, "className");

                try
                {
                        Class<?> clazz = Class.forName(className);
                        Constructor<?> ctor = findConstructor(clazz,
                                ReportContext.class, ReportService.class);

                        if (ctor != null)
                        {
                                return ctor.newInstance(context, service);
                        }

                        ctor = findConstructor(clazz, ReportContext.class);

                        if (ctor != null)
                        {
                                return ctor.newInstance(context);
                        }

                        ctor = findConstructor(clazz, ReportService.class);

                        if (ctor != null)
                        {
                                return ctor.newInstance(service);
                        }

                        ctor = findConstructor(clazz);

                        if (ctor != null)
                        {
                                return ctor.newInstance();
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
                                        return candidate.newInstance(args);
                                }
                                catch (InvocationTargetException | InstantiationException
                                        | IllegalAccessException e)
                                {
                                        // try next constructor
                                }
                        }

                        throw new IllegalStateException(
                                "No suitable constructor for " + className);
                }
                catch (ClassNotFoundException e)
                {
                        ReportBundles.Bundle bundle = ReportBundles
                                .bundleForGenerator(className);

                        return new BundledTemplateJasperGenerator(bundle, context);
                }
                catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
                {
                        throw new IllegalStateException(
                                "Unable to instantiate generator: " + className, e);
                }
        }

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

        static void setReportData(Object generator, List<?> beans)
        {
                if (beans == null || beans.isEmpty())
                {
                        return;
                }

                try
                {
                        Method method = generator.getClass()
                                .getMethod("setReportData", List.class);
                        method.setAccessible(true);
                        method.invoke(generator, beans);
                }
                catch (NoSuchMethodException e)
                {
                        // Optional API; ignore if not present.
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                        throw new IllegalStateException(
                                "Failed to set report data on generator", e);
                }
        }

        static JasperPrint generatePrint(Object generator) throws JRException
        {
                try
                {
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

        static String getBaseName(Object generator)
        {
                try
                {
                        Method method = generator.getClass().getMethod("getBaseName");
                        method.setAccessible(true);
                        Object result = method.invoke(generator);

                        if (result instanceof String name && !name.isBlank())
                        {
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
                        Object result = method.invoke(generator, format, print, baseName);

                        if (result instanceof File file)
                        {
                                return file;
                        }

                        throw new IllegalStateException(
                                "writeJasperOutput did not return a File for "
                                        + generator.getClass().getName());
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
