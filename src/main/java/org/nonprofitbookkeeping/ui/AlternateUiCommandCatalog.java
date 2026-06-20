package org.nonprofitbookkeeping.ui;

import java.util.ArrayList;
import java.util.List;

/** Builds command metadata for the alternate command center and admin navigation. */
class AlternateUiCommandCatalog
{
    static final String DATABASE_COMPANY = "Database & Company";
    static final String IMPORT_EXPORT = "Import/Export";
    static final String RUN = "Run";
    static final String REPORT_ACTIONS = "Reports actions";
    static final String TOOLBAR = "Toolbar-style actions";
    static final String FUNDRAISING = "Fundraising";
    static final String BANKING = "Banking";
    static final String HELP = "Help";

    private final UiSessionContext sessionContext;

    AlternateUiCommandCatalog(UiSessionContext sessionContext)
    {
        this.sessionContext = sessionContext;
    }

    List<CommandDescriptor> commands(AlternateUiCommandActions actions)
    {
        List<CommandDescriptor> commands = new ArrayList<>();
        addDatabaseCompanyCommands(commands, actions);
        addImportExportCommands(commands, actions);
        addRunCommands(commands, actions);
        addToolbarCommands(commands, actions);
        commands.add(command(REPORT_ACTIONS, "Schedule", actions.scheduleReport()));
        commands.add(command(FUNDRAISING, "Donors", actions.openDonors()));
        commands.add(command(FUNDRAISING, "Funds", AppPanelId.FUNDS, actions.openFunds()));
        commands.add(command(BANKING, "Reconcile Accounts", actions.reconcileAccounts()));
        commands.add(command(BANKING, "Undeposited Funds", actions.undepositedFunds()));
        commands.add(command(BANKING, "Documents & Attachments", actions.documents()));
        commands.add(command(BANKING, "Account Activity", AppPanelId.LEDGER_REGISTER, actions.openJournal()));
        commands.add(command(BANKING, "Transactions", AppPanelId.LEDGER_REGISTER, actions.openJournal()));
        commands.add(command(HELP, "Help Center", actions.help()));
        return commands;
    }

    private void addDatabaseCompanyCommands(List<CommandDescriptor> commands, AlternateUiCommandActions actions)
    {
        commands.add(command(DATABASE_COMPANY, "Open Database", AppPanelId.DATABASE_ADMIN, actions.openDatabase()));
        commands.add(command(DATABASE_COMPANY, "Close Database", AppPanelId.DATABASE_ADMIN, actions.openDatabase()));
        commands.add(command(DATABASE_COMPANY, "Import Database", AppPanelId.DATABASE_ADMIN, actions.importDatabase()));
        commands.add(command(DATABASE_COMPANY, "Export/Backup Database", AppPanelId.DATABASE_ADMIN, actions.exportDatabase()));
        commands.add(command(DATABASE_COMPANY, "Validate Database", AppPanelId.DATABASE_ADMIN, actions.openDatabase()));
        commands.add(command(DATABASE_COMPANY, "Repair/Recover H2 Database", AppPanelId.DATABASE_ADMIN, actions.repairDatabase()));
        commands.add(command(DATABASE_COMPANY, "Migrate Schema", AppPanelId.DATABASE_ADMIN, actions.repairDatabase()));
        commands.add(command(DATABASE_COMPANY, "Create Company", AppPanelId.COMPANY_ADMIN, actions.createCompany()));
        commands.add(command(DATABASE_COMPANY, "Destroy/Delete Company", AppPanelId.COMPANY_ADMIN, actions.destroyCompany()));
        commands.add(command(DATABASE_COMPANY, "Populate Company", AppPanelId.COMPANY_ADMIN, actions.populateCompany()));
        commands.add(command(DATABASE_COMPANY, "Create Sample Company", AppPanelId.COMPANY_ADMIN, actions.createSampleCompany()));
        commands.add(command(DATABASE_COMPANY, "Open Company", AppPanelId.COMPANY_ADMIN, actions.openCompany()));
    }

    private void addImportExportCommands(List<CommandDescriptor> commands, AlternateUiCommandActions actions)
    {
        commands.add(command(IMPORT_EXPORT, "Import Chart of Accounts", AppPanelId.IMPORT_EXPORT, actions.importChartOfAccounts()));
        commands.add(command(IMPORT_EXPORT, "Export Chart of Accounts", AppPanelId.IMPORT_EXPORT, actions.exportChartOfAccounts()));
        commands.add(command(IMPORT_EXPORT, "Import SCLX", AppPanelId.IMPORT_EXPORT, actions.importSclx()));
        commands.add(command(IMPORT_EXPORT, "Monthly Close Checklist", AppPanelId.MONTHLY_CLOSE, actions.openMonthlyClose()));
    }

    private void addRunCommands(List<CommandDescriptor> commands, AlternateUiCommandActions actions)
    {
        commands.add(command(RUN, "Chart of Accounts", AppPanelId.CHART_OF_ACCOUNTS, actions.openChartOfAccounts()));
        commands.add(command(RUN, "Journal", AppPanelId.LEDGER_REGISTER, actions.openJournal()));
        commands.add(command(RUN, "Inventory", AppPanelId.INVENTORY, actions.openInventory()));
        commands.add(command(RUN, "Reports Workspace", AppPanelId.REPORTS_WORKSPACE, actions.openReportsWorkspace()));
    }

    private void addToolbarCommands(List<CommandDescriptor> commands, AlternateUiCommandActions actions)
    {
        commands.add(command(TOOLBAR, "New", actions.newItem()));
        commands.add(command(TOOLBAR, "Save", actions.save()));
        commands.add(command(TOOLBAR, "Delete", actions.delete()));
        commands.add(command(TOOLBAR, "Cancel", actions.cancel()));
        commands.add(command(TOOLBAR, "Find", actions.find()));
        commands.add(command(TOOLBAR, "Journal", AppPanelId.LEDGER_REGISTER, actions.openJournal()));
    }

    CommandDescriptor command(String category, String label, Runnable action)
    {
        return command(category, label, null, action);
    }

    CommandDescriptor command(String category, String label, AppPanelId panelId, Runnable action)
    {
        CommandAvailability availability = availabilityFor(label, panelId);
        return new CommandDescriptor(label, category, action, availability, reasonFor(availability, label, panelId), panelId);
    }

    private CommandAvailability availabilityFor(String label, AppPanelId panelId)
    {
        if (isNotImplemented(label))
        {
            return CommandAvailability.NOT_IMPLEMENTED;
        }
        if (requiresCompany(label, panelId) && !sessionContext.isCompanyOpen())
        {
            return CommandAvailability.DISABLED;
        }
        if (requiresDatabase(label, panelId) && !sessionContext.isDatabaseOpen())
        {
            return CommandAvailability.DISABLED;
        }
        return CommandAvailability.AVAILABLE;
    }

    private String reasonFor(CommandAvailability availability, String label, AppPanelId panelId)
    {
        if (availability == CommandAvailability.NOT_IMPLEMENTED)
        {
            return label + " is not implemented in the alternate UI yet.";
        }
        if (availability == CommandAvailability.DISABLED)
        {
            if (requiresCompany(label, panelId)) return "Open a company before using this command.";
            if (requiresDatabase(label, panelId)) return "Open a database before using this command.";
        }
        return "";
    }

    private boolean requiresDatabase(String label, AppPanelId panelId)
    {
        return panelId == AppPanelId.COMPANY_ADMIN || panelId == AppPanelId.IMPORT_EXPORT
            || label.equals("Close Database") || label.equals("Export/Backup Database");
    }

    private boolean requiresCompany(String label, AppPanelId panelId)
    {
        return panelId == AppPanelId.CHART_OF_ACCOUNTS || panelId == AppPanelId.LEDGER_REGISTER
            || panelId == AppPanelId.INVENTORY || panelId == AppPanelId.REPORTS_WORKSPACE
            || panelId == AppPanelId.FUNDS || label.equals("Schedule") || label.equals("Donors")
            || label.equals("Reconcile Accounts") || label.equals("Undeposited Funds")
            || label.equals("Documents & Attachments") || label.equals("Account Activity")
            || label.equals("Transactions") || label.equals("New") || label.equals("Save")
            || label.equals("Delete") || label.equals("Cancel");
    }

    private boolean isNotImplemented(String label)
    {
        return false;
    }
}
