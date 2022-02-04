package org.openslx.libvirt.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class LibvirtXmlDocumentStub extends LibvirtXmlDocument
{
	public LibvirtXmlDocumentStub( File xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml );
	}

	public LibvirtXmlDocumentStub( File xml, InputStream rngSchema )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, rngSchema );
	}
}

public class LibvirtXmlDocumentTest
{
	private static final String EMPTY = new String();

	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		Configurator.setRootLevel( Level.OFF );
	}

	private LibvirtXmlDocument newLibvirtXmlDocumentInstance( String xmlFileName )
	{
		LibvirtXmlDocument document = null;

		try {
			File xmlFile = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
			document = new LibvirtXmlDocumentStub( xmlFile );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
			String errorMsg = new String( "Cannot prepare requested Libvirt XML file from the resources folder" );
			fail( errorMsg );
		}

		return document;
	}

	private LibvirtXmlDocument newLibvirtXmlDocumentValidationInstance( String xmlFileName, String rngSchemaFileName )
			throws LibvirtXmlValidationException
	{
		LibvirtXmlDocument document = null;

		try {
			File xmlFile = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
			InputStream rngSchema = LibvirtXmlResources.getLibvirtRng( rngSchemaFileName );
			document = new LibvirtXmlDocumentStub( xmlFile, rngSchema );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException e ) {
			String errorMsg = new String( "Cannot prepare requested Libvirt XML file from the resources folder" );
			fail( errorMsg );
		}

		return document;
	}

	private static long countLines( Reader input ) throws IOException
	{
		final BufferedReader bfrContent = new BufferedReader( input );
		return bfrContent.lines().count();
	}

	public static long countLinesFromString( String input ) throws IOException
	{
		return LibvirtXmlDocumentTest.countLines( new StringReader( input ) );
	}

	public static long countLinesFromFile( File input ) throws IOException
	{
		return LibvirtXmlDocumentTest.countLines( new FileReader( input ) );
	}

	@Test
	@DisplayName( "Read libvirt XML file to String" )
	public void testReadXmlFileToString() throws LibvirtXmlSerializationException, IOException
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		File originalXmlFile = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-ubuntu-20-04-vm.xml" );

		final String readXmlContent = vm.toXml();

		assertNotNull( readXmlContent );

		final long lengthReadXmlContent = LibvirtXmlDocumentTest.countLinesFromString( readXmlContent );
		final long lengthOriginalXmlContent = LibvirtXmlDocumentTest.countLinesFromFile( originalXmlFile );

		assertEquals( lengthOriginalXmlContent, lengthReadXmlContent );
	}

	@Test
	@DisplayName( "Read libvirt XML file to file" )
	public void testReadXmlFileToFile() throws LibvirtXmlSerializationException, IOException
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		File originalXmlFile = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		File readXmlFile = LibvirtXmlTestResources.createLibvirtXmlTempFile();

		vm.toXml( readXmlFile );

		final String readXmlContent = FileUtils.readFileToString( readXmlFile, StandardCharsets.UTF_8 );
		final String originalXmlContent = FileUtils.readFileToString( originalXmlFile, StandardCharsets.UTF_8 );

		assertNotNull( readXmlContent );

		final long lengthReadXmlContent = LibvirtXmlDocumentTest.countLinesFromString( readXmlContent );
		final long lengthOriginalXmlContent = LibvirtXmlDocumentTest.countLinesFromString( originalXmlContent );

		assertEquals( lengthOriginalXmlContent, lengthReadXmlContent );
	}

	@Test
	@DisplayName( "Validate correct libvirt XML file" )
	public void testValidateCorrectXmlFile()
	{
		Executable validateXmlDocument = () -> {
			this.newLibvirtXmlDocumentValidationInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml", "domain.rng" );
		};

		assertDoesNotThrow( validateXmlDocument );
	}

	@Test
	@DisplayName( "Validate incorrect libvirt XML file" )
	public void testValidateIncorrectXmlFile()
	{
		Executable validateXmlDocument = () -> {
			this.newLibvirtXmlDocumentValidationInstance( "qemu-kvm_default-ubuntu-20-04-vm-invalid.xml", "domain.rng" );
		};

		assertThrows( LibvirtXmlValidationException.class, validateXmlDocument );
	}

	@Test
	@DisplayName( "Get non-existent node from libvirt XML file" )
	public void testGetNonExistentElement()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getRootXmlNode().getXmlElement( "info" ) );
	}

	@Test
	@DisplayName( "Set non-existent node in libvirt XML file" )
	public void testSetNonExistentElement()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElement( "info" );
		assertNotNull( vm.getRootXmlNode().getXmlElement( "info" ) );
	}

	@Test
	@DisplayName( "Get non-existent element's value in libvirt XML file" )
	public void testGetNonExistentElementValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getRootXmlNode().getXmlElementValue( "info" ) );
	}

	@Test
	@DisplayName( "Set non-existent element's value in libvirt XML file" )
	public void testSetNonExistentElementValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElementValue( "info", "content" );
		assertEquals( "content", vm.getRootXmlNode().getXmlElementValue( "info" ) );
	}

	@Test
	@DisplayName( "Get empty element from libvirt XML file" )
	public void testGetEmptyElement()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNotNull( vm.getRootXmlNode().getXmlElement( "features/acpi" ) );
	}

	@Test
	@DisplayName( "Set empty element in libvirt XML file" )
	public void testSetEmptyElement()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElement( "features/acpi" );
		assertNotNull( vm.getRootXmlNode().getXmlElement( "features/acpi" ) );
	}

	@Test
	@DisplayName( "Get empty element's value from libvirt XML file" )
	public void testGetEmptyElementValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( EMPTY, vm.getRootXmlNode().getXmlElementValue( "features/acpi" ) );
	}

	@Test
	@DisplayName( "Set empty element's value in libvirt XML file" )
	public void testSetEmptyElementValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElementValue( "features/acpi", "content" );
		assertEquals( "content", vm.getRootXmlNode().getXmlElementValue( "features/acpi" ) );
	}

	@Test
	@DisplayName( "Get non-existent element's attribute value from libvirt XML file" )
	public void testGetNonExistentElementAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getRootXmlNode().getXmlElementAttributeValue( "info", "test" ) );
	}

	@Test
	@DisplayName( "Set non-existent element's attribute value from libvirt XML file" )
	public void testSetNonExistentElementAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElementAttributeValue( "info", "test", "info" );
		assertEquals( "info", vm.getRootXmlNode().getXmlElementAttributeValue( "info", "test" ) );
	}

	@Test
	@DisplayName( "Get element's non-existent attribute value from libvirt XML file" )
	public void testGetElementNonExistentAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getRootXmlNode().getXmlElementAttributeValue( "features/acpi", "test" ) );
	}

	@Test
	@DisplayName( "Set element's non-existent attribute value from libvirt XML file" )
	public void testSetElementNonExistentAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElementAttributeValue( "features/acpi", "test", "info" );
		assertEquals( "info", vm.getRootXmlNode().getXmlElementAttributeValue( "features/acpi", "test" ) );
	}

	@Test
	@DisplayName( "Get element's attribute value from libvirt XML file" )
	public void testGetElementAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "partial", vm.getRootXmlNode().getXmlElementAttributeValue( "cpu", "check" ) );
	}

	@Test
	@DisplayName( "Set element's attribute value from libvirt XML file" )
	public void testSetElementAttributeValue()
	{
		LibvirtXmlDocument vm = this.newLibvirtXmlDocumentInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.getRootXmlNode().setXmlElementAttributeValue( "cpu", "check", "full" );
		assertEquals( "full", vm.getRootXmlNode().getXmlElementAttributeValue( "cpu", "check" ) );
	}
}
