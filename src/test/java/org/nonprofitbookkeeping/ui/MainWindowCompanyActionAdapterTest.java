package org.nonprofitbookkeeping.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.model.AppPreferencesState;
import org.nonprofitbookkeeping.model.MultiCompanyState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainWindowCompanyActionAdapterTest
{
    @BeforeAll
    static void setupFx()
    {
        FxTestSupport.initToolkitOrSkip();
    }

    @Test
    void constructorUsesProvidedCompanyActionAdapter()
    {
        CompanyActionAdapter adapter = new CompanyActionAdapter()
        {
            @Override
            public void openCompany(Runnable onCompanyOpened) { }

            @Override
            public void createOrEditCompany() { }

            @Override
            public void saveCompany() { }

            @Override
            public boolean closeCompany() { return false; }
        };

        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow(inMemoryStore(), adapter);
            assertSame(adapter, window.companyActionAdapterForTests());
            return null;
        });
    }

    @Test
    void constructorDefaultsCompanyActionAdapterWhenNull()
    {
        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow(inMemoryStore(), null);
            assertNotNull(window.companyActionAdapterForTests());
            assertTrue(window.companyActionAdapterForTests() instanceof LegacyCompanyActionAdapter);
            return null;
        });
    }

    private static AppStateStore inMemoryStore()
    {
        return new AppStateStore()
        {
            @Override
            public Optional<AppPreferencesState> loadPreferences()
            {
                return Optional.empty();
            }

            @Override
            public Optional<MultiCompanyState> loadMultiCompany()
            {
                return Optional.empty();
            }

            @Override
            public void savePreferences(AppPreferencesState state)
            {
            }

            @Override
            public void saveMultiCompany(MultiCompanyState state)
            {
            }
        };
    }
}
