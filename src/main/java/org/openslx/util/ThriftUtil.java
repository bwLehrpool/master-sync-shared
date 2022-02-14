package org.openslx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openslx.virtualization.configuration.VirtualizationConfigurationVmwareFileFormat;

public class ThriftUtil {

	public static List<byte[]> unwrapByteBufferList(List<ByteBuffer> blockHashes) {
		if (blockHashes == null || blockHashes.isEmpty())
			return null;
		List<byte[]> hashList = new ArrayList<>(blockHashes.size());
		for (ByteBuffer hash : blockHashes) {
			byte[] buffer = new byte[hash.remaining()];
			((Buffer)hash).mark();
			hash.get(buffer);
			((Buffer)hash).reset();
			hashList.add(buffer);
		}
		return hashList;
	}

	public static byte[] unwrapByteBuffer(ByteBuffer buffer) {
		byte[] byteArray = null;
		if (buffer != null) {
			byteArray = new byte[buffer.remaining()];
			((Buffer)buffer).mark();
			buffer.get(byteArray);
			((Buffer)buffer).reset();
		}
		return byteArray;
	}

	public static String byteBufferToString(ByteBuffer buffer) {
		byte[] bytes = unwrapByteBuffer(buffer);
		BufferedReader reader;
		StringBuffer content = new StringBuffer("");
		try {
			// Why is a generic function to convert a buffer to string CALLING A VMWARE SPECIFIC FUNCTION!?
			reader = VirtualizationConfigurationVmwareFileFormat.getVmxReader(bytes, bytes.length);
			String line="";
			while ((line=reader.readLine()) != null) {
				content.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			// swallow - shouldn't happen. 
			return null;
		}
		return content.toString();
	}
}
