package fi.iki.elonen;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openslx.util.Util;

/**
 * A simple, tiny, nicely embeddable HTTP server in Java
 * <p/>
 * <p/>
 * NanoHTTPD
 * <p>
 * Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen,
 * 2010 by Konstantinos Togias
 * </p>
 * <p/>
 * <p/>
 * <b>Features + limitations: </b>
 * <ul>
 * <p/>
 * <li>Only one Java file</li>
 * <li>Java 5 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if
 * you need them.)</li>
 * <li>Supports parameter parsing of GET and POST methods (+ rudimentary PUT
 * support in 1.25)</li>
 * <li>Supports both dynamic content and file serving</li>
 * <li>Supports file upload (since version 1.2, 2010)</li>
 * <li>Supports partial content (streaming)</li>
 * <li>Supports ETags</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * <li>Default code serves files and shows all HTTP parameters and headers</li>
 * <li>File server supports directory listing, index.html and index.htm</li>
 * <li>File server supports partial content (streaming)</li>
 * <li>File server supports ETags</li>
 * <li>File server does the 301 redirection trick for directories without '/'</li>
 * <li>File server supports simple skipping for files (continue download)</li>
 * <li>File server serves also very long files without memory overhead</li>
 * <li>Contains a built-in list of most common MIME types</li>
 * <li>All header names are converted to lower case so they don't vary between
 * browsers/clients</li>
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * <b>How to use: </b>
 * <ul>
 * <p/>
 * <li>Subclass and implement serve() and embed to your own program</li>
 * <p/>
 * </ul>
 * <p/>
 * See the separate "LICENSE.md" file for the distribution license (Modified BSD
 * licence)
 */
public abstract class NanoHTTPD implements Runnable
{

	/**
	 * Maximum time to wait on Socket.getInputStream().read() (in milliseconds)
	 * This is required as the Keep-Alive HTTP connections would otherwise
	 * block the socket reading thread forever (or as long the browser is open).
	 */
	public static final int SOCKET_READ_TIMEOUT = 10000;
	/**
	 * Common MIME type for dynamic content: plain text
	 */
	public static final String MIME_PLAINTEXT = "text/plain";
	/**
	 * Common MIME type for dynamic content: html
	 */
	public static final String MIME_HTML = "text/html";
	/**
	 * Pseudo-Parameter to use to store the actual query string in the
	 * parameters map for later
	 * re-processing.
	 */
	private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
	private final ServerSocket myServerSocket;
	private Set<Socket> openConnections = new HashSet<Socket>();
	/**
	 * Pluggable strategy for asynchronously executing requests.
	 */
	private final ExecutorService asyncRunner;

	protected int maxRequestSize = 0;

	/**
	 * Constructs an HTTP server on given hostname and port.
	 */
	public NanoHTTPD( String hostname, int port, ExecutorService executor ) throws IOException
	{
		this.asyncRunner = executor;
		myServerSocket = new ServerSocket();
		myServerSocket.setReuseAddress( true );
		myServerSocket.bind( ( hostname != null ) ? new InetSocketAddress( hostname, port )
				: new InetSocketAddress( port ) );
	}

	/**
	 * Start the server.
	 */
	@Override
	public void run()
	{

		do {
			try {
				final Socket sck = myServerSocket.accept();
				registerConnection( sck );
				sck.setSoTimeout( SOCKET_READ_TIMEOUT );
				final InputStream inputStream = sck.getInputStream();
				try {
					asyncRunner.execute( new Runnable() {
						@Override
						public void run()
						{
							OutputStream outputStream = null;
							try {
								outputStream = sck.getOutputStream();
								HTTPSession session = new HTTPSession( inputStream, outputStream,
										sck.getInetAddress() );
								while ( !sck.isClosed() && !sck.isInputShutdown() && !sck.isOutputShutdown() ) {
									session.execute();
								}
							} catch ( Exception e ) {
								// When the socket is closed by the client, we throw our own SocketException
								// to break the  "keep alive" loop above.
								if ( ! ( e instanceof SocketTimeoutException )
										&& ! ( e instanceof SocketException && "NanoHttpd Shutdown".equals( e.getMessage() ) ) ) {
									e.printStackTrace();
								}
							} finally {
								Util.safeClose( outputStream, inputStream, sck );
								unRegisterConnection( sck );
							}
						}
					} );
				} catch ( RejectedExecutionException e ) {
					Util.safeClose( sck, inputStream );
					unRegisterConnection( sck );
				}
			} catch ( IOException e ) {
			}
		} while ( !myServerSocket.isClosed() );
		serverStopped();
	}

	public void serverStopped()
	{

	}

	/**
	 * Stop the server.
	 */
	public void stop()
	{
		try {
			Util.safeClose( myServerSocket );
			closeAllConnections();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Registers that a new connection has been set up.
	 * 
	 * @param socket the {@link Socket} for the connection.
	 */
	public synchronized void registerConnection( Socket socket )
	{
		openConnections.add( socket );
	}

	/**
	 * Registers that a connection has been closed
	 * 
	 * @param socket
	 *           the {@link Socket} for the connection.
	 */
	public synchronized void unRegisterConnection( Socket socket )
	{
		openConnections.remove( socket );
	}

	/**
	 * Forcibly closes all connections that are open.
	 */
	public synchronized void closeAllConnections()
	{
		for ( Socket socket : openConnections ) {
			Util.safeClose( socket );
		}
	}

	public final int getListeningPort()
	{
		return myServerSocket == null ? -1 : myServerSocket.getLocalPort();
	}

	public final boolean wasStarted()
	{
		return myServerSocket != null;
	}

	public final boolean isAlive()
	{
		return wasStarted() && !myServerSocket.isClosed();
	}

	/**
	 * Override this to customize the server.
	 * <p/>
	 * <p/>
	 * (By default, this returns a 404 "Not Found" plain text error response.)
	 * 
	 * @param uri Percent-decoded URI without parameters, for example
	 *           "/index.cgi"
	 * @param method "GET", "POST" etc.
	 * @param parms Parsed, percent decoded parameters from URI and, in case of
	 *           POST, data.
	 * @param headers Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	@Deprecated
	public Response serve( String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files )
	{
		return new Response( Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found" );
	}

	/**
	 * Override this to customize the server.
	 * <p/>
	 * <p/>
	 * (By default, this returns a 404 "Not Found" plain text error response.)
	 * 
	 * @param session The HTTP session
	 * @return HTTP response, see class Response for details
	 */
	public Response serve( IHTTPSession session )
	{
		Method method = session.getMethod();
		if ( Method.PUT.equals( method ) || Method.POST.equals( method ) ) {
			try {
				session.parseBody();
			} catch ( IOException ioe ) {
				return new Response( Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage() );
			} catch ( ResponseException re ) {
				return new Response( re.getStatus(), MIME_PLAINTEXT, re.getMessage() );
			}
		}

		Map<String, String> parms = session.getParms();
		parms.put( QUERY_STRING_PARAMETER, session.getQueryParameterString() );
		return serve( session.getUri(), method, session.getHeaders(), parms, null );
	}

	/**
	 * Decode percent encoded <code>String</code> values.
	 * 
	 * @param str the percent encoded <code>String</code>
	 * @return expanded form of the input, for example "foo%20bar" becomes
	 *         "foo bar"
	 */
	protected String decodePercent( String str )
	{
		String decoded = null;
		try {
			decoded = URLDecoder.decode( str, "UTF8" );
		} catch ( UnsupportedEncodingException ignored ) {
		}
		return decoded;
	}

	/**
	 * Decode parameters from a URL, handing the case where a single parameter
	 * name might have been
	 * supplied several times, by return lists of values. In general these lists
	 * will contain a
	 * single
	 * element.
	 * 
	 * @param parms original <b>NanoHTTPD</b> parameters values, as passed to
	 *           the <code>serve()</code> method.
	 * @return a map of <code>String</code> (parameter name) to
	 *         <code>List&lt;String&gt;</code> (a
	 *         list of the values supplied).
	 */
	protected Map<String, List<String>> decodeParameters( Map<String, String> parms )
	{
		return this.decodeParameters( parms.get( QUERY_STRING_PARAMETER ) );
	}

	/**
	 * Decode parameters from a URL, handing the case where a single parameter
	 * name might have been
	 * supplied several times, by return lists of values. In general these lists
	 * will contain a
	 * single
	 * element.
	 * 
	 * @param queryString a query string pulled from the URL.
	 * @return a map of <code>String</code> (parameter name) to
	 *         <code>List&lt;String&gt;</code> (a
	 *         list of the values supplied).
	 */
	protected Map<String, List<String>> decodeParameters( String queryString )
	{
		Map<String, List<String>> parms = new HashMap<String, List<String>>();
		if ( queryString != null ) {
			StringTokenizer st = new StringTokenizer( queryString, "&" );
			while ( st.hasMoreTokens() ) {
				String e = st.nextToken();
				int sep = e.indexOf( '=' );
				String propertyName = ( sep >= 0 ) ? decodePercent( e.substring( 0, sep ) ).trim()
						: decodePercent(
								e ).trim();
				if ( !parms.containsKey( propertyName ) ) {
					parms.put( propertyName, new ArrayList<String>() );
				}
				String propertyValue = ( sep >= 0 ) ? decodePercent( e.substring( sep + 1 ) ) : null;
				if ( propertyValue != null ) {
					parms.get( propertyName ).add( propertyValue );
				}
			}
		}
		return parms;
	}

	// ------------------------------------------------------------------------------- //
	//
	// Threading Strategy.
	//
	// ------------------------------------------------------------------------------- //

	/**
	 * HTTP Request methods, with the ability to decode a <code>String</code>
	 * back to its enum value.
	 */
	public enum Method
	{
		GET, PUT, POST, DELETE, HEAD, OPTIONS;

		static Method lookup( String method )
		{
			for ( Method m : Method.values() ) {
				if ( m.toString().equalsIgnoreCase( method ) ) {
					return m;
				}
			}
			return null;
		}
	}

	// ------------------------------------------------------------------------------- //

	/**
	 * HTTP response. Return one of these from serve().
	 */
	public static class Response
	{
		/**
		 * HTTP status code after processing, e.g. "200 OK", Status.OK
		 */
		private IStatus status;
		/**
		 * MIME type of content, e.g. "text/html"
		 */
		private String mimeType;
		/**
		 * Data of the response, may be null.
		 */
		private InputStream data;
		/**
		 * Headers for the HTTP response. Use addHeader() to add lines.
		 */
		private final Map<String, String> header = new HashMap<String, String>();
		/**
		 * The request method that spawned this response.
		 */
		private Method requestMethod;
		/**
		 * Use chunkedTransfer
		 */
		private boolean chunkedTransfer;

		/**
		 * Default constructor: response = Status.OK, mime = MIME_HTML and your
		 * supplied message
		 */
		public Response( String msg )
		{
			this( Status.OK, MIME_HTML, msg );
		}

		/**
		 * Basic constructor.
		 */
		public Response( IStatus status, String mimeType, InputStream data, boolean chunked )
		{
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
			this.chunkedTransfer = chunked;
		}

		/**
		 * Basic constructor. Enable chunked transfer for everything
		 * except ByteArrayInputStream.
		 */
		public Response( IStatus status, String mimeType, InputStream data )
		{
			this( status, mimeType, data, ! ( data instanceof ByteArrayInputStream ) );
		}

		/**
		 * Convenience method that makes an InputStream out of given byte array.
		 */
		public Response( IStatus status, String mimeType, byte[] data )
		{
			this( status, mimeType, data == null ? null : new ByteArrayInputStream( data ) );
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response( IStatus status, String mimeType, String txt )
		{
			this( status, mimeType, txt == null ? null : txt.getBytes( StandardCharsets.UTF_8 ) );
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader( String name, String value )
		{
			header.put( name, value );
		}

		public String getHeader( String name )
		{
			return header.get( name );
		}

		private static final ThreadLocal<SimpleDateFormat> headerDateFormatter = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue()
			{
				return new SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'" );
			}
		};

		/**
		 * Sends given response to the socket.
		 */
		protected void send( OutputStream outputStream, boolean close ) throws IOException
		{
			String mime = mimeType;

			final StringBuilder sb = new StringBuilder();
			if ( status == null ) {
				throw new Error( "sendResponse(): Status can't be null." );
			}
			sb.append( "HTTP/1.1 " );
			sb.append( status.getDescription() );
			sb.append( "\r\n" );

			if ( mime != null ) {
				sb.append( "Content-Type: " );
				sb.append( mime );
				sb.append( "\r\n" );
			}

			if ( header.get( "Date" ) == null ) {
				sb.append( "Date: " );
				sb.append( headerDateFormatter.get().format( System.currentTimeMillis() ) );
				sb.append( "\r\n" );
			}

			for ( Entry<String, String> item : header.entrySet() ) {
				sb.append( item.getKey() );
				sb.append( ": " );
				sb.append( item.getValue() );
				sb.append( "\r\n" );
			}

			sendConnectionHeaderIfNotAlreadyPresent( sb, header, close );

			if ( requestMethod != Method.HEAD && chunkedTransfer ) {
				sendAsChunked( outputStream, sb );
			} else {
				int pending = data != null ? data.available() : 0;
				pending = sendContentLengthHeaderIfNotAlreadyPresent( sb, header, pending );
				sb.append( "\r\n" );
				outputStream.write( sb.toString().getBytes( StandardCharsets.UTF_8 ) );
				sb.setLength( 0 );
				sendAsFixedLength( outputStream, pending );
			}

			if ( sb.length() != 0 ) {
				outputStream.write( sb.toString().getBytes( StandardCharsets.UTF_8 ) );
			}
			Util.safeClose( data );
		}

		protected int sendContentLengthHeaderIfNotAlreadyPresent( StringBuilder sb,
				Map<String, String> header, int size )
		{
			for ( String headerName : header.keySet() ) {
				if ( headerName.equalsIgnoreCase( "content-length" ) ) {
					try {
						return Integer.parseInt( header.get( headerName ) );
					} catch ( NumberFormatException ex ) {
						return size;
					}
				}
			}

			sb.append( "Content-Length: " );
			sb.append( size );
			sb.append( "\r\n" );
			return size;
		}

		protected void sendConnectionHeaderIfNotAlreadyPresent( StringBuilder sb, Map<String, String> header, boolean close )
		{
			if ( close ) {
				if ( !headerAlreadySent( header, "connection" ) ) {
					sb.append( "Connection: close\r\n" );
				}
				return;
			}
			// Client wants keep-alive
			if ( !headerAlreadySent( header, "connection" ) ) {
				sb.append( "Connection: keep-alive\r\n" );
			}
			if ( !headerAlreadySent( header, "keep-alive" ) ) {
				sb.append( "Keep-Alive: timeout=" );
				sb.append( SOCKET_READ_TIMEOUT / 1000 - 1 );
				sb.append( "\r\n" );
			}
		}

		private boolean headerAlreadySent( Map<String, String> header, String name )
		{
			for ( String headerName : header.keySet() ) {
				if ( headerName.equalsIgnoreCase( name ) )
					return true;
			}
			return false;
		}

		private static final byte[] CRLF = "\r\n".getBytes();
		private static final byte[] CHUNKED_END = "0\r\n\r\n".getBytes();
		private static final int BUFFER_SIZE = 256 * 1024;

		private void sendAsChunked( OutputStream outputStream, StringBuilder sb ) throws IOException
		{
			sb.append( "Transfer-Encoding: chunked\r\n" );
			sb.append( "\r\n" );
			outputStream.write( sb.toString().getBytes( StandardCharsets.UTF_8 ) );
			sb.setLength( 0 );
			byte[] buff = new byte[ BUFFER_SIZE ];
			int read;
			while ( ( read = data.read( buff ) ) > 0 ) {
				outputStream.write( String.format( "%x\r\n", read ).getBytes() );
				outputStream.write( buff, 0, read );
				outputStream.write( CRLF );
			}
			outputStream.write( CHUNKED_END );
		}

		private void sendAsFixedLength( OutputStream outputStream, int pending ) throws IOException
		{
			if ( requestMethod != Method.HEAD && data != null ) {
				int BUFFER_SIZE = 16 * 1024;
				byte[] buff = new byte[ BUFFER_SIZE ];
				while ( pending > 0 ) {
					int read = data.read( buff, 0, ( ( pending > BUFFER_SIZE ) ? BUFFER_SIZE : pending ) );
					if ( read <= 0 ) {
						break;
					}
					outputStream.write( buff, 0, read );
					pending -= read;
				}
			}
		}

		public IStatus getStatus()
		{
			return status;
		}

		public void setStatus( IStatus status )
		{
			this.status = status;
		}

		public String getMimeType()
		{
			return mimeType;
		}

		public void setMimeType( String mimeType )
		{
			this.mimeType = mimeType;
		}

		public InputStream getData()
		{
			return data;
		}

		public void setData( InputStream data )
		{
			this.data = data;
		}

		public Method getRequestMethod()
		{
			return requestMethod;
		}

		public void setRequestMethod( Method requestMethod )
		{
			this.requestMethod = requestMethod;
		}

		public void setChunkedTransfer( boolean chunkedTransfer )
		{
			this.chunkedTransfer = chunkedTransfer;
		}

		public interface IStatus
		{
			int getRequestStatus();

			String getDescription();
		}

		/**
		 * Some HTTP response status codes
		 */
		public enum Status implements IStatus
		{
			SWITCH_PROTOCOL( 101, "Switching Protocols" ),
			OK( 200, "OK" ),
			CREATED( 201, "Created" ),
			ACCEPTED( 202, "Accepted" ),
			NO_CONTENT( 204, "No Content" ),
			PARTIAL_CONTENT( 206, "Partial Content" ),
			REDIRECT( 301, "Moved Permanently" ),
			NOT_MODIFIED( 304, "Not Modified" ),
			BAD_REQUEST( 400, "Bad Request" ),
			UNAUTHORIZED( 401, "Unauthorized" ),
			FORBIDDEN( 403, "Forbidden" ),
			NOT_FOUND( 404, "Not Found" ),
			METHOD_NOT_ALLOWED( 405, "Method Not Allowed" ),
			RANGE_NOT_SATISFIABLE( 416, "Requested Range Not Satisfiable" ),
			INTERNAL_ERROR( 500, "Internal Server Error" ),
			SERVICE_UNAVAILABLE( 503, "Service Unavailable" );

			private final int requestStatus;
			private final String description;

			Status( int requestStatus, String description )
			{
				this.requestStatus = requestStatus;
				this.description = description;
			}

			@Override
			public int getRequestStatus()
			{
				return this.requestStatus;
			}

			@Override
			public String getDescription()
			{
				return "" + this.requestStatus + " " + description;
			}
		}
	}

	public static final class ResponseException extends Exception
	{
		private static final long serialVersionUID = 6569838532917408380L;
		private final Response.Status status;

		public ResponseException( Response.Status status, String message )
		{
			super( message );
			this.status = status;
		}

		public ResponseException( Response.Status status, String message, Exception e )
		{
			super( message, e );
			this.status = status;
		}

		public Response.Status getStatus()
		{
			return status;
		}
	}

	/**
	 * Handles one session, i.e. parses the HTTP request and returns the
	 * response.
	 */
	public interface IHTTPSession
	{
		void execute() throws IOException;

		Map<String, String> getParms();

		Map<String, String> getHeaders();

		/**
		 * @return the path part of the URL.
		 */
		String getUri();

		String getQueryParameterString();

		Method getMethod();

		InputStream getInputStream();

		/**
		 * Adds the files in the request body to the files map.
		 * 
		 * @param files map to modify
		 */
		void parseBody() throws IOException, ResponseException;
	}

	protected class HTTPSession implements IHTTPSession
	{
		public static final int BUFSIZE = 8192;
		private final OutputStream outputStream;
		private PushbackInputStream inputStream;
		private InputStream wrappedInput;
		private int splitbyte;
		private int rlen;
		private Method method;
		private Map<String, String> parms;
		private Map<String, String> headers;
		private String queryParameterString;
		private String remoteIp;
		private boolean doClose;

		public HTTPSession( InputStream inputStream, OutputStream outputStream, InetAddress inetAddress )
		{
			this.inputStream = new PushbackInputStream( inputStream, BUFSIZE );
			this.outputStream = outputStream;
			remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1"
					: inetAddress.getHostAddress().toString();
			headers = new HashMap<String, String>();
			parms = new HashMap<String, String>();
		}

		@Override
		public void execute() throws IOException
		{
			doClose = true;
			try {
				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				// Do NOT assume that a single read will get the entire header at once!
				byte[] buf = new byte[ BUFSIZE ];
				splitbyte = 0;
				rlen = 0;
				{
					int read = -1;
					try {
						read = inputStream.read( buf, 0, BUFSIZE );
					} catch ( Exception e ) {
						throw e;
					}
					if ( read == -1 ) {
						// socket was been closed
						throw new SocketException( "NanoHttpd Shutdown" );
					}
					while ( read > 0 ) {
						rlen += read;
						splitbyte = findHeaderEnd( buf, rlen );
						if ( splitbyte > 0 || rlen >= BUFSIZE )
							break;
						// Try to keep reading as long as we've got less than 8kb
						read = inputStream.read( buf, rlen, BUFSIZE - rlen );
						if ( maxRequestSize != 0 && rlen > maxRequestSize )
							throw new SocketException( "Request too large" );
					}
					if ( splitbyte == 0 ) {
						throw new SocketException( "Connection closed" );
					}
				}

				if ( splitbyte < rlen ) {
					inputStream.unread( buf, splitbyte, rlen - splitbyte );
				}

				parms.clear();
				headers.clear();

				if ( null != remoteIp ) {
					headers.put( "remote-addr", remoteIp );
					headers.put( "http-client-ip", remoteIp );
				}

				// Create a BufferedReader for parsing the header.
				BufferedReader hin = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( buf,
						0, splitbyte ) ) );

				// Decode the header into parms and header java properties
				decodeHeader( hin, parms, headers );

				if ( !Util.isEmptyString( headers.get( "trailer" ) ) ) {
					throw new ResponseException( Response.Status.BAD_REQUEST, "BAD REQUEST: Trailers not supported." );
				}

				method = Method.lookup( headers.get( "http-method" ) );
				if ( method == null ) {
					throw new ResponseException( Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error." );
				}

				doClose = "close".equalsIgnoreCase( headers.get( "connection" ) ) || "1.0".equals( headers.get( "http-version" ) );
				if ( !doClose && method != Method.GET
						&& !headers.containsKey( "content-length" ) && !"chunked".equals( headers.get( "transfer-encoding" ) ) ) {
					doClose = true;
				}

				// Ok, now do the serve()
				Response r = serve( this );
				if ( r == null ) {
					throw new ResponseException( Response.Status.INTERNAL_ERROR,
							"SERVER INTERNAL ERROR: Serve() returned a null response." );
				} else {
					r.setRequestMethod( method );
					r.send( outputStream, doClose );
				}
				if ( doClose ) {
					Util.safeClose( outputStream, inputStream );
				} else {
					InputStream is = getInputStream();
					if ( is != null ) {
						while ( is.read( buf ) > 0 ) {
						}
					}
				}
			} catch ( SocketException e ) {
				// throw it out to close socket object (finalAccept)
				throw e;
			} catch ( SocketTimeoutException ste ) {
				throw ste;
			} catch ( IOException ioe ) {
				Response r = new Response( Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage() );
				r.send( outputStream, doClose );
				Util.safeClose( outputStream );
			} catch ( ResponseException re ) {
				Response r = new Response( re.getStatus(), MIME_PLAINTEXT, re.getMessage() );
				r.send( outputStream, doClose );
				Util.safeClose( outputStream );
			}
		}

		@Override
		public void parseBody() throws IOException, ResponseException
		{
			// If the method is POST, there may be parameters
			// in data section, too, read it:
			if ( method != Method.POST )
				return;

			long size;
			if ( headers.containsKey( "content-length" ) ) {
				size = Util.parseInt( headers.get( "content-length" ), -1 );
			} else {
				size = -1;
			}

			String contentType = null;
			String contentEncoding = null;
			String contentTypeHeader = headers.get( "content-type" );

			StringTokenizer st = null;
			if ( contentTypeHeader != null ) {
				st = new StringTokenizer( contentTypeHeader, "," );
				if ( st.hasMoreTokens() ) {
					String part[] = st.nextToken().split( ";\\s*", 2 );
					contentType = part[0].trim();
					if ( part.length == 2 ) {
						contentEncoding = part[1];
					}
				}
			}
			Charset cs = StandardCharsets.UTF_8;
			if ( contentEncoding != null ) {
				try {
					cs = Charset.forName( contentEncoding );
				} catch ( Exception e ) {
				}
			}

			// Use method here so we get the limited/chunked stream if applicable
			InputStream is = getInputStream();
			if ( is == null )
				return;

			if ( "multipart/form-data".equalsIgnoreCase( contentType ) ) {
				throw new ResponseException( Response.Status.BAD_REQUEST,
						"BAD REQUEST: Content type is multipart/form-data, which is not supported" );
			} else if ( "application/x-www-form-urlencoded".equalsIgnoreCase( contentType ) ) {
				// Handle application/x-www-form-urlencoded
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte pbuf[] = new byte[ 2000 ];
				while ( size > 0 ) {
					int ret = is.read( pbuf, 0, (int)Math.min( size, pbuf.length ) );
					if ( ret <= 0 )
						break;
					if ( ret >= 2 && pbuf[ret - 1] == '\n' && pbuf[ret - 2] == '\r' )
						break;
					size -= ret;
					baos.write( pbuf, 0, ret );
				}
				String postLine = new String( baos.toByteArray(), cs );
				baos.close();

				decodeParms( postLine, parms );
			} // Otherwise leave stream untouched so app can deal with it
		}

		/**
		 * Decodes the sent headers and loads the data into Key/value pairs
		 */
		private void decodeHeader( BufferedReader in, Map<String, String> parms,
				Map<String, String> headers )
				throws ResponseException
		{
			try {
				// Read the request line
				String inLine = in.readLine();
				if ( inLine == null ) {
					return;
				}

				StringTokenizer st = new StringTokenizer( inLine );
				if ( !st.hasMoreTokens() ) {
					throw new ResponseException( Response.Status.BAD_REQUEST,
							"BAD REQUEST: Syntax error. Usage: GET /example/file.html" );
				}

				String strMethod = st.nextToken();

				if ( !st.hasMoreTokens() ) {
					throw new ResponseException( Response.Status.BAD_REQUEST,
							"BAD REQUEST: Missing URI. Usage: GET /example/file.html" );
				}

				String uri = st.nextToken();

				// Decode parameters from the URI
				int qmi = uri.indexOf( '?' );
				if ( qmi >= 0 ) {
					decodeParms( uri.substring( qmi + 1 ), parms );
					uri = decodePercent( uri.substring( 0, qmi ) );
				} else {
					uri = decodePercent( uri );
				}

				// If there's another token, its protocol version,
				// followed by HTTP headers.
				// NOTE: this now forces header names lower case since they are
				// case insensitive and vary by client.
				if ( st.hasMoreTokens() ) {
					String strVersion = st.nextToken();
					String line = in.readLine();
					while ( line != null && line.length() > 0 ) {
						int p = line.indexOf( ':' );
						if ( p >= 0 )
							headers.put( line.substring( 0, p ).trim().toLowerCase( Locale.US ),
									line.substring( p + 1 ).trim() );
						line = in.readLine();
					}
					// Add version after, to override headers
					int sl = strVersion.indexOf( '/' );
					if ( sl != -1 ) {
						headers.put( "http-version", strVersion.substring( sl + 1 ) );
					}
				}

				headers.put( "http-uri", uri );
				headers.put( "http-method", strMethod );
			} catch ( IOException ioe ) {
				throw new ResponseException( Response.Status.INTERNAL_ERROR,
						"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe );
			}
		}

		/**
		 * Find byte index separating header from body. Return value will point
		 * to first byte after the final \r\n\r\n.
		 */
		private int findHeaderEnd( final byte[] buf, int rlen )
		{
			int splitbyte = 0;
			while ( splitbyte + 3 < rlen ) {
				if ( buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && buf[splitbyte + 2] == '\r'
						&& buf[splitbyte + 3] == '\n' ) {
					return splitbyte + 4;
				}
				splitbyte++;
			}
			return -1;
		}

		/**
		 * Decodes parameters in percent-encoded URI-format ( e.g.
		 * "name=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Map. NOTE: this doesn't support multiple identical
		 * keys due to the
		 * simplicity of Map.
		 */
		private void decodeParms( String parms, Map<String, String> p )
		{
			if ( parms == null ) {
				queryParameterString = "";
				return;
			}

			queryParameterString = parms;
			StringTokenizer st = new StringTokenizer( parms, "&" );
			while ( st.hasMoreTokens() ) {
				String e = st.nextToken();
				int sep = e.indexOf( '=' );
				if ( sep >= 0 ) {
					p.put( decodePercent( e.substring( 0, sep ) ).trim(), decodePercent( e.substring( sep + 1 ) ) );
				} else {
					p.put( decodePercent( e ).trim(), "" );
				}
			}
		}

		@Override
		public final Map<String, String> getParms()
		{
			return parms;
		}

		public String getQueryParameterString()
		{
			return queryParameterString;
		}

		@Override
		public final Map<String, String> getHeaders()
		{
			return headers;
		}

		@Override
		public final String getUri()
		{
			return headers.get( "http-uri" );
		}

		@Override
		public final Method getMethod()
		{
			return method;
		}

		@SuppressWarnings( "deprecation" )
		@Override
		public final InputStream getInputStream()
		{
			if ( method == Method.GET )
				return null;
			if ( wrappedInput == null ) {
				String s = headers.get( "content-length" );
				if ( s != null ) {
					long cl = Util.parseLong( s, -1 );
					if ( cl == 0 )
						return null;
					if ( cl < 0 ) {
						doClose = true;
						return null;
					}
					BoundedInputStream bis = new BoundedInputStream( inputStream, cl );
					bis.setPropagateClose( false );
					return wrappedInput = bis;
				}
				s = headers.get( "transfer-encoding" );
				if ( s != null ) {
					if ( "chunked".equalsIgnoreCase( s.trim() ) )
						return wrappedInput = new ChunkedInputStream( inputStream );
				}
			} else {
				return wrappedInput;
			}
			if ( !doClose )
				return null;
			return inputStream;
		}
	}

}
