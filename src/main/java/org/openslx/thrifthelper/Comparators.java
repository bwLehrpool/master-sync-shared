package org.openslx.thrifthelper;

import java.util.Comparator;

import org.openslx.bwlp.thrift.iface.ImageSummaryRead;
import org.openslx.bwlp.thrift.iface.ImageVersionDetails;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Organization;
import org.openslx.bwlp.thrift.iface.Virtualizer;

/**
 * A bunch of comparators for thrift classes. They will compare on what would be considered the
 * identifier ("primary key") of a struct, unless stated otherwise.
 */
public class Comparators
{

	public static final Comparator<ImageVersionDetails> imageVersionDetails = new Comparator<ImageVersionDetails>() {
		@Override
		public int compare( ImageVersionDetails o1, ImageVersionDetails o2 )
		{
			if ( o1 == null || o1.versionId == null )
				return o2 == null || o2.versionId == null ? 0 : 1;
			if ( o2 == null || o2.versionId == null )
				return -1;
			return o1.versionId.compareTo( o2.versionId );
		}
	};

	public static final Comparator<ImageSummaryRead> imageSummaryRead = new Comparator<ImageSummaryRead>() {
		@Override
		public int compare( ImageSummaryRead o1, ImageSummaryRead o2 )
		{
			if ( o1 == null || o1.imageBaseId == null )
				return o2 == null || o2.imageBaseId == null ? 0 : 1;
			if ( o2 == null || o2.imageBaseId == null )
				return -1;
			return o1.imageBaseId.compareTo( o2.imageBaseId );
		}
	};

	public static final Comparator<OperatingSystem> operatingSystem = new Comparator<OperatingSystem>() {
		@Override
		public int compare( OperatingSystem o1, OperatingSystem o2 )
		{
			if ( o1 == null )
				return o2 == null ? 0 : 1;
			if ( o2 == null )
				return -1;
			return o1.osId - o2.osId;
		}
	};

	public static final Comparator<Virtualizer> virtualizer = new Comparator<Virtualizer>() {
		@Override
		public int compare( Virtualizer o1, Virtualizer o2 )
		{
			if ( o1 == null || o1.virtId == null )
				return o2 == null || o2.virtId == null ? 0 : 1;
			if ( o2 == null || o2.virtId == null )
				return -1;
			return o1.virtId.compareTo( o2.virtId );
		}
	};

	public static final Comparator<Organization> organization = new Comparator<Organization>() {
		@Override
		public int compare( Organization o1, Organization o2 )
		{
			if ( o1 == null || o1.organizationId == null )
				return o2 == null || o2.organizationId == null ? 0 : 1;
			if ( o2 == null || o2.organizationId == null )
				return -1;
			return o1.organizationId.compareTo( o2.organizationId );
		}
	};

}