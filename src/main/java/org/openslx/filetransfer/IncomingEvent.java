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
	void incomingUploader( Uploader uploader ) throws IOException;

	void incomingDownloader( Downloader downloader ) throws IOException;
}
