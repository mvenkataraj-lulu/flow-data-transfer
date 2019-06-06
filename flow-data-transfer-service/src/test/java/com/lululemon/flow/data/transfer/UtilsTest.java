package com.lululemon.flow.data.transfer;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Utils}
 *
 */
public class UtilsTest {


    @Test
    public void testFileAsString() throws IOException {

        // Execute
        String result = Utils.fileAsString("files/sqlFile.sql", new DefaultResourceLoader());

        // Verify
        assertEquals("INSERT INTO TABLE (COLUMN1) VALUES ('');", result);
    }

    @Test(expected = FileNotFoundException.class)
    public void testFileAsStringExceptionWithClassLoader() throws IOException {

        // Execute
        Utils.fileAsString("files/fakse.sql", new DefaultResourceLoader());
    }

    @Test(expected = RuntimeException.class)
    public void testFileAsStringException() throws IOException {

        // Execute
        Utils.fileAsString("files/fakse.sql", null);
    }
}
