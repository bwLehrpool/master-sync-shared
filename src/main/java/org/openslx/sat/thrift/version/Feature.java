package org.openslx.sat.thrift.version;

public enum Feature {

	/**
	 * Server can properly extend the expiration time of an image version that
	 * is already expired, but has not been deleted yet. (Early versions of dmsd
	 * did not handle this case properly.)
	 */
	EXTEND_EXPIRED_VM,

}