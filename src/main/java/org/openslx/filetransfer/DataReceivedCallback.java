package org.openslx.filetransfer;

public interface DataReceivedCallback
{

	public boolean dataReceived(long fileOffset, int dataLength, byte[] data);

}
