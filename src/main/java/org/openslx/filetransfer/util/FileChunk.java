package org.openslx.filetransfer.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openslx.filetransfer.FileRange;

public class FileChunk
{

	public static final int SHA1_LENGTH = 20;
	public static final int CHUNK_SIZE_MIB = 16;
	public static final int CHUNK_SIZE = CHUNK_SIZE_MIB * ( 1024 * 1024 );

	public final FileRange range;
	private int failCount = 0;
	protected byte[] sha1sum;
	protected ChunkStatus status = ChunkStatus.MISSING;

	public FileChunk( long startOffset, long endOffset, byte[] sha1sum )
	{
		this.range = new FileRange( startOffset, endOffset );
		if ( sha1sum == null || sha1sum.length != SHA1_LENGTH ) {
			this.sha1sum = null;
		} else {
			this.sha1sum = sha1sum;
		}
	}

	public synchronized void setSha1Sum( byte[] sha1sum )
	{
		if ( this.sha1sum != null || sha1sum == null || sha1sum.length != SHA1_LENGTH )
			return;
		this.sha1sum = sha1sum;
		if ( this.status == ChunkStatus.COMPETE ) {
			this.status = ChunkStatus.HASHING;
		}
	}

	/**
	 * Signal that transferring this chunk seems to have failed (checksum
	 * mismatch).
	 * 
	 * @return Number of times the transfer failed now
	 */
	public synchronized int incFailed()
	{
		return ++failCount;
	}

	public int getChunkIndex()
	{
		return (int) ( range.startOffset / CHUNK_SIZE );
	}

	@Override
	public String toString()
	{
		return "[Chunk " + getChunkIndex() + " (" + range.startOffset + "-" + range.endOffset + "), fails: " + failCount + "]";
	}

	public synchronized byte[] getSha1Sum()
	{
		return sha1sum;
	}

	public synchronized ChunkStatus getStatus()
	{
		return status;
	}

	protected synchronized void setStatus( ChunkStatus status )
	{
		if ( status != null ) {
			this.status = status;
		}
	}

	//

	public static int fileSizeToChunkCount( long fileSize )
	{
		return (int) ( ( fileSize + CHUNK_SIZE - 1 ) / CHUNK_SIZE );
	}

	public static void createChunkList( Collection<FileChunk> list, long fileSize, List<byte[]> sha1Sums )
	{
		if ( fileSize < 0 )
			throw new IllegalArgumentException( "fileSize cannot be negative" );
		if ( !list.isEmpty() )
			throw new IllegalArgumentException( "Passed list is not empty" );
		long offset = 0;
		Iterator<byte[]> hashIt = null;
		if ( sha1Sums != null ) {
			hashIt = sha1Sums.iterator();
		}
		while ( offset < fileSize ) {
			long end = offset + CHUNK_SIZE;
			if ( end > fileSize )
				end = fileSize;
			byte[] hash = null;
			if ( hashIt != null && hashIt.hasNext() ) {
				hash = hashIt.next();
			}
			list.add( new FileChunk( offset, end, hash ) );
			offset = end;
		}
	}
}
