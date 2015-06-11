package org.openslx.thrifthelper;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * Binary protocol implementation for thrift.
 * Will not read messages bigger than 12MiB.
 * 
 */
public class TBinaryProtocolSafe extends TBinaryProtocol
{
	/**
	 * Factory
	 */
	@SuppressWarnings( "serial" )
	public static class Factory implements TProtocolFactory
	{

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
		super( trans );
		strictRead_ = strictRead;
		strictWrite_ = strictWrite;
	}

	/**
	 * Reading methods.
	 */

	public TMessage readMessageBegin() throws TException
	{
		int size = readI32();
		if ( size > maxLen )
			throw new TProtocolException( TProtocolException.SIZE_LIMIT, "Payload too big." );
		if ( size < 0 ) {
			int version = size & VERSION_MASK;
			if ( version != VERSION_1 ) {
				throw new TProtocolException( TProtocolException.BAD_VERSION, "Bad version in readMessageBegin" );
			}
			return new TMessage( readString(), (byte) ( size & 0x000000ff ), readI32() );
		} else {
			if ( strictRead_ ) {
				throw new TProtocolException( TProtocolException.BAD_VERSION, "Missing version in readMessageBegin, old client?" );
			}
			return new TMessage( readStringBody( size ), readByte(), readI32() );
		}
	}

	public String readString() throws TException
	{
		int size = readI32();
		if ( size > maxLen )
			throw new TProtocolException( TProtocolException.SIZE_LIMIT, "Payload too big." );
		if ( trans_.getBytesRemainingInBuffer() >= size ) {
			try {
				String s = new String( trans_.getBuffer(), trans_.getBufferPosition(), size, "UTF-8" );
				trans_.consumeBuffer( size );
				return s;
			} catch ( UnsupportedEncodingException e ) {
				throw new TException( "JVM DOES NOT SUPPORT UTF-8" );
			}
		}

		return readStringBody( size );
	}

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

