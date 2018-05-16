package org.openslx.filetransfer.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.openslx.filetransfer.FileRange;
import org.openslx.filetransfer.LocalChunkSource.ChunkSource;

public class FileChunk
{
	
	private static final Logger LOGGER = Logger.getLogger( FileChunk.class );
	
	/**
	 * Length in bytes of binary sha1 representation
	 */
	public static final int SHA1_LENGTH = 20;
	public static final int CHUNK_SIZE_MIB = 16;
	public static final int CHUNK_SIZE = CHUNK_SIZE_MIB * ( 1024 * 1024 );

	public final FileRange range;
	private int failCount = 0;
	protected byte[] sha1sum;
	protected CRC32 crc32;
	protected ChunkStatus status = ChunkStatus.MISSING;
	private boolean writtenToDisk = false;
	private ChunkSource localSource = null;

	static final byte[] NULL_BLOCK_SHA1 = new byte[] {
			0x3b, 0x44, 0x17, (byte)0xfc, 0x42, 0x1c, (byte)0xee, 0x30, (byte)0xa9, (byte)0xad, 0x0f,
			(byte)0xd9, 0x31, (byte)0x92, 0x20, (byte)0xa8, (byte)0xda, (byte)0xe3, 0x2d, (byte)0xa2
	};
	
	static final long NULL_BLOCK_CRC32 = 2759631178l;

	public FileChunk( long startOffset, long endOffset, byte[] sha1sum )
	{
		this.range = new FileRange( startOffset, endOffset );
		if ( sha1sum == null || sha1sum.length != SHA1_LENGTH ) {
			this.sha1sum = null;
		} else {
			this.sha1sum = sha1sum;
		}
	}

	synchronized boolean setSha1Sum( byte[] sha1sum )
	{
		if ( this.sha1sum != null || sha1sum == null || sha1sum.length != SHA1_LENGTH )
			return false;
		this.sha1sum = sha1sum;
		if ( Arrays.equals( sha1sum, NULL_BLOCK_SHA1 ) ) {
			// 
			writtenToDisk = true;
			if ( crc32 == null ) {
				crc32 = new CRC32() {
					@Override
					public long getValue()
					{
						return NULL_BLOCK_CRC32;
					}
				};
			}
			return true;
		}
		if ( this.status == ChunkStatus.COMPLETE ) {
			this.status = ChunkStatus.HASHING;
		}
		return true;
	}

	/**
	 * Signal that transferring this chunk seems to have failed (checksum
	 * mismatch).
	 * 
	 * @return Number of times the transfer failed now
	 */
	synchronized int incFailed()
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
		return "[Chunk " + getChunkIndex() + " (" + status + "), fails: " + failCount + "]";
	}

	public synchronized byte[] getSha1Sum()
	{
		return sha1sum;
	}

	public synchronized ChunkStatus getStatus()
	{
		return status;
	}

	public synchronized void calculateDnbd3Crc32( byte[] data )
	{
		// As this is usually called before we validated the sha1, handle the case where
		// this gets called multiple times and only remember the last result
		long old = Long.MAX_VALUE;
		if ( crc32 == null ) {
			crc32 = new CRC32();
		} else {
			LOGGER.info( "Redoing CRC32 of Chunk " + getChunkIndex() );
			old = crc32.getValue();
			crc32.reset();
		}
		int expectedLength = range.getLength();
		if ( expectedLength > data.length ) {
			LOGGER.error( "Chunk #" + getChunkIndex() + ": " + data.length + " instead of " + expectedLength + " for " + getChunkIndex() );
		}
		crc32.update( data, 0, expectedLength );
		if ( ( expectedLength % 4096 ) != 0 ) {
			// DNBD3 virtually pads all images to be a multiple of 4KiB in size,
			// so simulate that here too
			LOGGER.debug( "Block " + getChunkIndex() + " not multiple of 4k." );
			byte[] padding = new byte[ 4096 - ( expectedLength % 4096 ) ];
			crc32.update( padding );
		}
		if ( old != Long.MAX_VALUE && old != crc32.getValue() ) {
			LOGGER.warn( String.format( "Changed from %x to %x", old, crc32.getValue() ) );
		}
	}

	public synchronized void getCrc32Le( byte[] buffer, int offset )
	{
		if ( crc32 == null )
			throw new IllegalStateException( "Trying to get CRC32 on Chunk that doesn't have one" );
		int value = (int)crc32.getValue();
		buffer[offset + 3] = (byte) ( value >>> 24 );
		buffer[offset + 2] = (byte) ( value >>> 16 );
		buffer[offset + 1] = (byte) ( value >>> 8 );
		buffer[offset + 0] = (byte)value;
	}

	/**
	 * Whether the chunk of data this chunk refers to has been written to
	 * disk and is assumed to be valid/up to date.
	 */
	public synchronized boolean isWrittenToDisk()
	{
		return writtenToDisk;
	}

	synchronized void setStatus( ChunkStatus status )
	{
		if ( status != null ) {
			if ( status == ChunkStatus.COMPLETE ) {
				this.writtenToDisk = true;
			} else if ( status == ChunkStatus.MISSING || status == ChunkStatus.QUEUED_FOR_COPY ) {
				this.writtenToDisk = false;
			}
			this.status = status;
		}
	}

	//

	public static int fileSizeToChunkCount( long fileSize )
	{
		return (int) ( ( fileSize + CHUNK_SIZE - 1 ) / CHUNK_SIZE );
	}

	public static void createChunkList( List<FileChunk> list, long fileSize, List<byte[]> sha1Sums )
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

	public int getFailCount()
	{
		return failCount;
	}

	public void setSource( ChunkSource src )
	{
		this.localSource = src;
	}
	
	public ChunkSource getSources()
	{
		return this.localSource;
	}

}
