
package nonprofitbookkeeping.panels;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AccountTransactionDetailsPanelFX} listener management.
 */
class AccountTransactionDetailsPanelFXListenerTest
{
	static
	{
		System.setProperty("javafx.platform", "Monocle");
		System.setProperty("glass.platform", "Monocle");
		System.setProperty("monocle.platform", "Headless");
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.text", "t2k");
		System.setProperty("prism.es2", "false");
		System.setProperty("java.awt.headless", "true");
	}
	
	@BeforeAll
	static void initFxToolkit() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		
		try
		{
			Platform.startup(latch::countDown);
		}
		catch (IllegalStateException alreadyStarted)
		{
			latch.countDown();
		}
		
		if (!latch.await(5, TimeUnit.SECONDS))
		{
			throw new IllegalStateException("JavaFX platform failed to start");
		}
		
	}
	
	@BeforeEach
	void clearExistingListeners() throws Exception
	{
		runOnFxThread(() -> {
			
			for (CompanyChangeListener listener : CurrentCompany.CompanyListener
				.getListeners())
			{
				CurrentCompany.CompanyListener.removeCompanyListener(listener);
			}
			
			return null;
		});
		CurrentCompany.close();
		
	}
	
	@AfterEach
	void tearDownListeners() throws Exception
	{
		runOnFxThread(() -> {
			
			for (CompanyChangeListener listener : CurrentCompany.CompanyListener
				.getListeners())
			{
				CurrentCompany.CompanyListener.removeCompanyListener(listener);
			}
			
			return null;
		});
		
	}
	
	@Test
	void registersListenerOnceOnConstruction() throws Exception
	{
		assertTrue(CurrentCompany.CompanyListener.getListeners().isEmpty(),
			"Listeners should be empty before panel creation");
		
		AccountTransactionDetailsPanelFX panel =
			runOnFxThread(AccountTransactionDetailsPanelFX::new);
		
		try
		{
			assertEquals(1,
				CurrentCompany.CompanyListener.getListeners().size(),
				"Panel should register a single listener when constructed");
		}
		finally
		{
			runOnFxThread(() -> {
				panel.dispose();
				return null;
			});
		}
		
	}
	
	@Test
	void setupListenerDoesNotDuplicateRegistration() throws Exception
	{
		AccountTransactionDetailsPanelFX panel =
			runOnFxThread(AccountTransactionDetailsPanelFX::new);
		Method setupMethod = AccountTransactionDetailsPanelFX.class
			.getDeclaredMethod("setupCompanyChangeListener");
		setupMethod.setAccessible(true);
		
		Field listenerField = AccountTransactionDetailsPanelFX.class
			.getDeclaredField("companyChangeListener");
		listenerField.setAccessible(true);
		
		try
		{
			assertEquals(1,
				CurrentCompany.CompanyListener.getListeners().size(),
				"Initial listener registration should add exactly one listener.");
			
			CompanyChangeListener initialListener = runOnFxThread(
				() -> (CompanyChangeListener) listenerField.get(panel));
			
			runOnFxThread(() -> {
				setupMethod.invoke(panel);
				return null;
			});
			
			assertEquals(1,
				CurrentCompany.CompanyListener.getListeners().size(),
				"Re-invoking setup should not register additional listeners.");
			
			CompanyChangeListener afterListener = runOnFxThread(
				() -> (CompanyChangeListener) listenerField.get(panel));
			
			assertSame(initialListener, afterListener,
				"The panel should retain the original listener instance.");
		}
		finally
		{
			runOnFxThread(() -> {
				panel.dispose();
				return null;
			});
		}
		
	}
	
	@Test
	void disposeRemovesListener() throws Exception
	{
		AccountTransactionDetailsPanelFX panel =
			runOnFxThread(AccountTransactionDetailsPanelFX::new);
		
		assertEquals(1, CurrentCompany.CompanyListener.getListeners().size(),
			"Listener should be registered before dispose is called.");
		
		runOnFxThread(() -> {
			panel.dispose();
			return null;
		});
		
		assertTrue(CurrentCompany.CompanyListener.getListeners().isEmpty(),
			"Dispose should remove the panel's listener.");
		
	}
	
	private static <T> T runOnFxThread(Callable<T> task) throws Exception
	{
		
		if (Platform.isFxApplicationThread())
		{
			return task.call();
		}
		
		FutureTask<T> future = new FutureTask<>(task);
		Platform.runLater(future);
		
		try
		{
			return future.get(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw e;
		}
		catch (TimeoutException e)
		{
			throw new IllegalStateException("Timed out waiting for FX task", e);
		}
		catch (ExecutionException e)
		{
			Throwable cause = e.getCause();
			
			if (cause instanceof Exception)
			{
				throw (Exception) cause;
			}
			
			throw new RuntimeException(cause);
		}
		
	}
	
}
