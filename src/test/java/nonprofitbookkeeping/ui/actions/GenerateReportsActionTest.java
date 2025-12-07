
package nonprofitbookkeeping.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GenerateReportsAction} that focus on the legacy Swing bridge.
 */
public class GenerateReportsActionTest
{
	
	@Test
	public void actionPerformedDelegatesToHandle()
	{
		TestableGenerateReportsAction action =
			new TestableGenerateReportsAction();
		
		java.awt.event.ActionEvent swingEvent =
			new java.awt.event.ActionEvent(this,
				java.awt.event.ActionEvent.ACTION_PERFORMED, "generate");
		
		action.actionPerformed(swingEvent);
		
		assertEquals(1, action.invocationCount);
		assertNotNull(action.lastEvent);
		assertSame(this, action.lastEvent.getSource());
		
	}
	
	@Test
	public void actionPerformedHandlesNullEvent()
	{
		TestableGenerateReportsAction action =
			new TestableGenerateReportsAction();
		
		action.actionPerformed(null);
		
		assertEquals(1, action.invocationCount);
		assertNotNull(action.lastEvent);
		
	}
	
	private static final class TestableGenerateReportsAction
		extends GenerateReportsAction
	{
		
		private TestableGenerateReportsAction()
		{
			super(null);
			
		}
		
		private int invocationCount;
		private javafx.event.ActionEvent lastEvent;
		
		@Override
		public void handle(javafx.event.ActionEvent event)
		{
			this.invocationCount++;
			this.lastEvent = event;
			
		}
		
	}
	
}
