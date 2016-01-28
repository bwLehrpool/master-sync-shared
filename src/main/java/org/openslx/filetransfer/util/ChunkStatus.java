package org.openslx.filetransfer.util;

public enum ChunkStatus
{
	// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying, 5 = hashing
	COMPLETE( 0 ),
	MISSING( 1 ),
	UPLOADING( 2 ),
	QUEUED_FOR_COPY( 3 ),
	COPYING( 4 ),
	HASHING( 5 );

	public final byte val;

	private ChunkStatus( int val )
	{
		this.val = (byte)val;
	}
}
