import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that Builds an inverted index from text files
 * 
 */
public class IndexBuilder {

	/**
	 * "Builds an index from a list of text files."
	 * 
	 * @param myfiles
	 * @return
	 */
	public static void build(InvertedIndex index, ArrayList<Path> files) {
		for (Path file : files) {
			build(index, file);
		}
	}

	/**
	 * Index Helper, Parses .txt file line by line and calls InvertedIndex.add()
	 * to add word from file to index
	 * 
	 * @param index
	 * @param file
	 * @throws IOException
	 */
	public static void build(InvertedIndex index, Path file) {

//		String[] filewords;

		try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))
				) {
			InvertedIndex fileindex = new InvertedIndex();
			String line;
			int wordlocation = 1;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				// temp = FileParser.parseText(line);
				line = FileParser.cleanText(line);
				// filewords = line.split("\\s");
				for (String w : line.split("\\s")) {
					if (w.length() > 0) {
						fileindex.add(w, file.toString(), wordlocation);
						wordlocation++;
					}

				}

			}
			index.addAll(fileindex);
		} catch (IOException | NullPointerException e) {
			System.out.println("Issue reading file");
		}

	}
}
