package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;

import java.nio.file.Path;
import java.util.Map;

/**
 * CLI maintenance entrypoint for read model rebuild and drift checks.
 */
public final class ReadModelMaintenanceTool {
    private ReadModelMaintenanceTool() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: ReadModelMaintenanceTool <dbPath> <rebuild|drift>");
            System.exit(2);
        }
        Database.init(Path.of(args[0]));
        Database.get().ensureSchema();

        ReadModelMaintenanceService service = new ReadModelMaintenanceService();
        String op = args[1].trim().toLowerCase();
        if ("rebuild".equals(op)) {
            service.rebuildAll();
            System.out.println("Read models rebuilt successfully.");
            return;
        }
        if ("drift".equals(op)) {
            Map<String, java.math.BigDecimal> drift = service.detectDrift();
            drift.forEach((k, v) -> System.out.println(k + "=" + v));
            return;
        }
        System.err.println("Unknown operation: " + args[1]);
        System.exit(2);
    }
}
