import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Traverses directory given a path, finds each .txt file in the directory
 * passed in. Every .txt file it finds, it Parses the file, adding each word in
 * that file to the InvertedIndex.
 * 
 * @author Twye
 * 
 */
public class MultiThreadTraverserBuilder {

	private final WorkQueue workers;
	private int pending;
	private static final Logger logger = LogManager.getLogger();
	private ArrayList<Path> paths;
	private final MultiReaderLock lock;

	/**
	 * Creates new Work Queue and pending work variable
	 * 
	 * @param int threads
	 */
	public MultiThreadTraverserBuilder(int threads) {
		lock = new MultiReaderLock();
		workers = new WorkQueue(threads);
		paths = new ArrayList<Path>();
		pending = 0;
	}

	/**
	 * Sub-class that runs each thread that was spawned for every .txt file
	 * found in a given directory.
	 * 
	 * @author Twye
	 * 
	 */
	private class DirectoryMinion implements Runnable {

		private Path directory;
		private InvertedIndex wordI;

		public DirectoryMinion(Path directory, InvertedIndex index) {
			this.directory = directory;
			this.wordI = index;
			logger.debug("Created a directory worker for {}", directory);
			uppending();
		}

		@Override
		public void run() {
			try {
				ArrayList<Path> singledir = new ArrayList<Path>();
				for (Path path : Files.newDirectoryStream(directory)) {
					if (Files.isDirectory(path)) {
						workers.execute(new DirectoryMinion(path, this.wordI));
					} else if (path.toString().toLowerCase().endsWith(".txt")) {
						workers.execute(new BuilderMinion(path, this.wordI));
//						singledir.add(path);
					}

				}
//				lock.lockWrite();
//				paths.addAll(singledir);
//				lock.unlockWrite();

			} catch (Exception e) {
				logger.warn("Unable to traverse directory {}", directory);
				System.out.println("Unable to traverse " + directory);
				// e.printStackTrace();
			}
			downpending();
		}
	}

	/**
	 * Creates a new thread for every sub-directory found within original
	 * directory path passed in.
	 * 
	 * @param directory
	 * @param index
	 */
	public void addDirectory(Path directory, InvertedIndex index) {
		if (Files.isDirectory(directory)) {
			workers.execute(new DirectoryMinion(directory, index));
		} else if (directory.toString().toLowerCase().endsWith(".txt")) {
			System.out.println("Never gets here");
			lock.lockWrite();
			paths.add(directory);
			lock.unlockWrite();
		}

		finish();
		logger.debug("Finishing directory work");
	}

	/**
	 * Index Helper, Parses .txt file line by line and calls InvertedIndex.add()
	 * to add word from file to index
	 * 
	 * @param index
	 * @param file
	 * @throws IOException
	 */
//	public void directoryTraverseBuildIndex(Path directory, InvertedIndex index) {
//
//		addDirectory(directory, index);

//		logger.debug("Starting to build.");
//
//		for (Path x : paths) {
//			workers.execute(new BuilderMinion(x, index));
//		}
//		logger.debug("Finishing building.");
//		finish();
//	}

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
		logger.debug("pending is" + pending);
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
	
	/**
	 *Sub class for build index. Each Minion creates a index of their assigned file
	 *Adds all file index's to master index passed in
	 *
	 * 
	 */
	private class BuilderMinion implements Runnable {

		private Path file;
		private final InvertedIndex index;
		private final InvertedIndex fileIndex;

		/**
		 * BuilderMinion constructor takes a file to parse and the master index
		 * @param file
		 * @param index
		 */
		public BuilderMinion(Path file, InvertedIndex index) {

			this.file = file;
			this.index = index;
			this.fileIndex = new InvertedIndex();
			uppending();
		}

		/**
		 * builder subclass run method, for each file creates a InvertedIndex, 
		 * calls addAll to add each file index to master index.
		 * 
		 */
		@Override
		public void run() {
			IndexBuilder.build(this.index, this.file);
			downpending();
		}
	}
}
