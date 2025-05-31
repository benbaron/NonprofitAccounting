package nonprofitbookkeeping.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    private Company company;

    @BeforeEach
    void setUp() {
        this.company = new Company();
    }

    // --- Tests for setCompanyFile() and getCompanyFile() ---

    @Test
    @DisplayName("getCompanyFile: Initially should return null")
    void testGetCompanyFile_initial_isNull() {
        assertNull(this.company.getCompanyFile(), "Initially, companyFile should be null.");
    }

    @Test
    @DisplayName("setCompanyFile/getCompanyFile: Set with a valid File object")
    void testSetAndGetCompanyFile_withValidFile_returnsSameFile() {
        File testFile = new File("/test/path/company.data");
        this.company.setCompanyFile(testFile);
        // assertSame checks for object identity, which should hold here.
        // assertEquals would also work if File.equals() is based on path.
        assertSame(testFile, this.company.getCompanyFile(), "getCompanyFile should return the File object that was set.");
    }

    @Test
    @DisplayName("setCompanyFile/getCompanyFile: Set with null should return null")
    void testSetCompanyFile_withNull_returnsNull() {
        // First set a file, then set to null
        File testFile = new File("/test/path/company.data");
        this.company.setCompanyFile(testFile);
        assertNotNull(this.company.getCompanyFile(), "Company file should be set before testing null assignment.");

        this.company.setCompanyFile(null);
        assertNull(this.company.getCompanyFile(), "getCompanyFile should return null after companyFile is set to null.");
    }

    // --- Tests for getParentFile() ---

    @Test
    @DisplayName("getParentFile: When companyFile is null, should return null")
    void testGetParentFile_whenCompanyFileIsNull() {
        this.company.setCompanyFile(null); // Ensure it's null
        assertNull(this.company.getParentFile(), "getParentFile should return null if companyFile is null.");
    }

    @Test
    @DisplayName("getParentFile: When companyFile has a parent, should return correct parent File")
    void testGetParentFile_whenCompanyFileHasParent_returnsCorrectParent() {
        File testFile = new File("/test/path/some/company.data");
        this.company.setCompanyFile(testFile);

        File expectedParent = new File("/test/path/some");
        File actualParent = this.company.getParentFile();

        assertNotNull(actualParent, "Parent file should not be null for a nested file path.");
        assertEquals(expectedParent.getAbsolutePath(), actualParent.getAbsolutePath(),
                     "getParentFile should return the correct parent directory.");
    }

    @Test
    @DisplayName("getParentFile: When companyFile is relative (filename only), should return null")
    void testGetParentFile_whenCompanyFileIsRelative_returnsNull() {
        File testFile = new File("company.data"); // Relative path with no parent component
        this.company.setCompanyFile(testFile);

        assertNull(this.company.getParentFile(),
                     "getParentFile should return null for a relative file with no parent path component.");
    }

    @Test
    @DisplayName("getParentFile: When companyFile is a root directory, should return null")
    void testGetParentFile_whenCompanyFileIsRoot_returnsNull() {
        // File.separator provides the correct root for the OS (e.g., "/" for Unix, "C:\" needs more care)
        // For a simple root like "/", getParentFile() returns null.
        // For "C:\", getParentFile() might return "C:". This test focuses on the typical "/" case.
        File testFile = new File(File.separator);
        this.company.setCompanyFile(testFile);

        // A single File.separator might be interpreted differently by File.getParentFile()
        // depending on OS and JDK version. A more robust test for "no parent" might be a filename.
        // However, for a path like "/", getParentFile() is typically null.
        // For a path like "C:", getParentFile() is null.
        // For "C:\file", parent is "C:\". For "C:\", parent is null.

        if (File.separator.equals("/")) { // Unix-like root
             assertNull(this.company.getParentFile(), "getParentFile should return null for the root directory '/' on Unix-like systems.");
        } else {
            // Windows: new File("C:\\") -> getParentFile() is null.
            // new File("C:") -> getParentFile() is null.
            // This test is a bit OS-dependent if we try to model specific drive letters.
            // Sticking to File.separator often implies a simple root for testing.
            // Let's test a file directly under root as well, where parent IS root.
            File fileInRoot = new File(File.separator + "somefile.txt");
            this.company.setCompanyFile(fileInRoot);
            File expectedParent = new File(File.separator);
            assertEquals(expectedParent.getAbsolutePath(), this.company.getParentFile().getAbsolutePath(),
                         "Parent of a file in root should be the root directory.");

            // And if companyFile IS the root itself:
            this.company.setCompanyFile(new File(File.separator));
            assertNull(this.company.getParentFile(), "Parent of the root directory itself should be null.");
        }
    }
}
