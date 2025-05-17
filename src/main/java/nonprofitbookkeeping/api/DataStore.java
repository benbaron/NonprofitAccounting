// File: DataStore.java
package nonprofitbookkeeping.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

/**
 * Abstraction for JSON-backed persistence of domain objects.
 * <p>
 * Provides methods to load and save arbitrary objects to/from files,
 * as well as listing files in a directory with a given extension.
 * </p>
 */
public interface DataStore {

    /**
     * Loads an instance of the given type from the specified file.
     *
     * @param <T>   the type of the object to load
     * @param type  the Class of the object to load
     * @param file  the file to read from (expected to contain JSON)
     * @return the deserialized object
     * @throws IOException if an error occurs while reading or parsing the file
     * @throws ActionCancelledException if file chooser is cancelled
     * @throws NoFileCreatedException 
     */
    <T> T load(Class<T> type, File file) throws IOException, ActionCancelledException, NoFileCreatedException;

    /**
     * Saves the given object to the specified file in JSON format.
     *
     * @param obj   the object to serialize
     * @param file  the file to write to
     * @throws IOException if an error occurs while writing the file
     * @throws ActionCancelledException if file chooser is cancelled
     * @throws NoFileCreatedException 
     */
    void save(Object obj, File file) throws IOException, ActionCancelledException, NoFileCreatedException;

    /**
     * Lists all files in the given directory (non-recursively) whose names end
     * with the specified extension.
     *
     * @param directory   the directory to search
     * @param extension   the file extension filter (e.g. ".npbk" or "json")
     * @return a List of matching File instances; empty if none found or directory invalid
     */
    List<File> listFiles(File directory, String extension);
}
