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

	/**
	 * Check if the given range lies within this chunk's range.
	 * 
	 * @param startOffset Start offset to compare to
	 * @param endOffset End offset to compare to
	 * @return true iff the given offsets are a subset or equal to the offsets represented by this
	 *         class
	 */
	public boolean contains( long startOffset, long endOffset )
	{
		return this.startOffset <= startOffset && this.endOffset >= endOffset;
	}

	@Override
	public boolean equals( Object other )
	{
		if ( other == this )
			return true;
		if ( other == null || ! ( other instanceof FileRange ) )
			return false;
		FileRange o = (FileRange)other;
		return o.startOffset == this.startOffset && o.endOffset == this.endOffset;
	}

	@Override
	public int hashCode()
	{
		return (int)startOffset ^ Integer.rotateLeft( (int)endOffset, 16 ) ^ (int)(startOffset >> 32);
	}

	@Override
	public String toString()
	{
		return startOffset + "-" + endOffset;
	}

}
