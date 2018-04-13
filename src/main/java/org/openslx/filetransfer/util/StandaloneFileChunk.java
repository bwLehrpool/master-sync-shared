package org.openslx.filetransfer.util;

public class StandaloneFileChunk extends FileChunk
{

	public StandaloneFileChunk( long startOffset, long endOffset, byte[] sha1sum )
	{
		super( startOffset, endOffset, sha1sum );
	}

	public void overrideStatus(ChunkStatus status)
	{
		this.status = status;
	}
	
}
