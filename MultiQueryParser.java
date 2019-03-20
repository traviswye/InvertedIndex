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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a MultiQueryParser storing each query word with an array list of all
 * of the result objects for that given query word. Uses a linkedHashMap to
 * maintain the order of each word/results passed in.
 * 
 * Parses query file searching for each line as its read in.
 */
public class MultiQueryParser {

	/** Maps query line to the search results for that line. */
	private final LinkedHashMap<String, ArrayList<SearchResult>> resultmap;
	private final WorkQueue workers;
	private final MultiReaderLock lock;
	private int pending;
	private static final Logger logger = LogManager.getLogger();

	public MultiQueryParser(int threads) {
		this.resultmap = new LinkedHashMap<>();
		this.workers = new WorkQueue(threads);
		this.lock = new MultiReaderLock();
		this.pending = 0;
	}

	/**
	 * Prints the LinkedHashMap to a results file.
	 * 
	 * @param String
	 *            outpath - where the file is wrote to
	 */
	public void print(String outpath) {

		Path opath = Paths.get(outpath).toAbsolutePath().normalize();
		lock.lockRead();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				opath, Charset.forName("UTF-8")));) {
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
		lock.unlockRead();
	}

	/**
	 * Reads in query file, puts each line into the search result map and adds new work 
	 * 
	 * </em> </p>
	 * 
	 * @param path
	 *            - file path to open
	 * @return list of cleaned words
	 * @throws IOException
	 */
	public void searchHelp(InvertedIndex index, Path querypath)
			throws IOException {

		try (BufferedReader reader = Files.newBufferedReader(querypath,
				Charset.forName("UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = FileParser.cleanText(line);
				if (line.isEmpty()) {
					continue;
				}
				logger.debug("Spawning new SearchWorker");
				lock.lockWrite();
				resultmap.put(line, null);
				lock.unlockWrite();
				workers.execute(new SearchMinion(line, index));

			}

		}
		finish();
		logger.debug("finishing work");
	}

	/**
	 * Inner class that breaks up the query line and searches for the occurrences
	 * in the index.
	 * 
	 * Returns the ArrayList of results and updates the initial empty Results
	 * ArrayList.
	 * 
	 * @author Twye
	 * 
	 */
	public class SearchMinion implements Runnable {

		private String line;
		private InvertedIndex index;
		
		/**Constructor for SearchMinion, takes a line from the file and the master index
		 * 
		 * @param line
		 * @param index
		 */
		public SearchMinion(String line, InvertedIndex index) {
			this.line = line;
			this.index = index;
			uppending();
		}
		/**
		 * run method: Calls search and updates the result map with what is returned
		 */
		public void run() {
			List<String> subword = (FileParser.parseText(this.line));
			ArrayList<SearchResult> results = this.index.search(subword);
			logger.debug("Updating ResultMap for: " + line);
			lock.lockWrite();
			resultmap.put(line, results);
			lock.unlockWrite();
			logger.debug("FINISHED search for: " + line);
			downpending();
		}
	}

	/**
	 * Increments pending by 1
	 */
	private synchronized void uppending() {
		this.pending++;
		logger.debug("pending work " + pending);
	}

	/**
	 * Decrements pending by 1, Notify threads if no work is pending
	 */
	private synchronized void downpending() {
		this.pending--;
		if (this.pending == 0) {
			notifyAll();
		}
		logger.debug("pending is " + pending);
	}

	/**
	 * Waits until there is no pending work left
	 */
	private synchronized void finish() {
		while (pending > 0) {
			try {
				logger.debug("waiting...");
				this.wait();
				logger.debug("waiting over!!");
			} catch (InterruptedException e) {
				logger.debug("Unable to finish work", e);
			}

		}
	}

	/**
	 * calls finish() then shuts down workers
	 */
	public void shutdown() {
		finish();
		workers.shutdown();
		logger.debug("shutdown");
	}

}