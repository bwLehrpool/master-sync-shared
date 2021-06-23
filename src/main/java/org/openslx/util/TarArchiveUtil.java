package org.openslx.util;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TarArchiveUtil {



	public static void tarPutFile(TarOutputStream output, String fileName, String data) throws IOException
	{
		if (data == null)
			return;
		tarPutFile(output, fileName, data.getBytes(StandardCharsets.UTF_8));
	}

	public static void tarPutFile(TarOutputStream output, String fileName, byte[] data) throws IOException
	{
		if (data == null)
			return;
		output.putNextEntry(new TarEntry(
				TarHeader.createHeader(fileName, data.length, Util.unixTime(), false, 0644)));
		output.write(data);
	}
}
