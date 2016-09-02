package org.openslx.thrifthelper;

import java.util.Comparator;

import org.openslx.bwlp.thrift.iface.ImageSummaryRead;
import org.openslx.bwlp.thrift.iface.ImageVersionDetails;
import org.openslx.bwlp.thrift.iface.Location;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Organization;
import org.openslx.bwlp.thrift.iface.UserInfo;
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

	public static final Comparator<OperatingSystem> operatingSystemByName = new Comparator<OperatingSystem>() {
		@Override
		public int compare( OperatingSystem o1, OperatingSystem o2 )
		{
			if ( o1 == null )
				return o2 == null ? 0 : 1;
			if ( o2 == null )
				return -1;
			return o1.osName.compareTo(o2.osName);
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
	
	public static final Comparator<UserInfo> user = new Comparator<UserInfo>() {
		@Override
		public int compare( UserInfo o1, UserInfo o2 )
		{
			if ( o1 == null || o1.userId == null )
				return o2 == null || o2.userId == null ? 0 : 1;
			if ( o2 == null || o2.userId == null )
				return -1;
			return o1.userId.compareTo( o2.userId );
		}
	};

	public static final Comparator<Location> location = new Comparator<Location>() {
		@Override
		public int compare( Location o1, Location o2 )
		{
			if ( o1 == null || o1.locationName == null )
				return o2 == null || o2.locationName == null ? 0 : 1;
			if ( o2 == null || o2.locationName == null )
				return -1;
			return o1.locationName.compareTo( o2.locationName );
		}
	};

}
