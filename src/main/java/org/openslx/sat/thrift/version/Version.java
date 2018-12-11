package org.openslx.sat.thrift.version;

/**
 * This class merely contains the RPC version of the sat-server.
 * If you modify the thrift RPC in an incompatible way, bump the version number by
 * one, so connecting clients will be notified that they need to update
 * their dozentenmodul client.
 */
public class Version {
	
	public static final long MIN_VERSION = 4;

	public static final long VERSION = 5;
	
}
