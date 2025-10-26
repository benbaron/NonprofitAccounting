package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import nonprofitbookkeeping.model.CurrentCompany;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link AccountTransactionDetailsPanelFX} manages its company change listener
 * without registering duplicates and properly unregisters it when the panel is disposed.
 */
class AccountTransactionDetailsPanelListenerTest
{
        @BeforeAll static void initToolkit()
        {
                // Configure JavaFX to use the headless Monocle platform so tests run without a display.
                System.setProperty("javafx.platform", "Monocle");
                System.setProperty("glass.platform", "Monocle");
                System.setProperty("monocle.platform", "Headless");
                System.setProperty("prism.order", "sw");
                System.setProperty("prism.text", "t2k");
                System.setProperty("prism.es2", "false");
                System.setProperty("prism.allowhidpi", "false");
                System.setProperty("java.awt.headless", "true");

                try
                {
                        Platform.startup(() -> {
                        });
                }
                catch (IllegalStateException alreadyStarted)
                {
                        // Toolkit already initialized by another test.
                }
        }

        @BeforeEach void clearRegisteredListeners()
        {
                removeAllCompanyListeners();
        }

        @AfterEach void tearDownListeners()
        {
                removeAllCompanyListeners();
        }

        @Test void setupRegistersListenerOnlyOnce() throws Exception
        {
                AccountTransactionDetailsPanelFX panel = createPanelOnFxThread();

                try
                {
                        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size(),
                                "Panel should register exactly one listener during construction.");

                        Method setupMethod =
                                AccountTransactionDetailsPanelFX.class.getDeclaredMethod("setupCompanyChangeListener");
                        setupMethod.setAccessible(true);

                        runOnFxThread(() -> invokeQuietly(panel, setupMethod));
                        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size(),
                                "Re-invoking setup should not register a second listener.");

                        runOnFxThread(() -> invokeQuietly(panel, setupMethod));
                        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size(),
                                "Multiple invocations should keep listener count at one.");
                }
                finally
                {
                        runOnFxThread(panel::dispose);
                }
        }

        @Test void disposeRemovesListener() throws Exception
        {
                AccountTransactionDetailsPanelFX panel = createPanelOnFxThread();

                try
                {
                        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size(),
                                "Panel should register a listener on construction.");

                        runOnFxThread(panel::dispose);

                        assertTrue(CurrentCompany.CompanyListener.getListeners().isEmpty(),
                                "Disposing the panel should remove the registered listener.");
                }
                finally
                {
                        runOnFxThread(panel::dispose);
                }
        }

        private AccountTransactionDetailsPanelFX createPanelOnFxThread()
        {
                CompletableFuture<AccountTransactionDetailsPanelFX> future = new CompletableFuture<>();
                Platform.runLater(() -> future.complete(new AccountTransactionDetailsPanelFX()));
                return future.join();
        }

        private void runOnFxThread(Runnable runnable)
        {
                CompletableFuture<Void> future = new CompletableFuture<>();
                Platform.runLater(() -> {
                        try
                        {
                                runnable.run();
                                future.complete(null);
                        }
                        catch (Throwable throwable)
                        {
                                future.completeExceptionally(throwable);
                        }
                });
                future.join();
        }

        private void invokeQuietly(AccountTransactionDetailsPanelFX panel, Method method)
        {
                try
                {
                        method.invoke(panel);
                }
                catch (Exception exception)
                {
                        throw new RuntimeException(exception);
                }
        }

        private void removeAllCompanyListeners()
        {
                for (CurrentCompany.CompanyChangeListener listener : CurrentCompany.CompanyListener.getListeners())
                {
                        CurrentCompany.CompanyListener.removeCompanyListener(listener);
                }
        }
}
