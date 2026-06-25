package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.service.CompanyManagementService;

/** Non-production utilities separated from normal company administration. */
public class DeveloperToolsPanel implements AppPanel
{
    private final VBox root = new VBox(10);
    private final Label status = new Label();
    private final CompanyManagementService companyService =
        new CompanyManagementService();

    public DeveloperToolsPanel()
    {
        this.root.setPadding(new Insets(12));
        Label title = new Label("Developer Tools");
        title.getStyleClass().add("alternate-panel-title");
        Label warning = new Label(
            "These tools create deterministic development and test data. " +
                "Do not use them in a production bookkeeping database.");
        warning.setWrapText(true);
        Button sample = new Button("Create Deterministic Sample Company");
        sample.setOnAction(event -> {
            try
            {
                long id = this.companyService.createDeterministicSampleCompany();
                this.status.setText("Created sample company ID " + id + ".");
            }
            catch (Exception ex)
            {
                this.status.setText("Unable to create sample company: " +
                    ex.getMessage());
            }
        });
        this.root.getChildren().setAll(title, warning, sample, this.status);
    }

    @Override
    public String title()
    {
        return "Developer Tools";
    }

    @Override
    public Node root()
    {
        return this.root;
    }
}
