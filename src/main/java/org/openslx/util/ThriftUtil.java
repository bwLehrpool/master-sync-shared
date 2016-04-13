package org.openslx.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

}
