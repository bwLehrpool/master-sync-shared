package org.openslx.filetransfer;

public class FileRange
{

	/**
	 * Offset of first byte of range in file, inclusive
	 */
	public final long startOffset;
	/**
	 * Offset of last byte of range in file, exclusive
	 */
	public final long endOffset;

	/**
	 * Create a FileRange instance
	 * 
	 * @param startOffset Offset of first byte of range in file, inclusive
	 * @param endOffset Offset of last byte of range in file, exclusive
	 */
	public FileRange( long startOffset, long endOffset )
	{
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	/**
	 * Get length of range
	 * 
	 * @return length of range, in bytes
	 */
	public int getLength()
	{
		return (int) ( endOffset - startOffset );
	}

	@Override
	public boolean equals( Object other )
	{
		if ( other == null || ! ( other instanceof FileRange ) )
			return false;
		FileRange o = (FileRange)other;
		return o.startOffset == this.startOffset && o.endOffset == this.endOffset;
	}

}
