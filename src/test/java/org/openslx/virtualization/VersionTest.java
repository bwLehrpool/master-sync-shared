package org.openslx.virtualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VersionTest
{
	@Test
	@DisplayName( "Test that version is supported in list of versions" )
	public void testVersionIsSupported()
	{
		final Version version = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );
		final List<Version> versions = Collections.unmodifiableList( Arrays.asList(
				new Version( Short.valueOf( "2" ) ),
				new Version( Short.valueOf( "4" ), Short.valueOf( "3" ) ),
				new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) ),
				new Version( Short.valueOf( "1" ), Short.valueOf( "3" ) ) ) );

		assertTrue( version.isSupported( versions ) );
	}

	@Test
	@DisplayName( "Test that version is not supported in list of versions" )
	public void testVersionIsNotSupported()
	{
		final Version version = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );
		final List<Version> versions = Collections.unmodifiableList( Arrays.asList(
				new Version( Short.valueOf( "2" ) ),
				new Version( Short.valueOf( "4" ), Short.valueOf( "3" ) ),
				new Version( Short.valueOf( "6" ), Short.valueOf( "9" ) ),
				new Version( Short.valueOf( "1" ), Short.valueOf( "3" ) ) ) );

		assertFalse( version.isSupported( versions ) );
	}

	@Test
	@DisplayName( "Test that versions are equal" )
	public void testVersionEquals()
	{
		final Version versionOne = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );
		final Version versionTwo = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );

		assertTrue( versionOne.equals( versionTwo ) );
		assertTrue( versionTwo.equals( versionOne ) );
	}

	@Test
	@DisplayName( "Test that versions are not equal" )
	public void testVersionNotEquals()
	{
		final Version versionOne = new Version( Short.valueOf( "3" ), Short.valueOf( "2" ) );
		final Version versionTwo = new Version( Short.valueOf( "3" ), Short.valueOf( "3" ) );

		assertFalse( versionOne.equals( versionTwo ) );
		assertFalse( versionTwo.equals( versionOne ) );
	}

	@Test
	@DisplayName( "Test that version is smaller than" )
	public void testVersionSmallerThan()
	{
		final Version versionOne = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );
		final Version versionTwo = new Version( Short.valueOf( "3" ), Short.valueOf( "2" ) );

		assertEquals( -1, versionOne.compareTo( versionTwo ) );
		assertEquals( 1, versionTwo.compareTo( versionOne ) );
	}

	@Test
	@DisplayName( "Test that version is larger than" )
	public void testVersionLargerThan()
	{
		final Version versionOne = new Version( Short.valueOf( "3" ), Short.valueOf( "3" ) );
		final Version versionTwo = new Version( Short.valueOf( "3" ), Short.valueOf( "2" ) );

		assertEquals( 1, versionOne.compareTo( versionTwo ) );
		assertEquals( -1, versionTwo.compareTo( versionOne ) );
	}

	@Test
	@DisplayName( "Test that versions are equal (compareTo)" )
	public void testVersionEqualCompareTo()
	{
		final Version versionOne = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );
		final Version versionTwo = new Version( Short.valueOf( "2" ), Short.valueOf( "3" ) );

		assertEquals( 0, versionOne.compareTo( versionTwo ) );
		assertEquals( 0, versionTwo.compareTo( versionOne ) );
	}
}
