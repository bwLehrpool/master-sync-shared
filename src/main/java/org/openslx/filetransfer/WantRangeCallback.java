package org.openslx.filetransfer;

/**
 * Callback interface - called when the downloader needs to send a
 * range request to the remote peer.
 */
public interface WantRangeCallback
{

	public FileRange get();
	
}
