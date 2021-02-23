package org.openslx.thrifthelper;

import org.openslx.bwlp.thrift.iface.ImagePublishData;

public class ImagePublishDataEx extends ImagePublishData
{
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 314945044011262005L;

	public String exImagePath;
	public boolean exIsValid;
}
