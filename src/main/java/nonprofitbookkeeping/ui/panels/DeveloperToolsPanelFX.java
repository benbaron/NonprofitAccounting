package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.service.DemoCompanySeeder;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.util.FormatUtils;

/** Developer-only tools intentionally separated from normal company setup. */
public class DeveloperToolsPanelFX extends VBox
{
    @FunctionalInterface
    public interface CompanyOpener
    {
        void open(long companyId, String label) throws Exception;
    }

    private final CompanyRepository repository = new CompanyRepository();
    private final DemoCompanySeeder seeder = new DemoCompanySeeder();
    private final CompanyOpener companyOpener;
    private final Label status = new Label();

    public DeveloperToolsPanelFX()
    {
        this((companyId, label) -> {
            CurrentCompany.loadFromPersistent(companyId);
            PreferencesService.setLastUsedCompanyId(companyId);
        });
    }

    public DeveloperToolsPanelFX(CompanyOpener companyOpener)
    {
        this.companyOpener = companyOpener;
        setPadding(new Insets(12));
        setSpacing(10);

        Label title = new Label("Developer Tools");
        title.setStyle("-fx-font-size: 1.4em; -fx-font-weight: bold;");
        Label warning = new Label(
            "These tools create deterministic development data and should " +
                "not be used for production bookkeeping.");
        warning.setWrapText(true);

        Button sample = new Button(
            "Create Deterministic Sample Company");
        sample.setOnAction(event -> createSampleCompany());
        getChildren().setAll(title, warning, sample, this.status);
    }

    private void createSampleCompany()
    {
        if (!Database.isInitialized())
        {
            this.status.setText("Open a database first.");
            return;
        }
        try
        {
            for (CompanyRepository.CompanyRecord record :
                this.repository.listCompanies())
            {
                if ("Sample Nonprofit Company".equalsIgnoreCase(
                    record.name()))
                {
                    throw new IllegalStateException(
                        "The deterministic sample company already exists.");
                }
            }
            Company sample = new Company();
            this.seeder.seed(sample);
            long id = this.repository.save(null, sample);
            this.repository.markOpened(id);
            this.companyOpener.open(id, "Sample Nonprofit Company");
            if (sample.getCompanyProfileModel() != null)
            {
                FormatUtils.configureLocale(null,
                    sample.getCompanyProfileModel().getBaseCurrency());
            }
            this.status.setText("Created and opened sample company ID " + id);
        }
        catch (SQLException | IOException ex)
        {
            this.status.setText("Unable to create sample company: " +
                ex.getMessage());
        }
        catch (Exception ex)
        {
            this.status.setText(ex.getMessage());
        }
    }
}
