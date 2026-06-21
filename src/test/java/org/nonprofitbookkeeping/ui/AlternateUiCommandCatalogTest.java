package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AlternateUiCommandCatalogTest
{
    @Test
    void commandAvailabilityWhenNoDatabaseIsOpen()
    {
        UiSessionContext context = new UiSessionContext();
        List<CommandDescriptor> commands = new AlternateUiCommandCatalog(context).commands(AlternateUiCommandActions.noop());

        assertCommand(commands, "Open Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Open Company", CommandAvailability.DISABLED, "Open a database");
        assertCommand(commands, "Repair/Recover H2 Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Close Database", CommandAvailability.DISABLED, "Open a database");
        assertCommand(commands, "Chart of Accounts", CommandAvailability.DISABLED, "Open a company");
        assertCommand(commands, "Import Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Import SCLX", CommandAvailability.DISABLED, "Open a database");
        assertTrue(commands.stream().anyMatch(c -> c.category().equals(AlternateUiCommandCatalog.DATABASE_COMPANY)));
        assertTrue(commands.stream().anyMatch(c -> c.category().equals(AlternateUiCommandCatalog.IMPORT_EXPORT)));
    }

    @Test
    void commandAvailabilityWhenDatabaseOpenWithoutCompany()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/example"));
        List<CommandDescriptor> commands = new AlternateUiCommandCatalog(context).commands(AlternateUiCommandActions.noop());

        assertCommand(commands, "Open Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Open Company", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Repair/Recover H2 Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Export/Backup Database", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Journal", CommandAvailability.DISABLED, "Open a company");
        assertCommand(commands, "Create Company", CommandAvailability.AVAILABLE, "");
    }

    @Test
    void commandAvailabilityWhenCompanyOpen()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/example"));
        context.openCompany(7L, "Example Company");
        List<CommandDescriptor> commands = new AlternateUiCommandCatalog(context).commands(AlternateUiCommandActions.noop());

        assertCommand(commands, "Open Company", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Journal", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Save", CommandAvailability.AVAILABLE, "");
        assertCommand(commands, "Export Chart of Accounts", CommandAvailability.AVAILABLE, "");
    }

    private static void assertCommand(List<CommandDescriptor> commands, String label,
        CommandAvailability availability, String reasonFragment)
    {
        CommandDescriptor descriptor = commands.stream()
            .filter(command -> command.label().equals(label))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing command: " + label));
        assertEquals(availability, descriptor.availability());
        if (reasonFragment.isBlank())
        {
            assertTrue(descriptor.disabledReason().isBlank());
        }
        else
        {
            assertFalse(descriptor.disabledReason().isBlank());
            assertTrue(descriptor.disabledReason().contains(reasonFragment), descriptor.disabledReason());
        }
    }
}
