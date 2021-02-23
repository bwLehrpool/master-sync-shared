package org.openslx.vm;

public class UnsupportedVirtualizerFormatException extends Exception
{
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 5794121065945636839L;

	public UnsupportedVirtualizerFormatException(String message) {
		super(message);
	}
}