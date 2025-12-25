
package nonprofitbookkeeping.reports.jasper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AbstractReportGenerator} focusing on the behaviour added to
 * support {@link AbstractReportGenerator#setReportData(List)}.
 */
class AbstractReportGeneratorTest
{
	/**
	 * Minimal concrete implementation used for exercising the helper logic.
	 */
	private static final class DummyGenerator extends AbstractReportGenerator
	{
		private final List<String> defaultData = List.of("default");
		
		@Override
		protected List<?> getReportData()
		{
			return this.defaultData;
			
		}
		
		@Override
		protected Map<String, Object> getReportParameters()
		{
			return Collections.emptyMap();
			
		}
		
		@Override
		protected String getReportPath()
		{
			return "unused.jrxml";
			
		}
		
		@Override
		public String getBaseName()
		{
			return "dummy";
			
		}
		
		List<?> resolved()
		{
			return resolveReportData();
			
		}
		
	}
	
	@Test
	void resolveReportDataPrefersSuppliedBeans()
	{
		DummyGenerator generator = new DummyGenerator();
		
		assertEquals(List.of("default"), generator.resolved(),
			"When no beans are supplied the subclass data should be used.");
		
		List<String> beans = new ArrayList<>(List.of("a", "b"));
		generator.setReportData(beans);
		beans.add("c"); // Ensure the internal copy is defensive.
		
		List<?> resolved = generator.resolved();
		assertEquals(List.of("a", "b"), resolved,
			"Explicit beans should override subclass generated data.");
		
		@SuppressWarnings("unchecked")
		List<Object> mutableView = (List<Object>) resolved;
		assertThrows(UnsupportedOperationException.class,
			() -> mutableView.add("z"),
			"Resolved list must be unmodifiable to protect generator state.");
		
		generator.setReportData(null);
		assertEquals(List.of("default"), generator.resolved(),
			"Setting null should reset to subclass-generated data.");
		
	}
	
	@Test
	void resolveReportDataHonoursExplicitEmptyList()
	{
		DummyGenerator generator = new DummyGenerator();
		generator.setReportData(Collections.emptyList());
		assertTrue(generator.resolved().isEmpty(),
			"Callers should be able to provide an explicit empty data set.");
		
	}
	
}

