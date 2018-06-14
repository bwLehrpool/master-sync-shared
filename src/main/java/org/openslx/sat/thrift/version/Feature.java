package org.openslx.sat.thrift.version;

public enum Feature {

	/**
	 * Server can properly extend the expiration time of an image version that
	 * is already expired, but has not been deleted yet. (Early versions of dmsd
	 * did not handle this case properly.)
	 */
	EXTEND_EXPIRED_VM,
	
	/**
	 * Server supports configuring network shares for individual lectures. Whether 
	 * these will function on the clients further depends on the minilinux version!
	 */
	NETWORK_SHARES,

	/**
	 * Server supports multiple hypervisors which requires special handling since
	 * multiple components needs to be compatible to fully support them.
	 */
	MULTIPLE_HYPERVISORS,
	
	/**
	 * Server supports copying existing blocks server side.
	 */
	SERVER_SIDE_COPY,
	
	/**
	 * Server supports filtering lectures by LDAP/AD attributes 
	 */
	LECTURE_FILTER_LDAP,
	
}
