package test.resource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import test.resource.ResourceUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.resource.TestResourceUtil.TEST_RESOURCE_DIR;

/**
 * @author smid
 * @since 2021-05-27
 */
public class TransformOutputResourceUtil extends ResourceUtil {

    static private boolean WRITE_OUTPUT_INTO_FILE = true;

    public TransformOutputResourceUtil(String rootDir) {
        super(TEST_RESOURCE_DIR + '/' + rootDir);
    }

//    public Reader createFileReader(final File file) throws FileNotFoundException {
//        return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
//    }

    public Writer createFileWriter(String file) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                getResourcePath(file).toFile()),
                StandardCharsets.UTF_8));
    }

    public File getOutputSchemaFile(final String fileName) {
        return getFileResourcePath(fileName + ".xsd").toFile();
    }

    public void writeOutputSchemas(final XmlSchemaCollection outputSchemaCollection, final Set<String> schemaNames) {
        for (String schemaName : schemaNames) {
            XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);

            assertEquals(1, outputSchemas.length, "Multiple schemas of same system name: " + schemaName);

            if (WRITE_OUTPUT_INTO_FILE == true) {
                try {
                    // Output XSD
                    for (int i = 0; i < outputSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (outputSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (outputSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += ".xsd";

                        try (Writer writer = createFileWriter(outFileName)) {
                            outputSchemas[i].write(writer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
