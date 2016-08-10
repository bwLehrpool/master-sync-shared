package org.openslx.filetransfer.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openslx.util.ThriftUtil;

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

	// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying, 5 = hashing
	private final ByteBuffer statusArray;

	/**
	 * True if at least one block has a checksum set
	 */
	private boolean hasChecksum = false;

	// Do we need to keep valid chunks, or chunks that failed too many times?

	public ChunkList( long fileSize, List<byte[]> sha1Sums )
	{
		FileChunk.createChunkList( missingChunks, fileSize, sha1Sums );
		statusArray = ByteBuffer.allocate( missingChunks.size() );
		allChunks = Collections.unmodifiableList( new ArrayList<>( missingChunks ) );
	}

	/**
	 * Update the sha1sums of all chunks. This is meant to be used if you passed an incomplete list
	 * before (with null elements), so you don't have to hash the whole file before starting the
	 * upload, but periodically update it while the upload is running.
	 * 
	 * @param sha1Sums list of sums
	 */
	public synchronized void updateSha1Sums( List<byte[]> sha1Sums )
	{
		int index = 0;
		for ( byte[] sum : sha1Sums ) {
			if ( index >= allChunks.size() )
				break;
			if ( sum != null ) {
				allChunks.get( index ).setSha1Sum( sum );
				if ( !hasChecksum ) {
					hasChecksum = true;
				}
			}
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
			this.wait( 6000 );
			if ( missingChunks.isEmpty() )
				return null;
		}
		FileChunk c = missingChunks.remove( 0 );
		c.setStatus( ChunkStatus.UPLOADING );
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
			FileChunk chunk = allChunks.get( i );
			ChunkStatus status = chunk.getStatus();
			if ( hasChecksum && status == ChunkStatus.COMPLETE && chunk.getSha1Sum() == null ) {
				array[i] = ChunkStatus.HASHING.val;
			} else {
				array[i] = chunk.getStatus().val;
			}
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
	 * Set status of blocks according to given "ismissing" list. Intended to be called
	 * right after creating the list, in case we have a local file already and want to
	 * resume downloading.
	 */
	public synchronized void resumeFromStatusList( List<Boolean> statusList, long fileLength )
	{
		if ( !completeChunks.isEmpty() || !pendingChunks.isEmpty() ) {
			LOGGER.warn( "Inconsistent state: resume called when not all chunks are marked missing" );
		}
		int index = 0;
		for ( Boolean missing : statusList ) {
			FileChunk chunk = allChunks.get( index );
			if ( fileLength != -1 && fileLength < chunk.range.endOffset )
				break; // Stop, file is shorter than end of this chunk
			if ( missingChunks.remove( chunk ) || pendingChunks.remove( chunk ) ) {
				completeChunks.add( chunk );
			}
			if ( missing ) {
				// Trigger hashing
				chunk.setStatus( ChunkStatus.HASHING );
			} else {
				// Assume complete
				chunk.setStatus( ChunkStatus.COMPLETE );
			}
			index++;
		}
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
	public synchronized void markCompleted( FileChunk c, boolean hashCheckSuccessful )
	{
		if ( !pendingChunks.remove( c ) ) {
			LOGGER.warn( "Inconsistent state: markSuccessful called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!" );
			return;
		}
		c.setStatus( ( hashCheckSuccessful || c.getSha1Sum() == null ) ? ChunkStatus.COMPLETE : ChunkStatus.HASHING );
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
			LOGGER.warn( "Inconsistent state: markFailed called for Chunk " + c.toString()
					+ ", but chunk is not marked as currently transferring!" );
			return -1;
		}
		// Add as first element so it will be re-transmitted immediately
		c.setStatus( ChunkStatus.MISSING );
		missingChunks.add( 0, c );
		this.notifyAll();
		return c.incFailed();
	}

	/**
	 * Mark a missing chunk as complete.
	 */
	private synchronized boolean markMissingAsComplete( int index )
	{
		FileChunk chunk = allChunks.get( index );
		if ( completeChunks.contains( chunk ) )
			return true;
		if ( !missingChunks.remove( chunk ) ) {
			LOGGER.warn( "Inconsistent state: markMissingAsComplete called for chunk " + chunk.toString() + " (indexed as " + index
					+ ") which is not missing" );
			return false;
		}
		chunk.setStatus( ChunkStatus.COMPLETE );
		completeChunks.add( chunk );
		this.notifyAll();
		return true;
	}

	/**
	 * Check if all blocks in this list are marked as successfully transfered. If a complete chunk is
	 * marked as "hashing", or if there are some complete chunks without a sha1sum and some with a
	 * sha1sum, the transfer is considered incomplete.
	 * 
	 * @return true iff transfer is complete
	 */
	public synchronized boolean isComplete()
	{
		if ( !missingChunks.isEmpty() || !pendingChunks.isEmpty() )
			return false;
		boolean sawWithHash = false;
		for ( FileChunk chunk : completeChunks ) {
			if ( chunk.status == ChunkStatus.HASHING )
				return false;
			if ( chunk.sha1sum != null ) {
				sawWithHash = true;
			} else if ( chunk.sha1sum == null && sawWithHash ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public synchronized String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( '{' );
		for ( FileChunk chunk : allChunks ) {
			sb.append( '[' );
			sb.append( chunk.getChunkIndex() );
			if ( chunk.getSha1Sum() != null )
				sb.append( '+' );
			////
			switch ( chunk.status ) {
			case COMPLETE:
				sb.append( 'C' );
				break;
			case COPYING:
				sb.append( '>' );
				break;
			case HASHING:
				sb.append( 'H' );
				break;
			case MISSING:
				sb.append( 'M' );
				break;
			case QUEUED_FOR_COPY:
				sb.append( 'Q' );
				break;
			case UPLOADING:
				sb.append( 'P' );
				break;
			default:
				sb.append( '?' );
				break;

			}
			sb.append( '|' ); ////////////
			if ( missingChunks.contains( chunk ) )
				sb.append( 'M' );
			if ( pendingChunks.contains( chunk ) )
				sb.append( 'P' );
			if ( completeChunks.contains( chunk ) )
				sb.append( 'C' );
			sb.append( ']' );
		}
		sb.append( '}' );
		return sb.toString();
	}

	public synchronized boolean isEmpty()
	{
		return allChunks.isEmpty();
	}

	public List<FileChunk> getAll()
	{
		return allChunks;
	}

	public synchronized String getQueueName( FileChunk chunk )
	{
		if ( missingChunks.contains( chunk ) )
			return "missing";
		if ( pendingChunks.contains( chunk ) )
			return "pending";
		if ( completeChunks.contains( chunk ) )
			return "completed";
		return "NOQUEUE";
	}

	public static boolean hashListsEqualFcBb( List<FileChunk> one, List<ByteBuffer> two )
	{
		return hashListsEqualFcArray( one, ThriftUtil.unwrapByteBufferList( two ) );
	}

	public static boolean hashListsEqualFcArray( List<FileChunk> one, List<byte[]> two )
	{
		if ( one.size() != two.size() )
			return false;
		FileChunk first = one.get( 0 );
		if ( first == null || first.getSha1Sum() == null )
			return false;
		Iterator<byte[]> it = two.iterator();
		for ( FileChunk existingChunk : one ) {
			byte[] testChunk = it.next();
			if ( !Arrays.equals( testChunk, existingChunk.getSha1Sum() ) )
				return false;
		}
		return true;
	}

	public static boolean hashListsEqualBbBb( List<ByteBuffer> list1, List<ByteBuffer> list2 )
	{
		return hashListsEqualBbArray( list1, ThriftUtil.unwrapByteBufferList( list2 ) );
	}

	public static boolean hashListsEqualBbArray( List<ByteBuffer> bufferList, List<byte[]> arrayList )
	{
		return hashListsEqualArray( ThriftUtil.unwrapByteBufferList( bufferList ), arrayList );
	}

	public static boolean hashListsEqualArray( List<byte[]> list1, List<byte[]> list2 )
	{
		if ( list1.size() != list2.size() )
			return false;
		Iterator<byte[]> it1 = list1.iterator();
		Iterator<byte[]> it2 = list2.iterator();
		while ( it1.hasNext() && it2.hasNext() ) {
			if ( !Arrays.equals( it1.next(), it2.next() ) )
				return false;
		}
		return true;
	}

}
