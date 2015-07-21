package org.openslx.filetransfer.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class ChunkList {

	private static final Logger LOGGER = Logger.getLogger(ChunkList.class);

	/**
	 * Chunks that are missing from the file
	 */
	private final List<FileChunk> missingChunks = new LinkedList<>();

	/**
	 * Chunks that are currently being uploaded or hash-checked
	 */
	private final List<FileChunk> pendingChunks = new LinkedList<>();

	private final List<FileChunk> completeChunks = new ArrayList<>(100);

	// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying
	private final ByteBuffer statusArray;

	// Do we need to keep valid chunks, or chunks that failed too many times?

	public ChunkList(long fileSize, List<ByteBuffer> sha1Sums) {
		FileChunk.createChunkList(missingChunks, fileSize, sha1Sums);
		statusArray = ByteBuffer.allocate(missingChunks.size());
	}

	/**
	 * Get a missing chunk, marking it pending.
	 * 
	 * @return chunk marked as missing
	 */
	public synchronized FileChunk getMissing() {
		if (missingChunks.isEmpty())
			return null;
		FileChunk c = missingChunks.remove(0);
		pendingChunks.add(c);
		return c;
	}

	/**
	 * Get the block status as byte representation.
	 */
	public synchronized ByteBuffer getStatusArray() {
		byte[] array = statusArray.array();
		//Arrays.fill(array, (byte)0);
		for (FileChunk c : missingChunks) {
			array[c.getChunkIndex()] = 1;
		}
		for (FileChunk c : pendingChunks) {
			array[c.getChunkIndex()] = 2;
		}
		for (FileChunk c : completeChunks) {
			array[c.getChunkIndex()] = 0;
		}
		return statusArray;
	}

	/**
	 * Get completed chunks as list
	 * 
	 * @return List containing all successfully transfered chunks
	 */
	public synchronized List<FileChunk> getCompleted() {
		return new ArrayList<>(completeChunks);
	}

	/**
	 * Mark a chunk currently transferring as successfully transfered.
	 * 
	 * @param c The chunk in question
	 */
	public synchronized void markSuccessful(FileChunk c) {
		if (!pendingChunks.remove(c)) {
			LOGGER.warn("Inconsistent state: markTransferred called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!");
			return;
		}
		completeChunks.add(c);
	}

	/**
	 * Mark a chunk currently transferring or being hash checked as failed
	 * transfer. This increases its fail count and re-adds it to the list of
	 * missing chunks.
	 * 
	 * @param c The chunk in question
	 * @return Number of times transfer of this chunk failed
	 */
	public synchronized int markFailed(FileChunk c) {
		if (!pendingChunks.remove(c)) {
			LOGGER.warn("Inconsistent state: markTransferred called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!");
			return -1;
		}
		// Add as first element so it will be re-transmitted immediately
		missingChunks.add(0, c);
		return c.incFailed();
	}

	public synchronized boolean isComplete() {
		return missingChunks.isEmpty() && pendingChunks.isEmpty();
	}

}
