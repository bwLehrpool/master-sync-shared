package org.openslx.thrifthelper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TLayeredTransport;

/**
 * Binary protocol implementation for thrift.
 * Will not read messages bigger than 12MiB.
 * 
 */
public class TBinaryProtocolSafe extends TBinaryProtocol
{

	private final static Logger LOGGER = LogManager.getLogger( ThriftHandler.class );

	/**
	 * Factory
	 */
	public static class Factory implements TProtocolFactory
	{

		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 6896537370338823740L;

		protected boolean strictRead_ = false;
		protected boolean strictWrite_ = true;

		public Factory()
		{
			this( false, true );
		}

		public Factory(boolean strictRead, boolean strictWrite)
		{
			strictRead_ = strictRead;
			strictWrite_ = strictWrite;
		}

		public TProtocol getProtocol( TTransport trans )
		{
			return new TBinaryProtocolSafe( trans, strictRead_, strictWrite_ );
		}
	}

	private static final int maxLen = 12 * 1024 * 1024; // 12 MiB

	/**
	 * Constructor
	 */
	public TBinaryProtocolSafe(TTransport trans)
	{
		this( trans, false, true );
	}

	public TBinaryProtocolSafe(TTransport trans, boolean strictRead, boolean strictWrite)
	{
		super( trans, maxLen, maxLen, strictRead, strictWrite );
	}

	/*
	 * Reading methods.
	 */

	@Override
	public TMessage readMessageBegin() throws TException
	{
		int size;
		try {
			size = readI32();
		} catch ( TTransportException e ) {
			// Do this to suppress certain SSL handshake errors that result from port scanning and service probing
			if ( e.getCause() instanceof SSLException ) {
				String m = e.getCause().getMessage();
				// We still want SSL errors that help diagnosing more specific SSL errors that relate to actual
				// SSL handshake attempts, like incompatible TLS versions or ciphers.
				if ( !m.contains( "Remote host terminated the handshake" )
						&& !m.contains( "Unsupported or unrecognized SSL message" ) ) {
					LOGGER.warn( getIp() + m );
				}
				// Fake an END_OF_FILE exception, as the logException() method in the server class will
				// ignore there. Let's hope it will stay ignored in the future.
				throw new TTransportException( TTransportException.END_OF_FILE );
			} else if ( e.getCause() instanceof SocketException
					&& ( e.getCause().getMessage().contains( " timed out" )
							|| e.getCause().getMessage().contains( "Connection reset" )
							|| e.getCause().getMessage().contains( "Connection or inbound" ) ) ) {
				// Faaaake
				throw new TTransportException( TTransportException.END_OF_FILE );
			} else if ( e.getMessage().contains( "larger than max length" ) || e.getMessage().contains( "Read a negative frame size" ) ) {
				// Also fake, since this one prints a whole stack trace compared to the other
				// message by AbstractNonblockingServer
				LOGGER.debug( e.getMessage(), e );
				throw new TTransportException( TTransportException.END_OF_FILE );
			}
			throw e;
		}
		if ( size > maxLen )
			throw new TProtocolException( TProtocolException.SIZE_LIMIT, getIp() + "Payload too big." );
		if ( size < 0 ) {
			int version = size & VERSION_MASK;
			if ( version != VERSION_1 ) {
				LOGGER.warn( getIp() + "Bad version (" + version + ") in readMessageBegin" );
				throw new TTransportException( TTransportException.END_OF_FILE );
			}
			return new TMessage( readString(), (byte) ( size & 0x000000ff ), readI32() );
		} else {
			if ( strictRead_ ) {
				throw new TProtocolException( TProtocolException.BAD_VERSION, "Missing version in readMessageBegin, old client?" );
			}
			return new TMessage( readStringBody( size ), readByte(), readI32() );
		}
	}

	private String getIp()
	{
		TTransport t = trans_;
		while ( t instanceof TLayeredTransport ) {
			t = ( (TLayeredTransport)t ).getInnerTransport();
		}
		InetAddress ia = null;
		if ( t instanceof TSocket ) {
			SocketAddress sa = ( (TSocket)t ).getSocket().getRemoteSocketAddress();
			if ( sa != null && ( sa instanceof InetSocketAddress ) )
				ia = ( (InetSocketAddress)sa ).getAddress();
			if ( ia == null )
				ia = ( (TSocket)t ).getSocket().getInetAddress();
		} else {
			LOGGER.debug( "getIp(" + t.getClass().getSimpleName() + ")" );
		}
		if ( ia == null )
			return "";
		return ia.getHostAddress() + ": ";
	}

	@Override
	public String readString() throws TException
	{
		int size = readI32();
		if ( size > maxLen )
			throw new TProtocolException( TProtocolException.SIZE_LIMIT, "Payload too big." );
		if ( trans_.getBytesRemainingInBuffer() >= size ) {
			String s = new String( trans_.getBuffer(), trans_.getBufferPosition(), size, StandardCharsets.UTF_8 );
			trans_.consumeBuffer( size );
			return s;
		}

		return readStringBody( size );
	}

	@Override
	public ByteBuffer readBinary() throws TException
	{
		int size = readI32();
		if ( size > maxLen )
			throw new TProtocolException( TProtocolException.SIZE_LIMIT, "Payload too big." );
		if ( trans_.getBytesRemainingInBuffer() >= size ) {
			ByteBuffer bb = ByteBuffer.wrap( trans_.getBuffer(), trans_.getBufferPosition(), size );
			trans_.consumeBuffer( size );
			return bb;
		}

		byte[] buf = new byte[ size ];
		trans_.readAll( buf, 0, size );
		return ByteBuffer.wrap( buf );
	}

}
