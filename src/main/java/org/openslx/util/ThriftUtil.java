package org.openslx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openslx.util.vm.VmwareConfig;

public class ThriftUtil {

	public static List<byte[]> unwrapByteBufferList(List<ByteBuffer> blockHashes) {
		if (blockHashes == null || blockHashes.isEmpty())
			return null;
		List<byte[]> hashList = new ArrayList<>(blockHashes.size());
		for (ByteBuffer hash : blockHashes) {
			byte[] buffer = new byte[hash.remaining()];
			hash.mark();
			hash.get(buffer);
			hash.reset();
			hashList.add(buffer);
		}
		return hashList;
	}

	public static byte[] unwrapByteBuffer(ByteBuffer buffer) {
		byte[] byteArray = null;
		if (buffer != null) {
			byteArray = new byte[buffer.remaining()];
			buffer.mark();
			buffer.get(byteArray);
			buffer.reset();
		}
		return byteArray;
	}

	public static String byteBufferToString(ByteBuffer buffer) {
		byte[] bytes = unwrapByteBuffer(buffer);
		BufferedReader reader;
		StringBuffer content = new StringBuffer("");
		try {
			reader = VmwareConfig.getVmxReader(bytes, bytes.length);
			String line="";
			while ((line=reader.readLine()) != null) {
				content.append(line + System.lineSeparator());
			}
			reader.close();
		} catch (IOException e) {
			// swallow - shouldn't happen. 
			return null;
		}
		return content.toString();
	}
}
