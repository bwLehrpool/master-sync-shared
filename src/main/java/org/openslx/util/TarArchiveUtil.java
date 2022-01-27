package org.openslx.util;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class TarArchiveUtil {

	private TarArchiveUtil() {}

	public static class TarArchiveReader implements AutoCloseable {
		private boolean isCompressed;
		private final TarInputStream tarInputStream;

		private TarEntry currentEntry = null;

		public TarArchiveReader(InputStream in) throws IOException {
			this(in,true,false);
		}

		public TarArchiveReader(InputStream in, boolean isBuffered, boolean isCompressed) throws IOException {
			this.isCompressed = isCompressed;
			
			InputStream stream = in;
			if (isBuffered) {
				stream = new BufferedInputStream(stream);
			}

			if (isCompressed) {
				stream = new GZIPInputStream(stream);
			}

			this.tarInputStream = new TarInputStream(stream);
		}

		public boolean hasNextEntry() throws IOException {
			this.currentEntry = this.tarInputStream.getNextEntry();
			if (this.currentEntry != null) {
				return true;
			} else {
				return false;
			}
		}

		public String getEntryName() {
			return this.currentEntry.getName();
		}

		public byte[] readCurrentEntry() throws IOException	{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] rawData = new byte[1024];
			int count = 0;

			while ((count = this.tarInputStream.read(rawData)) != -1) {
				output.write(rawData, 0, count);
			}

			return output.toByteArray();
		}

		@Override
		public void close() throws IOException {
			tarInputStream.close();			
		}
	
	}

	public static class TarArchiveWriter implements AutoCloseable
	{
		private boolean isBuffered;
		private boolean isCompressed;
		private final TarOutputStream tarOutputStream;
	
		public TarArchiveWriter (OutputStream out) throws IOException {
			this(out, true, true);
		}

		public TarArchiveWriter (OutputStream out, boolean isBuffered, boolean isCompressed) throws IOException {
			this.isBuffered = isBuffered;
			this.isCompressed = isCompressed;

			OutputStream stream = out;
			
			if (isBuffered) {
				stream = new BufferedOutputStream(stream);
			}

			if (isCompressed) {
				stream = new GZIPOutputStream(stream);
			}

			this.tarOutputStream = new TarOutputStream(stream);
		}

		public void writeFile(String filename, String data) throws IOException {

			if (data == null)
				return;
			putFile(filename, data.getBytes(StandardCharsets.UTF_8));
		}

		public void writeFile(String filename, byte[] data) throws IOException {
			if (data == null)
				return;
			putFile(filename, data);
		}

		private void putFile(String filename, byte[] data) throws IOException {
			if (data == null)
				return;
			tarOutputStream.putNextEntry(new TarEntry(TarHeader.createHeader(filename,
				data.length, Util.unixTime(), false, 0644)));
			tarOutputStream.write(data);
		}

		@Override
		public void close() throws Exception {
			tarOutputStream.close();
		}
	}
}
