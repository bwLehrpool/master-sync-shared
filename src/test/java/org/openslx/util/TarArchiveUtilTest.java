package org.openslx.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.util.TarArchiveUtil.TarArchiveReader;
import org.openslx.util.TarArchiveUtil.TarArchiveWriter;

public class TarArchiveUtilTest {

    @Test
    @DisplayName( "Test creating tgz file" )
    public void testCreateTarGz() throws IOException 
    {
        // dummy content
        final String DUMMY_FILENAME = "test";
        final String DUMMY_FILE_DATA = "Hello World";

        // create targz file with dummy content
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarArchiveWriter tarArchiveWriter = new TarArchiveWriter(out);

        tarArchiveWriter.writeFile(DUMMY_FILENAME, DUMMY_FILE_DATA);
        tarArchiveWriter.close();


        // read created targz file,
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TarArchiveReader tarArchiveReader = new TarArchiveReader(in, true, true);

        assertTrue(tarArchiveReader.hasNextEntry(), "Tar Archive should contain a file");
        assertEquals(DUMMY_FILENAME, tarArchiveReader.getEntryName());

        String test_string = new String(tarArchiveReader.readCurrentEntry(), StandardCharsets.UTF_8);
        assertEquals(DUMMY_FILE_DATA, test_string);        
        tarArchiveReader.close();
    }
}
