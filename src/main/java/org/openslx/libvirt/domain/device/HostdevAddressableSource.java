package org.openslx.libvirt.domain.device;

/**
 * Addressable source operations for a hostdev device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the source.
 */
public abstract interface HostdevAddressableSource<T>
{
	/**
	 * Returns the source of the source device (on the Libvirt host).
	 * 
	 * @return source of the source device (on the Libvirt host).
	 */
	public T getSource();

	/**
	 * Sets the source for the source device (on the Libvirt host).
	 * 
	 * @param source source for the source device (on the Libvirt host).
	 */
	public void setSource( T source );
}
