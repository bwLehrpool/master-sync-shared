package org.openslx.filetransfer.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class ChunkList
{

	private static final Logger LOGGER = Logger.getLogger( ChunkList.class );

	/**
	 * Here we keep a list of all chunks in the proper order, in case we quickly need to access one
	 */
	private final List<FileChunk> allChunks;

	/**
	 * Chunks that are missing from the file
	 */
	private final List<FileChunk> missingChunks = new LinkedList<>();

	/**
	 * Chunks that are currently being uploaded or hash-checked
	 */
	private final List<FileChunk> pendingChunks = new LinkedList<>();

	private final List<FileChunk> completeChunks = new ArrayList<>( 100 );

	// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying
	private final ByteBuffer statusArray;

	// Do we need to keep valid chunks, or chunks that failed too many times?

	public ChunkList( long fileSize, List<byte[]> sha1Sums )
	{
		FileChunk.createChunkList( missingChunks, fileSize, sha1Sums );
		statusArray = ByteBuffer.allocate( missingChunks.size() );
		allChunks = new ArrayList<>( missingChunks );
	}

	/**
	 * Update the sha1sums of all chunks. This is meant to be used if you passed an incomplete list
	 * before (with null elements), so you don't have to has hthe whole file before starting the
	 * upload, but periodically update it while thie upload is running.
	 * 
	 * @param sha1Sums list of sums
	 */
	public synchronized void updateSha1Sums( List<byte[]> sha1Sums )
	{
		int index = 0;
		for ( byte[] sum : sha1Sums ) {
			if ( index >= allChunks.size() )
				break;
			if ( sum == null )
				continue;
			allChunks.get( index ).setSha1Sum( sum );
			index++;
		}
	}

	/**
	 * Get a missing chunk, marking it pending.
	 * 
	 * @return chunk marked as missing
	 * @throws InterruptedException
	 */
	public synchronized FileChunk getMissing() throws InterruptedException
	{
		if ( missingChunks.isEmpty() && pendingChunks.isEmpty() )
			return null;
		if ( missingChunks.isEmpty() ) {
			this.wait( 3000 );
			if ( missingChunks.isEmpty() )
				return null;
		}
		FileChunk c = missingChunks.remove( 0 );
		pendingChunks.add( c );
		return c;
	}

	/**
	 * Get the block status as byte representation.
	 */
	public synchronized ByteBuffer getStatusArray()
	{
		byte[] array = statusArray.array();
		for ( int i = 0; i < array.length; ++i ) {
			array[i] = allChunks.get( i ).getStatus().val;
		}
		return statusArray;
	}

	/**
	 * Get completed chunks as list
	 * 
	 * @return List containing all successfully transfered chunks
	 */
	public synchronized List<FileChunk> getCompleted()
	{
		return new ArrayList<>( completeChunks );
	}

	/**
	 * Get a chunk that is marked complete, has a sha1 hash, but has not been hash-checked yet.
	 * 
	 * @return chunk
	 */
	public synchronized FileChunk getUnhashedComplete()
	{
		for ( Iterator<FileChunk> it = completeChunks.iterator(); it.hasNext(); ) {
			FileChunk chunk = it.next();
			if ( chunk.sha1sum != null && chunk.status == ChunkStatus.HASHING ) {
				it.remove();
				pendingChunks.add( chunk );
				return chunk;
			}
		}
		return null;
	}

	/**
	 * Mark a chunk currently transferring as successfully transfered.
	 * 
	 * @param c The chunk in question
	 */
	public synchronized void markSuccessful( FileChunk c )
	{
		if ( !pendingChunks.remove( c ) ) {
			LOGGER.warn( "Inconsistent state: markTransferred called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!" );
			return;
		}
		c.setStatus( ChunkStatus.COMPETE );
		completeChunks.add( c );
		this.notifyAll();
	}

	/**
	 * Mark a chunk currently transferring or being hash checked as failed
	 * transfer. This increases its fail count and re-adds it to the list of
	 * missing chunks.
	 * 
	 * @param c The chunk in question
	 * @return Number of times transfer of this chunk failed
	 */
	public synchronized int markFailed( FileChunk c )
	{
		if ( !pendingChunks.remove( c ) ) {
			LOGGER.warn( "Inconsistent state: markTransferred called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!" );
			return -1;
		}
		// Add as first element so it will be re-transmitted immediately
		c.setStatus( ChunkStatus.MISSING );
		missingChunks.add( 0, c );
		this.notifyAll();
		return c.incFailed();
	}

	public synchronized boolean isComplete()
	{
		return missingChunks.isEmpty() && pendingChunks.isEmpty();
	}

}
