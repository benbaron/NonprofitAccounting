package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.service.AssetRecordService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetsRegisterPanelConstructorTest
{
    @Test
    void exposesPublicServiceConstructorForInjection()
        throws NoSuchMethodException
    {
        Constructor<AssetsRegisterPanel> ctor =
            AssetsRegisterPanel.class.getConstructor(AssetRecordService.class);
        assertNotNull(ctor);
        assertTrue(Modifier.isPublic(ctor.getModifiers()));
    }
}
