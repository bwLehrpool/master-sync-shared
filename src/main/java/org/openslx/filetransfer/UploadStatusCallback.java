package org.openslx.filetransfer;

public interface UploadStatusCallback
{

	public void uploadError( String message );

	public void uploadProgress( long bytesSent );

}
