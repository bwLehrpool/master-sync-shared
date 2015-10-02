package org.openslx.thrifthelper;

public class TransferStatusWrapper
{
	
	public static enum BlockStatus {
		COMPLETE, MISSING, UPLOADING, QUEUED_FOR_COPYING, COPYING, HASHING;
	}
	
	// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying, 5 = hashing
	private byte[] blocks = null;
	
	public TransferStatusWrapper(byte[] blocks) {
		this.blocks = blocks;
	}
	
	public void setBlocks(byte[] blocks) {
		this.blocks = blocks;
	}
	
	public boolean isComplete() {
		for (byte block : blocks) {
			if (block != 0)
				return false;
		}
		return true;
	}
	
	public float getComplete() {
		int done = 0;
		for (byte block : blocks) {
			if (block == 0) {
				done++;
			}
		}
		return ((float)done / (float)blocks.length);
	}
	
	public float getPercentComplete() {
		 return getComplete() * 100f;
	}
	
	public byte[] getBlocks() {
		return this.blocks;
	}
	
	public boolean isEmpty() {
		return blocks == null || blocks.length == 0;
	}
	
	public int getBlockCount() {
		return blocks.length;
	}
	
	public BlockStatus get(int index) {
		switch (blocks[index]) {
		case 0:
			return BlockStatus.COMPLETE;
		case 1:
			return BlockStatus.MISSING;
		case 2:
			return BlockStatus.UPLOADING;
		case 3:
			return BlockStatus.QUEUED_FOR_COPYING;
		case 4:
			return BlockStatus.COPYING;
		}
		return null;
	}

}
