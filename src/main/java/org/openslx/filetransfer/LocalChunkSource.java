package org.openslx.filetransfer;

import java.util.ArrayList;
import java.util.List;

public interface LocalChunkSource
{

	public static class ChunkSource
	{
		public final List<SourceFile> sourceCandidates;
		public final byte[] sha1sum;

		public ChunkSource( byte[] sha1sum )
		{
			this.sha1sum = sha1sum;
			this.sourceCandidates = new ArrayList<>();
		}

		public void addFile( String file, long offset, int size )
		{
			this.sourceCandidates.add( new SourceFile( file, offset, size ) );
		}
	}

	public List<ChunkSource> getCloneSources( List<byte[]> sums );

	public static class SourceFile
	{
		public final String fileName;
		public final long offset;
		public final int chunkSize;

		public SourceFile( String file, long offset, int size )
		{
			this.fileName = file;
			this.offset = offset;
			this.chunkSize = size;
		}
	}

}
