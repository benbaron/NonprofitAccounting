package nonprofitbookkeeping.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Architecture guard: top-level impex *Record types should have a matching
 * repository in persistence/impex.
 */
class ImpexRecordCoverageTest
{
	@Test
	void topLevelImpexRecordsHaveRepositories() throws IOException
	{
		Path repoRoot = Path.of("").toAbsolutePath();
		Path impexModelDir =
			repoRoot.resolve("src/main/java/nonprofitbookkeeping/model/impex");
		Path impexRepoDir =
			repoRoot.resolve("src/main/java/nonprofitbookkeeping/persistence/impex");

		try (Stream<Path> modelStream = Files.list(impexModelDir);
		     Stream<Path> repoStream = Files.list(impexRepoDir))
		{
			Set<String> topLevelRecords = modelStream
				.map(Path::getFileName)
				.map(Path::toString)
				.filter(n -> n.endsWith("Record.java"))
				.filter(n -> !n.equals("package-info.java"))
				.map(n -> n.substring(0, n.length() - ".java".length()))
				.collect(Collectors.toCollection(TreeSet::new));

			Set<String> repositories = repoStream
				.map(Path::getFileName)
				.map(Path::toString)
				.filter(n -> n.endsWith("Repository.java"))
				.map(n -> n.substring(0, n.length() - "Repository.java".length()))
				.collect(Collectors.toCollection(TreeSet::new));

			Set<String> missing = topLevelRecords.stream()
				.filter(record -> !repositories.contains(record))
				.collect(Collectors.toCollection(TreeSet::new));

			assertTrue(missing.isEmpty(),
				"Missing impex repositories for: " + missing);
		}
	}
}
