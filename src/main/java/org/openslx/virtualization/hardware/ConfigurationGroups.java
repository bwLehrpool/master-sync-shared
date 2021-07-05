package org.openslx.virtualization.hardware;

public enum ConfigurationGroups
{

	NIC_MODEL( "E0VirtDev" ), 
	USB_SPEED( "maxUSBSpeed" ), 
	SOUND_CARD_MODEL( "sound" ), 
	GFX_TYPE( "3DAcceleration" ), 
	HW_VERSION( "HWVersion" );

	/** Identifier to use when looking up translation for this group. Should never change */
	public final String i18n;

	private ConfigurationGroups( String i18n )
	{
		this.i18n = i18n;
	}

}
