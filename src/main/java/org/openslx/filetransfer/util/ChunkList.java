package org.openslx.filetransfer.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.filetransfer.LocalChunkSource.ChunkSource;
import org.openslx.util.ThriftUtil;

public class ChunkList
{

	private static final Logger LOGGER = LogManager.getLogger( ChunkList.class );

	/**
	 * Here we keep a list of all chunks in the proper order, in case we quickly need to access one
	 */
	private final List<FileChunk> allChunks;

	/**
	 * Chunks that are missing from the file
	 */
	private final LinkedList<FileChunk> missingChunks = new LinkedList<>();

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
	 * @return lowest index of chunk that didn't have a sha1sum before, -1 if no new ones  
	 */
	public synchronized int updateSha1Sums( List<byte[]> sha1Sums )
	{
		int index = 0;
		int firstNew = -1;
		for ( byte[] sum : sha1Sums ) {
			if ( index >= allChunks.size() )
				break;
			if ( sum != null ) {
				FileChunk chunk = allChunks.get( index );
				if ( chunk.setSha1Sum( sum ) ) {
					if ( firstNew == -1 ) {
						firstNew = index;
					}
					if ( chunk.status == ChunkStatus.MISSING && Arrays.equals( FileChunk.NULL_BLOCK_SHA1, sum ) ) {
						markMissingAsComplete( index );
					}
				}
				if ( !hasChecksum ) {
					hasChecksum = true;
				}
			}
			index++;
		}
		return firstNew;
	}

	/**
	 * Get CRC32 list in DNBD3 format. All checksums are little
	 * endian and prefixed by the crc32 sum of the list itself.
	 */
	public synchronized byte[] getDnbd3Crc32List() throws IOException
	{
		byte buffer[] = new byte[ allChunks.size() * 4 + 4 ]; // 4 byte per chunk plus master
		long nextChunkOffset = 0;
		int nextCrcArrayPos = 4;
		for ( FileChunk c : allChunks ) {
			if ( c.crc32 == null ) {
				throw new IllegalStateException( "Called on ChunkList that doesn't have crc32 enabled" );
			}
			if ( c.range.startOffset != nextChunkOffset ) {
				throw new IllegalStateException( "Chunk list is not in order or has wrong chunk size" );
			}
			nextChunkOffset += FileChunk.CHUNK_SIZE;
			c.getCrc32Le( buffer, nextCrcArrayPos );
			nextCrcArrayPos += 4;
		}
		CRC32 masterCrc = new CRC32();
		masterCrc.update( buffer, 4, buffer.length - 4 );
		int value = (int)masterCrc.getValue();
		buffer[3] = (byte) ( value >>> 24 );
		buffer[2] = (byte) ( value >>> 16 );
		buffer[1] = (byte) ( value >>> 8 );
		buffer[0] = (byte)value;
		return buffer;
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
		FileChunk c = missingChunks.removeFirst();
		c.setStatus( ChunkStatus.UPLOADING );
		pendingChunks.add( c );
		return c;
	}

	/**
	 * Returns true if this list contains a chunk with state MISSING,
	 * which means the chunk doesn't have a sha1 known to exist in
	 * another image.
	 * @return
	 */
	public synchronized boolean hasLocallyMissingChunk()
	{
		return !missingChunks.isEmpty() && missingChunks.peekFirst().status == ChunkStatus.MISSING;
	}

	/**
	 * Get a chunk that is marked as candidate for copying.
	 * Returns null if none are available.
	 */
	public synchronized FileChunk getCopyCandidate()
	{
		if ( missingChunks.isEmpty() )
			return null;
		FileChunk last = missingChunks.removeLast();
		if ( last.status != ChunkStatus.QUEUED_FOR_COPY ) {
			// Put back
			missingChunks.add( last );
			return null;
		}
		// Is a candidate
		last.setStatus( ChunkStatus.COPYING );
		pendingChunks.add( last );
		return last;
	}

	/**
	 * Mark the given chunks for potential local copying instead of receiving them
	 * from peer.
	 * @param firstNew 
	 * @param sources
	 */
	public synchronized void markLocalCopyCandidates( List<ChunkSource> sources )
	{
		for ( ChunkSource src : sources ) {
			try {
				if ( src.sourceCandidates.isEmpty() )
					continue;
				List<FileChunk> append = null;
				for ( Iterator<FileChunk> it = missingChunks.iterator(); it.hasNext(); ) {
					FileChunk chunk = it.next();
					if ( !Arrays.equals( chunk.sha1sum, src.sha1sum ) )
						continue;
					if ( chunk.status == ChunkStatus.QUEUED_FOR_COPY )
						continue;
					// Bingo
					if ( append == null ) {
						append = new ArrayList<>( 20 );
					}
					it.remove();
					chunk.setStatus( ChunkStatus.QUEUED_FOR_COPY );
					chunk.setSource( src );
					append.add( chunk );
				}
				if ( append != null ) {
					// Move all the chunks queued for copying to the end of the list, so when
					// we getMissing() a chunk for upload from client, these ones would only
					// come last, in case reading from storage and writing back is really slow
					missingChunks.addAll( append );
				}
			} catch ( Exception e ) {
				LOGGER.warn( "chunk clone list if messed up", e );
			}
		}
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
		missingChunks.addFirst( c );
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

	/**
	 * Returns true if the last chunk is exactly 16MiB and all zeros
	 * @return
	 */
	public boolean lastChunkIsZero()
	{
		if ( allChunks.isEmpty() )
			return false;
		FileChunk chunk = allChunks.get( allChunks.size() - 1 );
		return chunk.sha1sum != null && Arrays.equals( FileChunk.NULL_BLOCK_SHA1, chunk.sha1sum );
	}

}
