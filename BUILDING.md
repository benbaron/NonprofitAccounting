# BUILDING

## H2 Database Setup Checklist

This project is being migrated from serialized data files to a relational H2 database. The following steps outline the remaining work required to complete the transition.

### 1. Initialize the Database
- Switch the JDBC URL from the in-memory configuration (`jdbc:h2:mem:nonprofit`) to a file-backed URL, e.g. `jdbc:h2:file:./data/nonprofit;AUTO_SERVER=TRUE`.
- Remove or revisit `hibernate.hbm2ddl.auto=update`; use explicit migrations (Flyway or Liquibase) or validation for schema changes.
- Consolidate `EntityManagerFactory` creation so all initialization occurs in a single class.

### 2. Start the Database Engine
- Start an embedded H2 TCP server using `Server.createTcpServer().start()` before creating the `EntityManagerFactory` if remote connections are needed.
- Register a shutdown hook to stop the server when the application exits.

### 3. Operate the Database (CRUD)
- Continue using repository classes with proper transaction boundaries.
- Consider a base repository or shared exception handling to avoid code duplication.
- Ensure all `EntityManager` instances are closed to prevent file locks on the database.

### 4. Back Up the Database
- Implement a backup service using H2's `SCRIPT TO`, `BACKUP TO`, or direct file copy.
- Expose backup and restore operations through the UI or command-line interfaces.

### 5. Create a New Company
- Annotate domain models like `Company`, `CompanyProfileModel`, `Ledger`, and `ChartOfAccounts` with `@Entity` and define identifiers.
- Develop a `CompanyRepository` to persist and retrieve company metadata.
- Expand `DatabaseService` to persist and load profiles, ledgers, and charts of accounts through the repository layer.

### 6. Other Operations
- Implement a clean shutdown routine that closes the `EntityManagerFactory` and stops the H2 server.
- Add connection pooling (e.g., HikariCP) for better performance.
- Provide migration utilities for importing legacy data into the new schema.

## Vendoring Maven Dependencies for Offline Builds

To run tests without network access, prefetch required plugins and their dependencies into a local repository using the Maven Dependency Plugin.

1. **Add the plugin to `pom.xml`**

   ```xml
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
                 <!-- list each plugin or dependency to vendor -->
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

2. **List the artifacts**

   Add `<artifactItem>` entries for each required plugin (e.g., resources, compiler, surefire, exec) specifying `groupId`, `artifactId`, `version`, and `type` `maven-plugin`.

3. **Run the plugin**

   ```bash
   mvn dependency:copy@copy-maven-plugins
   ```

4. **Use the vendored repository**

   Run Maven with the vendored repository in offline mode:

   ```bash
  mvn -Dmaven.repo.local=./maven-repo --offline test
  ```

## Building and Deploying with Maven

1. Install JDK 17 and [Apache Maven](https://maven.apache.org/).
2. (Optional) Populate `maven-repo/` by running the vendoring goal above so builds can run offline.
3. Build the application:

   ```bash
   mvn -Dmaven.repo.local=./maven-repo --offline clean package
   ```

   The assembled JAR is written to `target/nonprofitbookkeeping-1.0-SNAPSHOT.jar`.
4. Run the application from the command line:

   ```bash
   java -jar target/nonprofitbookkeeping-1.0-SNAPSHOT.jar
   ```

## Building and Deploying with Eclipse

1. Install [Eclipse IDE](https://www.eclipse.org/ide/) with the **Maven Integration (m2e)** plugin.
2. Import the project:
   - *File → Import → Existing Maven Projects*
   - Select the repository root and finish the wizard.
3. Build within Eclipse:
   - Right‑click the project → *Run As → Maven build...*
   - Enter `clean package` as the goals and run.
4. Launch the application:
   - Right‑click `src/main/java/nonprofitbookkeeping/ui/NonprofitBookkeepingFX.java`
   - Choose *Run As → Java Application*.
5. (Optional) Export a runnable JAR via *File → Export → Java → Runnable JAR file* for distribution.

