package org.openslx.virtualization;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version information.
 * 
 * The version information is used in the field of virtualization (for virtualizers, disk images,
 * virtualization configuration files, ...).
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Version implements Comparable<Version>
{
	/**
	 * Regular expression to parse a version from a {@link String}.
	 * <p>
	 * The regular expression matches a version if its textual version information is well-formed
	 * according to the following examples:
	 * 
	 * <pre>
	 *   52
	 *   4.31
	 *   5.10.13
	 * </pre>
	 */
	private static final String VERSION_NUMBER_REGEX = "^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?$";

	/**
	 * Major number of the version.
	 */
	private final short major;

	/**
	 * Minor number of the version.
	 */
	private final short minor;

	/**
	 * Name or description of the version.
	 */
	private final String name;

	/**
	 * Creates a new version.
	 * 
	 * The version consists of a major version, whereas the minor version is set to the value
	 * <code>0</code> and the version name is undefined.
	 * 
	 * @param major major version.
	 */
	public Version( short major )
	{
		this( major, Short.valueOf( "0" ), null );
	}

	/**
	 * Creates a new version.
	 * 
	 * The version consists of a major version labeled with a version name, whereas the minor version
	 * is set to the value <code>0</code>.
	 * 
	 * @param major major version.
	 * @param name version name.
	 */
	public Version( short major, String name )
	{
		this( major, Short.valueOf( "0" ), name );
	}

	/**
	 * Creates a new version.
	 * 
	 * The version consists of a major and a minor version, whereas the version name is undefined.
	 * 
	 * @param major major version.
	 * @param minor minor version.
	 */
	public Version( short major, short minor )
	{
		this( major, minor, null );
	}

	/**
	 * Creates a new version.
	 * 
	 * The version consists of a major and a minor version labeled with a version name.
	 * 
	 * @param major major version.
	 * @param minor minor version.
	 * @param name version name.
	 */
	public Version( short major, short minor, String name )
	{
		this.major = major;
		this.minor = minor;
		this.name = name;
	}

	/**
	 * Returns the major version.
	 * 
	 * @return major version.
	 */
	public short getMajor()
	{
		return this.major;
	}

	/**
	 * Returns the minor version.
	 * 
	 * @return minor version.
	 */
	public short getMinor()
	{
		return this.minor;
	}

	/**
	 * Returns the full version as {@link Integer}.
	 * 
	 * The full version consists of the major and minor version where both are combined in one
	 * {@link Integer} value. The upper 16-bits of the value represent the major number, whereas
	 * the lower 16-bits represent the minor number.
	 * 
	 * @return full version as {@link Integer}.
	 */
	public int getVersion()
	{
		final int major = this.major;
		final int minor = this.minor;

		return ( major << Short.SIZE ) | minor;
	}

	/**
	 * Returns the name of the version.
	 * 
	 * @return name of the version.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Checks if version is supported by a version from a list of supported versions.
	 * 
	 * @param supportedVersions list of supported versions.
	 * @return state whether version is supported by a version from the list of versions or not.
	 */
	public boolean isSupported( List<Version> supportedVersions )
	{
		return supportedVersions.contains( this );
	}

	/**
	 * Returns a version from a list of supported versions filtered by its given filter predicate.
	 * 
	 * @param byFilter filter predicate.
	 * @param supportedVersions list of supported versions.
	 * @return version from a list of supported versions filtered by its given filter predicate.
	 */
	private static Version getInstanceByPredicateFromVersions( Predicate<Version> byFilter,
			List<Version> supportedVersions )
	{
		return supportedVersions.stream().filter( byFilter ).findFirst().orElse( null );
	}

	/**
	 * Returns a version from a list of supported versions by its given major version.
	 * 
	 * @param major version.
	 * @param supportedVersions list of supported versions.
	 * @return version from a list of supported versions by its given major version.
	 */
	public static Version getInstanceByMajorFromVersions( short major, List<Version> supportedVersions )
	{
		final Predicate<Version> byMajor = version -> major == version.getMajor();
		return Version.getInstanceByPredicateFromVersions( byMajor, supportedVersions );
	}

	/**
	 * Returns a version from a list of supported versions by its given major and minor version.
	 * 
	 * @param major version.
	 * @param minor version.
	 * @param supportedVersions list of supported versions.
	 * @return version from a list of supported versions by its given major and minor version.
	 */
	public static Version getInstanceByMajorMinorFromVersions( short major, short minor,
			List<Version> supportedVersions )
	{
		final Predicate<Version> byMajorMinor = version -> major == version.getMajor() && minor == version.getMinor();
		return supportedVersions.stream().filter( byMajorMinor ).findFirst().orElse( null );
	}

	/**
	 * Checks if this version is smaller than a specified {@code version}.
	 * 
	 * @param version for comparison.
	 * @return state whether this version is smaller than the specified {@code version} or not.
	 */
	public boolean isSmallerThan( Version version )
	{
		return ( this.compareTo( version ) < 0 ) ? true : false;
	}

	/**
	 * Checks if this version is greater than a specified {@code version}.
	 * 
	 * @param version for comparison.
	 * @return state whether this version is greater than the specified {@code version} or not.
	 */
	public boolean isGreaterThan( Version version )
	{
		return ( this.compareTo( version ) > 0 ) ? true : false;
	}

	/**
	 * Creates a new version parsed from a {@link String}.
	 * 
	 * The version consists of a major and a minor version parsed from the specified {@link String}.
	 * 
	 * @param version textual information containing a version as {@link String}. The textual
	 *           version should be well-formed according to the defined regular expression
	 *           {@link #VERSION_NUMBER_REGEX}.
	 * @return version instance.
	 */
	public static Version valueOf( String version )
	{
		final Version parsedVersion;

		if ( version == null || version.isEmpty() ) {
			parsedVersion = null;
		} else {
			final Pattern versionPattern = Pattern.compile( Version.VERSION_NUMBER_REGEX );
			final Matcher versionMatcher = versionPattern.matcher( version );

			if ( versionMatcher.find() ) {
				final String majorStr = versionMatcher.group( 1 );
				final String minorStr = versionMatcher.group( 2 );

				final short major = ( majorStr != null ) ? Short.valueOf( majorStr ) : 0;
				final short minor = ( minorStr != null ) ? Short.valueOf( minorStr ) : 0;

				parsedVersion = new Version( major, minor );

			} else {
				parsedVersion = null;
			}
		}

		return parsedVersion;
	}

	@Override
	public String toString()
	{
		if ( this.getName() == null || this.getName().isEmpty() ) {
			return String.format( "%d.%d", this.getMajor(), this.getMinor() );
		} else {
			return String.format( "%d.%d %s", this.getMajor(), this.getMinor(), this.getName() );
		}
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else if ( this.compareTo( Version.class.cast( obj ) ) == 0 ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo( Version v )
	{
		// compare the current version to the specified version
		if ( this.getMajor() < v.getMajor() ) {
			// current major version is smaller than the given major version
			return -1;
		} else if ( this.getMajor() > v.getMajor() ) {
			// current major version is larger than the given major version
			return 1;
		} else {
			// current major version is equal to the given major version
			// so compare the current minor version to the specified minor version
			if ( this.getMinor() < v.getMinor() ) {
				// current minor version is smaller than the given minor version
				// so the entire version is smaller than the given version
				return -1;
			} else if ( this.getMinor() > v.getMinor() ) {
				// current minor version is larger than the given minor version
				// so the entire version is larger than the given version
				return 1;
			} else {
				// current minor version is equal to the given minor version
				// so the entire version is equal to the given version
				return 0;
			}
		}
	}

	@Override
	public int hashCode()
	{
		return ( Short.valueOf( this.getMajor() ).hashCode() ) ^ ( Short.valueOf( this.getMinor() ).hashCode() );
	}
}
