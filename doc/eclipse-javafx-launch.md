# Launching `org.nonprofitbookkeeping.ui.FxMain` from Eclipse

Use these steps when you want to start the JavaFX desktop app directly from Eclipse.

## 0) Optional: import the bundled Eclipse launch file
A ready-to-run launch config is committed at:

- `NonprofitAccounting - FxMain (Maven ui).launch`

In Eclipse: `File` → `Import...` → `Run/Debug` → `Launch Configurations`, then select this file.

## 1) Import as a Maven project
1. `File` → `Import...` → `Maven` → `Existing Maven Projects`.
2. Select the repository root (`NonprofitAccounting`) and finish import.
3. Wait for Eclipse to complete Maven dependency resolution.

## 2) Verify JDK and Maven setup in Eclipse
1. Make sure the project is using **Java 17** (`Project` → `Properties` → `Java Compiler`).
2. In `Maven` → `Update Project...` (Alt+F5), force update snapshots/releases once.

## 3) Run the app (recommended Eclipse launch)
Because JavaFX needs platform-specific native jars, run through Maven profile `ui`:

1. Right click project → `Run As` → `Maven build...`
2. Set **Goals** to:
   ```
   -Pui javafx:run
   ```
3. Click `Run`.

This is the most reliable launch path in Eclipse.

## 4) Alternative: Java Application launch for `FxMain`
If you specifically want `Run As` → `Java Application` on `org.nonprofitbookkeeping.ui.FxMain`:

1. Open `Run Configurations...` → `Java Application`.
2. Create/select config for `org.nonprofitbookkeeping.ui.FxMain`.
3. On the `Classpath` tab, ensure Maven Dependencies are present.
4. Click `Apply` and `Run`.

If launch still fails, run `Maven` → `Update Project...` and retry.

## 5) Typical troubleshooting
- If you see JavaFX `LauncherImpl` startup exceptions, re-run `Maven > Update Project` and relaunch.
- If you run in a headless environment/remote shell without display, JavaFX UI launch will fail even with correct dependencies.
- If you previously created old run configs, delete and recreate them after Maven update.
