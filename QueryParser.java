import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Creates a QueryParser storing each query word with an array list of all
 * of the result objects for that given query word. Uses a linkedHashMap to
 * maintain the order of each word/results passed in.
 * 
 * Parses query file searching for each line as its read in.
 */
public class QueryParser {

	/** Maps query line to the search results for that line. */
	private final LinkedHashMap<String, ArrayList<SearchResult>> resultmap;

	public QueryParser() {
		resultmap = new LinkedHashMap<>();
	}

	/**
	 * Prints the LinkedHashMap to a results file.
	 * 
	 * @param String
	 *            outpath - where the file is wrote to
	 */
	public void print(String outpath) {
		Path opath = Paths.get(outpath).toAbsolutePath().normalize();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				opath, Charset.forName("UTF-8")));
		) {
			for (String queryword : resultmap.keySet()) {
				writer.println(queryword);
				for (SearchResult result : resultmap.get(queryword)) {
					writer.println(result.toString());
				}
				writer.println();
			}
		} catch (IOException e) {
			System.out.println("Problem writing result map to file");
		}

	}
	
	/**
	 * Reads the query file line-by-line and parses the resulting line into words using
	 * the CleanSearchText method. Searches for the cleaned parsed line adding the line and 
	 * each search result for the subwords in that line to the QueryParser/searchresultmap
	 * 
	 * </em>
	 * </p>
	 * 
	 * @param path
	 *            - file path to open
	 * @return list of cleaned words
	 * @throws IOException
	 */
	
	public void SearchHelp(InvertedIndex index, Path querypath) throws IOException{
		
		List<String> temp;
		try (BufferedReader reader = Files.newBufferedReader(querypath,
				Charset.forName("UTF-8"))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = FileParser.cleanText(line);
				if (line.isEmpty()) {
					continue;
				}
				temp = (FileParser.parseText(line));
				resultmap.put(line, index.search(temp));

			}
		}
	}
}