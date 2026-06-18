package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Observable session state for the alternate JavaFX shell.
 *
 * <p>New alternate UI code should read the active database and company state from this
 * context instead of directly consulting legacy globals. {@link CurrentCompany} is
 * intentionally retained as a compatibility bridge for legacy panels that still require it.</p>
 */
public class UiSessionContext
{
    public enum SessionState
    {
        NO_DATABASE,
        DATABASE_OPEN_NO_COMPANY,
        COMPANY_OPEN
    }

    private final ObjectProperty<Path> activeDatabaseBasePath = new SimpleObjectProperty<>();
    private final ObjectProperty<Long> activeCompanyId = new SimpleObjectProperty<>();
    private final StringProperty activeCompanyDisplayLabel = new SimpleStringProperty("No company open");
    private final BooleanProperty sampleCompany = new SimpleBooleanProperty(false);
    private final BooleanProperty populatedCompany = new SimpleBooleanProperty(false);
    private final BooleanProperty newlyCreatedCompany = new SimpleBooleanProperty(false);
    private final BooleanBinding databaseOpen = Bindings.isNotNull(activeDatabaseBasePath);
    private final BooleanBinding companyOpen = databaseOpen.and(Bindings.isNotNull(activeCompanyId));
    private final ObjectBinding<SessionState> sessionState = Bindings.createObjectBinding(this::calculateSessionState,
        activeDatabaseBasePath, activeCompanyId);
    private final StringBinding sessionDisplayLabel = Bindings.createStringBinding(this::calculateSessionDisplayLabel,
        activeDatabaseBasePath, activeCompanyId, activeCompanyDisplayLabel);

    public ObjectProperty<Path> activeDatabaseBasePathProperty()
    {
        return activeDatabaseBasePath;
    }

    public Path activeDatabaseBasePath()
    {
        return activeDatabaseBasePath.get();
    }

    public ObjectProperty<Long> activeCompanyIdProperty()
    {
        return activeCompanyId;
    }

    public Long activeCompanyId()
    {
        return activeCompanyId.get();
    }

    public ReadOnlyStringProperty activeCompanyDisplayLabelProperty()
    {
        return activeCompanyDisplayLabel;
    }

    public String activeCompanyDisplayLabel()
    {
        return activeCompanyDisplayLabel.get();
    }

    public BooleanBinding databaseOpenProperty()
    {
        return databaseOpen;
    }

    public boolean isDatabaseOpen()
    {
        return databaseOpen.get();
    }

    public BooleanBinding companyOpenProperty()
    {
        return companyOpen;
    }

    public boolean isCompanyOpen()
    {
        return companyOpen.get();
    }

    public ObjectBinding<SessionState> sessionStateProperty()
    {
        return sessionState;
    }

    public SessionState sessionState()
    {
        return sessionState.get();
    }

    public StringBinding sessionDisplayLabelProperty()
    {
        return sessionDisplayLabel;
    }

    public String sessionDisplayLabel()
    {
        return sessionDisplayLabel.get();
    }

    public BooleanProperty sampleCompanyProperty()
    {
        return sampleCompany;
    }

    public boolean isSampleCompany()
    {
        return sampleCompany.get();
    }

    public BooleanProperty populatedCompanyProperty()
    {
        return populatedCompany;
    }

    public boolean isPopulatedCompany()
    {
        return populatedCompany.get();
    }

    public BooleanProperty newlyCreatedCompanyProperty()
    {
        return newlyCreatedCompany;
    }

    public boolean isNewlyCreatedCompany()
    {
        return newlyCreatedCompany.get();
    }

    public void openDatabase(Path databaseBasePath)
    {
        activeDatabaseBasePath.set(databaseBasePath);
        clearCompany();
    }

    public void clearDatabase()
    {
        activeDatabaseBasePath.set(null);
        clearCompany();
    }

    public void openCompany(long companyId, String displayLabel)
    {
        openCompany(companyId, displayLabel, CompanyMetadata.production());
    }

    public void openCompany(long companyId, String displayLabel, CompanyMetadata metadata)
    {
        activeCompanyId.set(companyId);
        activeCompanyDisplayLabel.set(normalizeDisplayLabel(displayLabel));
        applyMetadata(metadata == null ? CompanyMetadata.production() : metadata);
    }

    public void clearCompany()
    {
        activeCompanyId.set(null);
        activeCompanyDisplayLabel.set("No company open");
        applyMetadata(CompanyMetadata.production());
    }

    public void updateCompanyMetadata(CompanyMetadata metadata)
    {
        applyMetadata(metadata == null ? CompanyMetadata.production() : metadata);
    }

    /**
     * Compatibility helper for legacy panels that still update {@link CurrentCompany}
     * directly. New alternate workflows should prefer {@link #openCompany(long, String)}.
     */
    public void refreshCompanyLabelFromLegacyCurrentCompany()
    {
        Company company = CurrentCompany.getCompany();
        if (CurrentCompany.isOpen() && company != null)
        {
            activeCompanyId.set(CurrentCompany.getCurrentCompanyId());
            activeCompanyDisplayLabel.set(normalizeDisplayLabel(company.getName()));
        }
    }

    private SessionState calculateSessionState()
    {
        if (activeDatabaseBasePath.get() == null)
        {
            return SessionState.NO_DATABASE;
        }
        return activeCompanyId.get() == null ? SessionState.DATABASE_OPEN_NO_COMPANY : SessionState.COMPANY_OPEN;
    }

    private String calculateSessionDisplayLabel()
    {
        return switch (calculateSessionState())
        {
            case NO_DATABASE -> "No database open";
            case DATABASE_OPEN_NO_COMPANY -> "Database open — no company open";
            case COMPANY_OPEN -> activeCompanyDisplayLabel();
        };
    }

    private void applyMetadata(CompanyMetadata metadata)
    {
        sampleCompany.set(metadata.sample());
        populatedCompany.set(metadata.populated());
        newlyCreatedCompany.set(metadata.newlyCreated());
    }

    private String normalizeDisplayLabel(String displayLabel)
    {
        return displayLabel == null || displayLabel.isBlank() ? "Unnamed company" : displayLabel;
    }

    public record CompanyMetadata(boolean sample, boolean populated, boolean newlyCreated)
    {
        public static CompanyMetadata production()
        {
            return new CompanyMetadata(false, false, false);
        }
    }
}
