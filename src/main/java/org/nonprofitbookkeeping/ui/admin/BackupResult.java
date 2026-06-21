package org.nonprofitbookkeeping.ui.admin;

import java.nio.file.Path;
import java.util.List;

/** Backup guidance and outputs associated with an administrative operation. */
public record BackupResult(boolean recommended, boolean required, boolean completed, List<Path> backupPaths, String message)
{
    public BackupResult
    {
        backupPaths = backupPaths == null ? List.of() : List.copyOf(backupPaths);
        message = ValidationMessage.normalize(message);
        if (required && !recommended)
        {
            recommended = true;
        }
    }

    public static BackupResult none()
    {
        return new BackupResult(false, false, false, List.of(), null);
    }

    public static BackupResult recommended(String message)
    {
        return new BackupResult(true, false, false, List.of(), message);
    }

    public static BackupResult completed(List<Path> backupPaths, String message)
    {
        return new BackupResult(true, false, true, backupPaths, message);
    }
}
