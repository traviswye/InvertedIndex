import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.charset.CharsetDecoder;
//import java.nio.charset.CodingErrorAction;
//import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;

public class DirectoryTraverser {

	/**
	 * Method traverses trough all the files within the passed directory, It
	 * will collect all the paths of files that are text files (.txt) storing
	 * them in an ArrayList.
	 * 
	 * The recursive version of this method is private. Users of this class will
	 * have to use the public version (see below).
	 * 
	 * @param myfiles
	 *            ArrayList that all files ending in .txt are stored to
	 * @param path
	 *            to retrieve the listing, assumes a directory and not a file is
	 *            passed
	 * @throws IOException
	 */
	private static void traverse(ArrayList<Path> myfiles, Path path)
			throws IOException {
		/*
		 * The try-with-resources block makes sure we close the directory stream
		 * when done, to make sure there aren't any issues later when accessing
		 * this directory.
		 */

		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			// Efficiently iterate through the files and subdirectories.
			for (Path file : listing) {
				// If the current file ends with .txt or .TXT it adds the path
				// to the file array.
				if (file.toString().toLowerCase().endsWith(".txt")) {
					myfiles.add(file.toAbsolutePath().normalize());
				}
				// If it is a subdirectory, recursively traverse.
				if (Files.isDirectory(file)) {
					traverse(myfiles, file);
				}
			}

		}
	}

	/**
	 * Safely starts the recursive traversal. Users of this class can access
	 * this method, so some validation is required. Outputs console message if
	 * exception is thrown.
	 * 
	 * @param directory
	 *            to traverse
	 * @throws IOException
	 */
	public static ArrayList<Path> traverse(Path directory) throws IOException {
		ArrayList<Path> files = new ArrayList<>();

		if (Files.isDirectory(directory)) {
			traverse(files, directory);
		} else {
			System.out.println(directory.getFileName()
					+ " is a file not a directory");

		}

		return files;
	}

}