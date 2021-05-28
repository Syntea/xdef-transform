package test.resource;

import org.apache.ws.commons.schema.XmlSchemaCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

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

//    public Reader createInputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
//        Path file = getFileResourcePath(Paths.get(fileName).resolve(fileName + fileExt).toString());
//        return createFileReader(file.toFile());
//    }

    // TODO: stejny jako createInputFileReader?
    public Reader createFileReader(final String fileName) throws FileNotFoundException {
        return createFileReader(getFileResourcePath(fileName).toFile());
    }

    public File getXmlDataFile(final String fileName) {
        return getFileResourcePath(
                Paths.get("data")
                        .resolve(fileName + ".xml")
                        .toString()
        ).toFile();
    }

//    public Writer createFileWriter(String file) throws FileNotFoundException {
//        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
//    }

    public File getXDefFile(final String fileName) {
        return getFileResourcePath(fileName + ".xdef").toFile();
    }

    public File getRefSchemaFile(final String fileName) {
        return getFileResourcePath(fileName + ".xsd").toFile();
    }

    public XmlSchemaCollection createRefXmlSchemaCollection(final String fileName) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        schemaCollection.setBaseUri(getRootDir().toAbsolutePath().toString());

        try (Reader reader = createFileReader(fileName + ".xsd")) {
            schemaCollection.read(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return schemaCollection;
    }

}
