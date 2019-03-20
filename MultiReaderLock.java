/**
 * A simple custom lock that allows simultaneously read operations, but
 * disallows simultaneously write and read/write operations.
 * 
 * You do not need to implement any form or priority to read or write
 * operations. The first thread that acquires the appropriate lock should be
 * allowed to continue.
 */
public class MultiReaderLock {
	private int readers;
	private int writers;

	/**
	 * Initializes a multi-reader (single-writer) lock.
	 */
	public MultiReaderLock() {
		readers = 0;
		writers = 0;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockRead() {
		while (writers > 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.out.println("Exception caught in lock read");

			}
		}
		readers++;
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockRead() {
		if (readers > 0) {
			this.readers--;
			if (readers == 0) {
				this.notifyAll();
			}
		}

	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockWrite() {

		while (writers > 0 || readers > 0) {
			try {
				this.wait();

			} catch (InterruptedException e) {
				System.out.println("Exception caught in lock read");

			}

		}
		this.writers++;

	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockWrite() {
		if (this.writers > 0) {
			this.writers--;
			notifyAll();
		}
	}

}