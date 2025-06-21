# Nonprofit Bookkeeping

This project is a Java/JavaFX desktop application for nonprofit bookkeeping. The main build is managed with Maven (`pom.xml`).

## Offline Maven Usage

Some development environments restrict network access. To build the project without hitting remote repositories, pre-populate a Maven repository with the required dependencies and plugins.

### Pre-populating a Maven Repository

Run Maven's offline preparation goal from the project root:

```bash
mvn --batch-mode dependency:go-offline
```

This downloads all dependencies and plugins declared in `pom.xml` into your local Maven repository (`~/.m2/repository`). After this step you can build with:

```bash
mvn --offline package
```

### Vendoring Dependencies in the Repository

If you want to keep a copy of the Maven repository inside the project (useful for completely isolated environments), run the helper script:

```bash
scripts/vendor-maven-dependencies.sh
```

On Windows PowerShell you can use the equivalent script:

```powershell
scripts/vendor-maven-dependencies.ps1
```

This script downloads all required artifacts into `lib/m2`. Subsequent Maven runs can use this directory as the local repository:

```bash
mvn -Dmaven.repo.local=lib/m2 --offline package
```

The vendored repository can be packaged or cached alongside the project to avoid any network downloads during build.
