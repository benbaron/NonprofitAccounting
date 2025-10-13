package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.actions.CreateOrEditCompanyActionFX;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * Lists the companies stored inside the shared database and allows the user to preview
 * and open them. The legacy file-based workflow has been replaced with database-backed
 * persistence so the UI now operates on {@link CompanyRepository.CompanyRecord} entries.
 */
public class CompanySelectionPanelFX extends BorderPane
{
        /** Callback invoked when a company has been successfully opened. */
        @FunctionalInterface public interface OnCompanyOpenedHandler
        {
                void onCompanyOpened(Company company);
        }

        private final CompanyRepository repository = new CompanyRepository();
        private final ListView<CompanyRecord> companyList = new ListView<>();
        private final ObservableList<CompanyRecord> companyItems = FXCollections.observableArrayList();
        private final TextArea previewArea = new TextArea();

        private OnCompanyOpenedHandler companyOpenedHandler;
        private Consumer<String> errorHandler = msg -> AlertBox.showError(null, msg);

        public CompanySelectionPanelFX()
        {
                setPadding(new Insets(10));
                buildUI();
                reloadCompanyList();
        }

        public CompanySelectionPanelFX(OnCompanyOpenedHandler companyOpenedHandler)
        {
                this();
                this.companyOpenedHandler = companyOpenedHandler;
        }

        /** Allows callers to override how error messages are surfaced. */
        public void setOnError(Consumer<String> handler)
        {
                if (handler != null)
                {
                        this.errorHandler = handler;
                }
        }

        /** Sets the handler that will be notified when the user opens a company. */
        public void setOnCompanyOpenedHandler(OnCompanyOpenedHandler handler)
        {
                this.companyOpenedHandler = handler;
        }

        private void buildUI()
        {
                this.companyList.setItems(this.companyItems);
                this.companyList.setCellFactory(list -> new ListCell<>()
                {
                        @Override protected void updateItem(CompanyRecord record, boolean empty)
                        {
                                super.updateItem(record, empty);

                                if (empty || record == null)
                                {
                                        setText(null);
                                }
                                else
                                {
                                        setText(record.name() + " (ID: " + record.id() + ")");
                                }
                        }
                });
                this.companyList.getSelectionModel().selectedItemProperty()
                        .addListener((obs, oldVal, newVal) -> showPreview(newVal));

                this.previewArea.setEditable(false);
                this.previewArea.setWrapText(true);

                SplitPane splitPane = new SplitPane(this.companyList, this.previewArea);
                splitPane.setDividerPositions(0.4);
                setCenter(splitPane);

                Button openBtn = new Button("Open Selected");
                Button createBtn = new Button("Create New Company…");
                openBtn.setOnAction(e -> openSelected());
                createBtn.setOnAction(e -> createNew());

                HBox buttons = new HBox(10, openBtn, createBtn);
                buttons.setPadding(new Insets(8));
                HBox.setHgrow(openBtn, Priority.NEVER);
                HBox.setHgrow(createBtn, Priority.NEVER);
                setBottom(buttons);
        }

        private void reloadCompanyList()
        {
                this.companyItems.clear();

                try
                {
                        this.companyItems.addAll(this.repository.listCompanies());
                }
                catch (SQLException e)
                {
                        this.errorHandler.accept("Failed to load companies: " + e.getMessage());
                }

                if (!this.companyItems.isEmpty())
                {
                                this.companyList.getSelectionModel().selectFirst();
                }
                else
                {
                        this.previewArea.clear();
                }
        }

        private void showPreview(CompanyRecord record)
        {
                if (record == null)
                {
                        this.previewArea.clear();
                        return;
                }

                try
                {
                        Company company = this.repository.load(record.id());
                        StringBuilder sb = new StringBuilder();

                        if (company.getCompanyProfileModel() != null)
                        {
                                sb.append("Name: ")
                                        .append(nullToEmpty(company.getCompanyProfileModel().getCompanyName()))
                                        .append('\n');
                                sb.append("Base currency: ")
                                        .append(nullToEmpty(company.getCompanyProfileModel().getBaseCurrency()))
                                        .append('\n');
                                sb.append("Fiscal year start: ")
                                        .append(nullToEmpty(company.getCompanyProfileModel().getFiscalYearStart()))
                                        .append('\n');
                                sb.append("Default bank account: ")
                                        .append(nullToEmpty(company.getCompanyProfileModel().getDefaultBankAccount()))
                                        .append('\n');
                        }

                        sb.append("Accounts: ")
                                .append(company.getChartOfAccounts() == null ? 0
                                        : company.getChartOfAccounts().getAccounts().size())
                                .append('\n');
                        sb.append("Transactions: ")
                                .append(company.getLedger() == null
                                        || company.getLedger().getJournal() == null ? 0
                                        : company.getLedger().getJournal().getJournalTransactions().size());

                        this.previewArea.setText(sb.toString());
                }
                catch (IOException | SQLException e)
                {
                        this.previewArea.setText("Unable to preview company: " + e.getMessage());
                }
        }

        void openSelected()
        {
                CompanyRecord record = this.companyList.getSelectionModel().getSelectedItem();

                if (record == null)
                {
                        this.errorHandler.accept("No company selected.");
                        return;
                }

                try
                {
                        CurrentCompany.loadFromPersistent(record.id());
                        PreferencesService.setLastUsedCompanyId(record.id());

                        if (this.companyOpenedHandler != null)
                        {
                                this.companyOpenedHandler.onCompanyOpened(CurrentCompany.getCompany());
                        }
                }
                catch (IOException e)
                {
                        this.errorHandler.accept("Failed to open company: " + e.getMessage());
                }
        }

        private void createNew()
        {
                Stage owner = getScene() != null ? (Stage) getScene().getWindow() : null;
                new CreateOrEditCompanyActionFX(owner);
                reloadCompanyList();

                if (this.companyOpenedHandler != null && CurrentCompany.getCompany() != null)
                {
                        this.companyOpenedHandler.onCompanyOpened(CurrentCompany.getCompany());
                }
        }

        private static String nullToEmpty(String value)
        {
                return value == null ? "" : value;
        }
}
