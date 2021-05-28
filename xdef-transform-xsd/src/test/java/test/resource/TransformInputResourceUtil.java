package test.resource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static test.resource.ResourceConst.XDEFINITION_FILE_EXT;
import static test.resource.ResourceConst.XML_FILE_EXT;
import static test.resource.ResourceConst.XML_SCHEMA_FILE_EXT;
import static test.resource.TestResourceUtil.TEST_RESOURCE_DIR;

/**
 * @author smid
 * @since 2021-05-27
 */
public class TransformInputResourceUtil extends ResourceUtil {

    public TransformInputResourceUtil(String rootDir) {
        super(TEST_RESOURCE_DIR + '/' + rootDir);
    }

    public static Reader createFileReader(final File file) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
    }

    public Reader createFileReader(final String fileName) throws FileNotFoundException {
        return createFileReader(getFileResourcePath(fileName).toFile());
    }

    public File getXmlDataFile(final String fileName) {
        return getFileResourcePath(
                Paths.get("data")
                        .resolve(fileName + XML_FILE_EXT)
                        .toString()
        ).toFile();
    }

    public File getSchemaFile(final String fileName) {
        return getFileResourcePath(fileName + XML_SCHEMA_FILE_EXT).toFile();
    }

    public File getXDefFile(final String fileName) {
        return getFileResourcePath(fileName + XDEFINITION_FILE_EXT).toFile();
    }

    public XmlSchemaCollection createXmlSchemaCollection(final String fileName) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        schemaCollection.setBaseUri(getRootDir().toAbsolutePath().toString());

        try (Reader reader = createFileReader(fileName + XML_SCHEMA_FILE_EXT)) {
            schemaCollection.read(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return schemaCollection;
    }

    public XmlSchema createXmlSchema(final String fileName) {
        final XmlSchemaCollection inputXmlSchemaCollection = new XmlSchemaCollection();
        inputXmlSchemaCollection.setBaseUri(getRootDir().toAbsolutePath().toString());
        try (Reader reader = createFileReader(fileName + XML_SCHEMA_FILE_EXT)) {
            return inputXmlSchemaCollection.read(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
