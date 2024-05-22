package fi.iki.elonen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ChunkedInputStreamTest
{

	@Test
	@DisplayName( "Simple decoding test" )
	public void testChunkedInput() throws IOException
	{
		String data = "4\r\nWiki\r\n7\r\npedia i\r\nB;1h\ra\nllo\r\nn \r\nchunks.\r\n0\r\n\r\n";
		String expected = "Wikipedia in \r\nchunks.";
		ChunkedInputStream stream = new ChunkedInputStream( new StringBufferInputStream( data ) );
		StringBuilder sb = new StringBuilder();
		int n;
		byte[] buf = new byte[ 6 ];
		while ( ( n = stream.read( buf ) ) > 0 ) {
			sb.append( new String( buf, 0, n, StandardCharsets.US_ASCII ) );
		}
		assertEquals( sb.toString(), expected );
	}

}
