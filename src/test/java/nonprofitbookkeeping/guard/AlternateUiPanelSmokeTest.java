package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;

/** Smoke coverage for every alternate UI panel registered in {@link AppPanelId}. */
class AlternateUiPanelSmokeTest
{
    private static final List<String> FORBIDDEN_DEMO_ACCOUNTING_SNIPPETS = List.of(
        "Payee A",
        "Payee B",
        "Program Supplies",
        "Office Rent",
        "Volunteer Meals",
        "Laptop Fleet",
        "Office Furniture",
        "Using fallback demo accounts",
        "$11,230",
        "$5,830",
        "$23,009",
        "$12,004",
        "$3,420",
        "$7,230",
        "$980");

    @BeforeAll
    static void initToolkit() throws Exception
    {
        System.setProperty("testfx.toolkit", "glass");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.es2", "false");
        CountDownLatch latch = new CountDownLatch(1);
        try
        {
            Platform.startup(latch::countDown);
        }
        catch (IllegalStateException alreadyStarted)
        {
            latch.countDown();
        }
        if (!latch.await(30, TimeUnit.SECONDS))
        {
            throw new AssertionError("Timed out starting JavaFX toolkit");
        }
    }

    static Stream<Arguments> panelIdsInSessionStates()
    {
        return Arrays.stream(AppPanelId.values())
            .flatMap(id -> fixturesFor(id).map(fixture -> Arguments.of(id, fixture)));
    }

    private static Stream<SessionFixture> fixturesFor(AppPanelId id)
    {
        if (id == AppPanelId.DATABASE_ADMIN || id == AppPanelId.COMPANY_ADMIN || id == AppPanelId.IMPORT_EXPORT)
        {
            return Stream.of(SessionFixture.NO_DATABASE, SessionFixture.DATABASE_OPEN_NO_COMPANY,
                SessionFixture.COMPANY_OPEN);
        }
        return Stream.of(SessionFixture.NO_DATABASE);
    }

    @ParameterizedTest(name = "{0} constructs in {1}")
    @MethodSource("panelIdsInSessionStates")
    @DisplayName("DefaultPanelFactory constructs every AppPanelId with a non-null root and non-blank title")
    void defaultPanelFactoryConstructsEveryPanelInPracticalSessionStates(AppPanelId id, SessionFixture fixture)
        throws Exception
    {
        runOnFxThread(() -> {
            UiSessionContext context = new UiSessionContext();
            fixture.apply(context);
            UiServiceProvider provider = new UiServiceProvider(context);
            PanelHost.DefaultPanelFactory factory = new PanelHost.DefaultPanelFactory(provider);

            AppPanel panel = assertDoesNotThrow(() -> factory.create(id),
                () -> "Expected DefaultPanelFactory to construct " + id + " in " + fixture);

            assertNotNull(panel, () -> "Panel should be created for " + id);
            assertFalse(panel.title() == null || panel.title().isBlank(),
                () -> "Panel title should be non-blank for " + id);
            assertNotNull(panel.root(), () -> "Panel root should be non-null for " + id);
        });
    }

    @ParameterizedTest(name = "{0} does not render demo rows in {1}")
    @MethodSource("panelIdsInSessionStates")
    @DisplayName("Panels do not insert realistic hardcoded accounting sample data when no service data is loaded")
    void panelsDoNotRenderRealisticHardcodedAccountingDemoData(AppPanelId id, SessionFixture fixture)
        throws Exception
    {
        runOnFxThread(() -> {
            UiSessionContext context = new UiSessionContext();
            fixture.apply(context);
            UiServiceProvider provider = new UiServiceProvider(context);
            AppPanel panel = new PanelHost.DefaultPanelFactory(provider).create(id);

            String renderedText = collectText(panel.root());
            List<String> offenders = FORBIDDEN_DEMO_ACCOUNTING_SNIPPETS.stream()
                .filter(renderedText::contains)
                .toList();

            assertFalse(offenders.size() > 0,
                () -> id + " in " + fixture + " rendered realistic demo accounting data: " + offenders);
        });
    }

    private static void runOnFxThread(ThrowingRunnable runnable) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];
        Platform.runLater(() -> {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                error[0] = t;
            }
            finally
            {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS))
        {
            throw new AssertionError("Timed out waiting for JavaFX panel smoke test");
        }
        if (error[0] != null)
        {
            throw new AssertionError("JavaFX panel smoke test failed", error[0]);
        }
    }

    private static String collectText(Node node)
    {
        StringBuilder text = new StringBuilder();
        appendText(node, text);
        return text.toString();
    }

    private static void appendText(Node node, StringBuilder text)
    {
        if (node == null)
        {
            return;
        }
        if (node instanceof Labeled labeled)
        {
            append(text, labeled.getText());
        }
        if (node instanceof TextInputControl input)
        {
            append(text, input.getText());
            append(text, input.getPromptText());
        }
        if (node instanceof ComboBoxBase<?> comboBox)
        {
            Object value = comboBox.getValue();
            append(text, value == null ? null : value.toString());
            append(text, comboBox.getPromptText());
        }
        if (node instanceof Text textNode)
        {
            append(text, textNode.getText());
        }
        if (node instanceof Parent parent)
        {
            parent.getChildrenUnmodifiable().forEach(child -> appendText(child, text));
        }
    }

    private static void append(StringBuilder text, String value)
    {
        if (value != null && !value.isBlank())
        {
            text.append('\n').append(value);
        }
    }

    private enum SessionFixture
    {
        NO_DATABASE
        {
            @Override void apply(UiSessionContext context) {}
        },
        DATABASE_OPEN_NO_COMPANY
        {
            @Override void apply(UiSessionContext context)
            {
                context.openDatabase(Path.of("target", "smoke-tests", "nonprofit-accounting-smoke.mv.db"));
            }
        },
        COMPANY_OPEN
        {
            @Override void apply(UiSessionContext context)
            {
                context.openDatabase(Path.of("target", "smoke-tests", "nonprofit-accounting-smoke.mv.db"));
                context.openCompany(1001L, "Smoke Test Company");
            }
        };

        abstract void apply(UiSessionContext context);
    }

    @FunctionalInterface
    private interface ThrowingRunnable
    {
        void run() throws Exception;
    }
}
