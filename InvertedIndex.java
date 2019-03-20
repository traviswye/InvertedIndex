import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a custom data structure indexing the location of each word in each file parsed
 * 
 */
public class InvertedIndex {

	
	private final TreeMap<String, TreeMap<String, ArrayList<Integer>>> wordIndex;
	private final MultiReaderLock lock;
	
	public InvertedIndex() {
		wordIndex = new TreeMap<>();
		this.lock = new MultiReaderLock();
	}

	/**
	 * Stores a word, the path where that word was found, and the position
	 * at that path the word was found.
	 * 
	 * @param String
	 *            word - Current word in the file/word list.
	 * @param String
	 *            path - String representation of the files Path that the word
	 *            is in.
	 * @param int l - Position of word in word list/in the file
	 */
	public void add(String word, String path, int locationinfile) {
		lock.lockWrite();
		if (!wordIndex.containsKey(word)) {
			wordIndex.put(word, new TreeMap<String, ArrayList<Integer>>());
		}

		if (!wordIndex.get(word).containsKey(path)) {
			wordIndex.get(word).put(path, new ArrayList<Integer>());
		}

		wordIndex.get(word).get(path).add(locationinfile);
		lock.unlockWrite();
	}
	

	public void addAll(InvertedIndex other) {
		lock.lockWrite();

		for (String word : other.wordIndex.keySet()) {
			
			if (this.wordIndex.containsKey(word) == false) {

				this.wordIndex.put(word, other.wordIndex.get(word));
			}
			else {

				for (String path : other.wordIndex.get(word).keySet()){
					
					if (wordIndex.get(word).containsKey(path) == false){
						
					wordIndex.get(word).put(path, other.wordIndex.get(word).get(path));
					}
					else{
						wordIndex.get(word).get(path).addAll(other.wordIndex.get(word).get(path));
					}
				}
			}

			
		}
		lock.unlockWrite();
	}

	/**
	 * Efficiently returns partial search results from your inverted index, such
	 * that any word in your inverted index that starts with a query word has its results
	 * returned.
	 * 
	 * @param List<String> queryWords
	 *            
	 */
	public ArrayList<SearchResult> search(List<String> queryWords) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		HashMap<String, SearchResult> pathresults = new HashMap<String, SearchResult>();
		lock.lockRead();
		for (String query : queryWords) {
			String curword = wordIndex.ceilingKey(query);
			
			while (curword != null) {
				if (!curword.startsWith(query)) {
					break;
				}
				
				int totalcount;
				int firstloc;
				
				for (String path : wordIndex.get(curword).keySet()) {
					totalcount = wordIndex.get(curword).get(path).size();
					firstloc = wordIndex.get(curword).get(path).get(0);
					
					if (pathresults.containsKey(path)) {
						pathresults.get(path).updateAddResults(totalcount, firstloc);
					} else {
						SearchResult updatingPath = new SearchResult(path, totalcount,
								firstloc);
						pathresults.put(path, updatingPath);
					}
				}
				
				curword = wordIndex.higherKey(curword);
			}

		}
		lock.unlockRead();
		results.addAll(pathresults.values());
		Collections.sort(results);
		return results;
	}

	/**
	 * Prints the inverted index to a passed in file.
	 * 
	 * 
	 * @param outpath
	 *            - Location Where the Index will be written too
	 */
	public void print(String outpath) {
		Path opath = Paths.get(outpath).toAbsolutePath().normalize();
		lock.lockRead();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				opath, Charset.forName("UTF-8")));
		) {
			for (String f : wordIndex.keySet()) {

				writer.print(f);
				for (String p : wordIndex.get(f).keySet()) {
					writer.print("\n" + "\"" + p + "\"");
					for (Integer i : wordIndex.get(f).get(p)) {
						writer.print(", " + i);
					}
				}
				writer.println();
				writer.println();
			}
		} catch (IOException e) {
			System.out.println("Unable to write index to " + outpath);

		}
		lock.unlockRead();
	}
	
}
