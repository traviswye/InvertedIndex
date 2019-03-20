import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 
 * - Address ALL of the warnings.
 * - Fix ALL of your formatting.
 * - Fix ALL of your exception handling.
 * - Fix ALL of your Javadoc comments.
 * 
 * I did not mark every occurrence, so go through all of your files
 * to find and fix these issues.
 * 
 * I will not pass your project until all of the above is addressed. 
 * We do not have much time remaining, so try to avoid losing time to
 * those easy-to-fix issues.
 * 
 * View the messages in the other files as well.
 * 
 * Your runtimes compared to project 2 are as follows:
 * 		Project 2: 11.0409 seconds
 * 		Project 3: 14.0558 seconds
 * 		Your speed up is 0.79!
 * 
 * This means your multithreaded code is slower than single-threaded. See
 * the comments for areas to improve efficiency.
 * 
 * You will need to RESUBMIT this project. If you have questions about
 * any of the comments, please stop by office hours or post on Piazza.
 */


/**
 * 
 * Recursively processes all text files in a directory and builds an inverted
 * index to store the mapping from words to the documents (and position within
 * those documents) where those words were found
 */
public class Driver {

	/**
	 * Main: Parses command line arguments creating an argument parser. Creates
	 * an inverted index containing each word, file, and position of each word
	 * in each file of the passed directory.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {

		ArrayList<Path> myfiles = new ArrayList<Path>();
		boolean multid= false;
		int defthreads = 5;
		ArgumentParser parser = new ArgumentParser(args);
		InvertedIndex invertedIndex = new InvertedIndex();

		
		if (parser.numFlags() == 0) {
			System.out
			.println("Please pass in valid argument flags. You didnt pass any in!");
			return;
		}

		if (parser.hasFlag("-t")){
			int uservalue =0;
			multid = true;
			if (parser.hasValue("-t")){		
				try {
					uservalue = Integer.parseInt(parser.getValue("-t"));
				} catch (NumberFormatException e) {
					System.out.println("Improper -t flag value, running with default threads: 5");
				}
				if (uservalue> 5 || uservalue<0){
					defthreads = 5;
				}

				else if (uservalue>0 && uservalue <6 ){
					defthreads =uservalue;
				}
			}

		}
		else{
//			System.out.println("You are running single threaded");
		}

		MultiThreadTraverserBuilder buildmydirect = new MultiThreadTraverserBuilder(defthreads);
		if (parser.hasFlag("-d")) {		
			if (parser.hasValue("-d")) {
				if (multid == true){
					System.out.println("multithreaded");
//					buildmydirect.directoryTraverseBuildIndex(Paths.get(parser.getValue("-d")), invertedIndex);
					
					buildmydirect.addDirectory(Paths.get(parser.getValue("-d")), invertedIndex);
					buildmydirect.shutdown();
				}else if (multid ==false){
					try {
//						System.out.println("single threaded");
						myfiles = DirectoryTraverser.traverse(Paths.get(parser
								.getValue("-d")));
						IndexBuilder.build(invertedIndex, myfiles);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				
			}

			else {
				System.out.println("Please check your directory path");
				return;
			}
		} else {
			System.out
			.println("Please enter a valid flag for your directory: -d");
			return;
		}

		if (parser.hasFlag("-i")) {
			if (parser.hasValue("-i")) {
				// print to this value
				invertedIndex.print(parser.getValue("-i"));
			} else {
				// print index.txt
				Path defpath = Paths.get(("index.txt")).toAbsolutePath()
						.normalize();
				invertedIndex.print(defpath.toString());
			}
		} else {
			System.out
			.println("Please enter a output flag: -i if you wish to get an output file.");

		}
		QueryParser searchResultMap = new QueryParser();
		MultiQueryParser ResultMap = new MultiQueryParser(defthreads);


		if (parser.hasFlag("-q")) {
			if (parser.hasValue("-q")) {
				Path querypath = Paths.get(parser.getValue("-q"))
						.toAbsolutePath().normalize();
				if (multid == true){
					try {
						ResultMap.searchHelp(invertedIndex, querypath);
						ResultMap.shutdown();
					} catch (IOException e) {
						System.out.println("Problem with your query file, please check the path");
					}
				}
				else if (multid==false){
					try {
						searchResultMap.SearchHelp(invertedIndex, querypath);
					} catch (IOException e) {
						System.out.println("Problem with your query file, please check the path");
					}
				}
			} else {
				System.out
				.println("Enter a -q flag with a value(Querypath) if you wish to search the index for files in a query word");
			}
			
		}
		if (parser.hasFlag("-r")) {
			if (parser.hasValue("-r")) {
				if (multid==true){
					ResultMap.print(parser.getValue("-r"));
				}else{
				// print to this value
				searchResultMap.print(parser.getValue("-r"));
				}
			} else {
				// print to result.txt
				Path defRpath = Paths.get(("results.txt")).toAbsolutePath()
						.normalize();
				
				if (multid==true){
					ResultMap.print(defRpath.toString());
				}else{
				// print to this value
				searchResultMap.print(defRpath.toString());
				}
			}

		} else {
			System.out
			.println("Please enter a -r flag if you wish to get a search output file");
			
		}

	}

}