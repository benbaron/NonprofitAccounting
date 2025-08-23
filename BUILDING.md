# BUILDING

## H2 Database Setup Checklist

This project is being migrated from serialized data files to a relational H2 database.  
The following steps outline the remaining work required to complete the transition.

### 1. Initialize the Database
- Switch the JDBC URL from `jdbc:h2:mem:nonprofit` to a file-backed URL, e.g.  
  `jdbc:h2:file:./data/nonprofit;AUTO_SERVER=TRUE`
- Remove or revisit `hibernate.hbm2ddl.auto=update`; use migrations or validation instead
- Consolidate `EntityManagerFactory` creation into a single class

### 2. Start the Database Engine
- Start an embedded H2 TCP server with `Server.createTcpServer().start()` if remote connections are needed
- Register a shutdown hook to stop the server when the application exits

### 3. Operate the Database (CRUD)
- Continue using repository classes with transaction boundaries
- Add a base repository or shared exception handling to reduce duplication
- Ensure all `EntityManager` instances are closed to prevent file locks

### 4. Back Up the Database
- Use H2 commands: `SCRIPT TO`, `BACKUP TO`, or direct file copy
- Expose backup and restore via UI or CLI

### 5. Create a New Company
- Annotate models like `Company`, `CompanyProfileModel`, `Ledger`, `ChartOfAccounts` with `@Entity`
- Create a `CompanyRepository` for metadata
- Expand `DatabaseService` to persist and load profiles, ledgers, and charts of accounts

### 6. Other Operations
- Implement a clean shutdown for `EntityManagerFactory` and H2 server
- Add connection pooling (e.g. HikariCP) for performance
- Provide utilities for importing legacy data

## Vendoring Maven Dependencies for Offline Builds

To run tests without network access, prefetch plugins and dependencies with the Maven Dependency Plugin.

- Add plugin to `pom.xml`:

```
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>3.6.0</version>
      <executions>
        <execution>
          <id>copy-maven-plugins</id>
          <goals>
            <goal>copy</goal>
          </goals>
          <configuration>
            <artifactItems>
              <!-- list plugins or dependencies -->
            </artifactItems>
            <outputDirectory>${project.basedir}/maven-repo</outputDirectory>
            <useRepositoryLayout>true</useRepositoryLayout>
            <includeTransitive>true</includeTransitive>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

- List artifacts: add `<artifactItem>` for each plugin (compiler, surefire, exec, etc.).

- Run the plugin:

```
mvn dependency:copy@copy-maven-plugins
```

- Use vendored repo:

```
mvn -Dmaven.repo.local=./maven-repo --offline test
```

## Building and Deploying with Maven

- Install JDK 17 and Apache Maven
- (Optional) Run vendoring to populate `maven-repo/`
- Build:

```
mvn -Dmaven.repo.local=./maven-repo --offline clean package
```

  JAR is created at `target/nonprofitbookkeeping-1.0-SNAPSHOT.jar`

- Run:

```
java -jar target/nonprofitbookkeeping-1.0-SNAPSHOT.jar
```

## Building and Deploying with Eclipse

- Install Eclipse IDE with Maven Integration (m2e)
- Import project: File → Import → Existing Maven Projects
- Build: Right-click project → Run As → Maven build… → Goals: `clean package`
- Launch: Right-click `NonprofitBookkeepingFX.java` → Run As → Java Application
- (Optional) Export a runnable JAR: File → Export → Java → Runnable JAR file
