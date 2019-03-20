
/**
 * This class is a custom object containing the results of each word. a Results
 * object stores (String path, int totalcount, int firstloc)
 * 
 * This class has an overridden compareTo method that allows us to sort our
 * search results by the number of total times query appears in file followed by the first
 * location in the file, if there is to be a tie amongst these two criteria in the Results,
 * it is sorted by the path name.
 * 
 *  Something like:
 * 
 * Stores search results for easy sorting by frequency and initial position.
 */
public class SearchResult implements Comparable<SearchResult> {

	private final String path;
	private int totalcount;
	private int firstloc;

	/**
	 * Initializes results with the specified path, count, and location.
	 * 
	 * @param path - path where query was found
	 * @param totalcount - total of times the query was found at the path
	 * @param firstloc - initial position query was found at the path
	 */
	public SearchResult(String path, int totalcount, int firstloc) {
		this.path = path;
		this.totalcount = totalcount;
		this.firstloc = firstloc;
	}
	
	/**
	 * returns the path of the Results object
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * returns the totalcount of the Results object
	 * @return totalcount
	 */
	public int getTotalcount() {
		return totalcount;
	}
	
	/**
	 * Adds up all the counts for query words in that path.
	 * @param totalcount
	 */
	public void addFrequency(int totalcount) {
		this.totalcount += totalcount;
	}
	
	/**
	 * returns the firstloc of the Results object
	 * @return
	 */
	public int getFirstloc() {
		return firstloc;
	}
	
	/**
	 * Sets lower first location to the objects first location.
	 * @param firstloc
	 */
	public void updateLocation(int firstloc) {
		if (firstloc < this.firstloc) {
			this.firstloc = firstloc;
		}
	}

	/**
	 * updates the totalcount and the firstloc of a Result object at the same time.
	 * Used in the case that the path already exists when compiling all the
	 * search results.
	 */
	public void updateAddResults(int totalcount, int firstloc) {
		addFrequency(totalcount);
		updateLocation(firstloc);
	}

	@Override
	public String toString() {
		return "\"" + path + "\"" + ", " + totalcount + ", " + firstloc;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 *      Allows sorting by highest totalcount, then lowest firstloc, then
	 *      path (alphabetical)
	 */

	@Override
	public int compareTo(SearchResult cur) {
		if (this.totalcount != cur.totalcount) {
			return Integer.compare(cur.totalcount, this.totalcount);
		}

		else {
			if (this.firstloc != cur.firstloc) {
				return Integer.compare(this.firstloc, cur.firstloc);
			} else {
				return String.CASE_INSENSITIVE_ORDER.compare(this.path,
						cur.path);
			}
		}
	}
}
