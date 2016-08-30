package org.openslx.filetransfer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.TransferInformation;
import org.openslx.filetransfer.Uploader;

public abstract class OutgoingTransferBase extends AbstractTransfer
{

	/*
	 * Constants
	 */

	private static final Logger LOGGER = Logger.getLogger( OutgoingTransferBase.class );

	private static final long INACTIVITY_TIMEOUT = TimeUnit.MINUTES.toMillis( 5 );

	/*
	 * Overridable constants
	 */

	protected static int MAX_CONNECTIONS_PER_TRANSFER = 2;

	/*
	 * Class members
	 */

	/**
	 * Remote peer is downloading, so we have Uploaders
	 */
	private final List<Uploader> uploads = new ArrayList<>();

	/**
	 * File being uploaded
	 */
	private final File sourceFile;

	private final TransferInformation transferInformation;

	public OutgoingTransferBase( String transferId, File sourceFile, int plainPort, int sslPort )
	{
		super( transferId );
		this.sourceFile = sourceFile;
		this.transferInformation = new TransferInformation( transferId, plainPort, sslPort );
	}

	/**
	 * Add another connection for this file transfer.
	 * 
	 * @param connection
	 * @return true if the connection is accepted, false if it should be
	 *         discarded
	 */
	public synchronized boolean addConnection( final Uploader connection, ExecutorService pool )
	{
		synchronized ( uploads ) {
			if ( uploads.size() >= MAX_CONNECTIONS_PER_TRANSFER )
				return false;
			uploads.add( connection );
		}
		return runConnectionInternal( connection, pool );
	}

	protected boolean runConnectionInternal( final Uploader connection, ExecutorService pool )
	{
		try {
			pool.execute( new Runnable() {
				@Override
				public void run()
				{
					boolean ret = connection.upload( sourceFile.getAbsolutePath() );
					synchronized ( uploads ) {
						uploads.remove( connection );
					}
					if ( ret ) {
						connectFails.set( 0 );
					}
					if ( ret && uploads.isEmpty() && potentialFinishTime.get() == 0 ) {
						potentialFinishTime.set( System.currentTimeMillis() );
					}
					if ( !ret && uploads.isEmpty() ) {
						connectFails.incrementAndGet();
					}
					lastActivityTime.set( System.currentTimeMillis() );
				}
			} );
		} catch ( Exception e ) {
			LOGGER.warn( "threadpool rejected the incoming file transfer", e );
			synchronized ( uploads ) {
				uploads.remove( connection );
			}
			return false;
		}
		return true;
	}

	@Override
	public TransferInformation getTransferInfo()
	{
		return transferInformation;
	}

	@Override
	public final boolean isActive()
	{
		return uploads.size() > 0 || lastActivityTime.get() + INACTIVITY_TIMEOUT > System.currentTimeMillis();
	}

	@Override
	public void cancel()
	{
		// Void
	}

	@Override
	public final int getActiveConnectionCount()
	{
		return uploads.size();
	}

}
