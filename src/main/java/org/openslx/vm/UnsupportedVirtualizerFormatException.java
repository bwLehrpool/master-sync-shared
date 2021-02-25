package org.openslx.vm;

@SuppressWarnings( "serial" )
public class UnsupportedVirtualizerFormatException extends Exception
{
	public UnsupportedVirtualizerFormatException(String message) {
		super(message);
	}
}