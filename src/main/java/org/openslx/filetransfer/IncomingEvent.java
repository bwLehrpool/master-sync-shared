package org.openslx.filetransfer;

import java.io.IOException;

/***************************************************************************/
/**
 * IncomingEvent interface for handling what should happen with incoming
 * uploader or downloader in Listener. Must be implemented outside.
 * 
 * @author bjoern
 */
public interface IncomingEvent
{
	void incomingDownloadRequest( Uploader uploader ) throws IOException;

	void incomingUploadRequest( Downloader downloader ) throws IOException;
}
