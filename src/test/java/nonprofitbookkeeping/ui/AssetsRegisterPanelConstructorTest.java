package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import nonprofitbookkeeping.service.AssetRecordService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetsRegisterPanelConstructorTest
{
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
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform failed to start");
    }

    @Test
    void exposesPublicServiceConstructorForInjection()
        throws NoSuchMethodException
    {
        Constructor<AssetsRegisterPanel> ctor =
            AssetsRegisterPanel.class.getConstructor(AssetRecordService.class);
        assertNotNull(ctor);
        assertTrue(Modifier.isPublic(ctor.getModifiers()));
    }

    @Test
    void keepsExpectedCoreFieldsPresent()
    {
        Set<String> fieldNames = Arrays.stream(AssetsRegisterPanel.class.getDeclaredFields())
            .map(Field::getName)
            .collect(java.util.stream.Collectors.toSet());

        assertTrue(fieldNames.contains("LOG"));
        assertTrue(fieldNames.contains("assetRecordService"));
        assertTrue(fieldNames.contains("root"));
        assertTrue(fieldNames.contains("table"));
        assertTrue(fieldNames.contains("status"));
    }

    @Test
    void doesNotExposeGenericRecordEditorPanelConstructor()
    {
        boolean hasGenericRecordEditorCtor = Arrays.stream(AssetsRegisterPanel.class.getConstructors())
            .flatMap(ctor -> Arrays.stream(ctor.getParameterTypes()))
            .anyMatch(type -> type.getSimpleName().equals("GenericRecordEditorPanel"));
        assertFalse(hasGenericRecordEditorCtor);
    }

    @Test
    void constructsOnJavaFxThreadWithService()
        throws Exception
    {
        AssetsRegisterPanel panel = runOnFxThread(() -> new AssetsRegisterPanel(new AssetRecordService()));
        assertNotNull(panel.root());
    }

    private static <T> T runOnFxThread(Callable<T> task) throws Exception
    {
        if (Platform.isFxApplicationThread())
        {
            return task.call();
        }

        FutureTask<T> future = new FutureTask<>(task);
        Platform.runLater(future);
        return future.get(5, TimeUnit.SECONDS);
    }
}
