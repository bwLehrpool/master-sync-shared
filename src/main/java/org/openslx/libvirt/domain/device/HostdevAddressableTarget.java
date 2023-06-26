package org.openslx.libvirt.domain.device;

/**
 * Addressable target operations for a hostdev device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the target.
 */
public abstract interface HostdevAddressableTarget<T>
{
	/**
	 * Returns the target of the target device (in the virtual machine).
	 * 
	 * @return target of the target device (in the virtual machine).
	 */
	public T getPciTarget();

	/**
	 * Sets the target for the target device (in the virtual machine).
	 * 
	 * @param target target for the target device (in the virtual machine).
	 */
	public void setPciTarget( T target );
}
