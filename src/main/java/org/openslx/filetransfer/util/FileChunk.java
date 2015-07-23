package org.openslx.filetransfer.util;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import org.openslx.filetransfer.FileRange;

public class FileChunk
{

	public static final int CHUNK_SIZE_MIB = 16;
	public static final int CHUNK_SIZE = CHUNK_SIZE_MIB * ( 1024 * 1024 );

	public final FileRange range;
	public final byte[] sha1sum;
	private int failCount = 0;

	public FileChunk( long startOffset, long endOffset, byte[] sha1sum )
	{
		this.range = new FileRange( startOffset, endOffset );
		this.sha1sum = sha1sum;
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

	//

	public static int fileSizeToChunkCount( long fileSize )
	{
		return (int) ( ( fileSize + CHUNK_SIZE - 1 ) / CHUNK_SIZE );
	}

	public static void createChunkList( Collection<FileChunk> list, long fileSize, List<ByteBuffer> sha1Sums )
	{
		if ( fileSize < 0 )
			throw new IllegalArgumentException( "fileSize cannot be negative" );
		if ( !list.isEmpty() )
			throw new IllegalArgumentException( "Passed list is not empty" );
		long chunkCount = fileSizeToChunkCount( fileSize );
		if ( sha1Sums != null ) {
			if ( sha1Sums.size() != chunkCount )
				throw new IllegalArgumentException(
						"Passed a sha1sum list, but hash count in list doesn't match expected chunk count" );
			long offset = 0;
			for ( ByteBuffer sha1sum : sha1Sums ) { // Do this as we don't know how efficient List.get(index) is...
				long end = offset + CHUNK_SIZE;
				if ( end > fileSize )
					end = fileSize;
				list.add( new FileChunk( offset, end, sha1sum.array() ) );
				offset = end;
			}
			return;
		}
		long offset = 0;
		while ( offset < fileSize ) { // ...otherwise we could share this code
			long end = offset + CHUNK_SIZE;
			if ( end > fileSize )
				end = fileSize;
			list.add( new FileChunk( offset, end, null ) );
			offset = end;
		}
	}

	public boolean hasSha1Sum()
	{
		return sha1sum != null && sha1sum.length == 20;
	}
}
