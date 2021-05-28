package test.schema2xd;

import org.apache.commons.io.FileUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.schema2xd.adapter.impl.DefaultXsd2XDefAdapter;
import org.xdef.transform.xsd.schema2xd.def.Xsd2XdFeature;
import org.xdef.transform.xsd.schema2xd.util.Xsd2XdUtils;
import test.resource.TransformInputResourceUtil;
import test.resource.TransformOutputResourceUtil;
import test.validator.XDefValidator;
import test.validator.XmlSchemaValidator;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;

/**
 * @author smid
 * @since 2021-05-28
 */
public class AbstractSchema2XdTransformSuite {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected TransformInputResourceUtil inputResourceUtil;
    protected TransformOutputResourceUtil outputResourceUtil;

    protected ReportWriter reportWriter;

    private XDefValidator xDefValidator;
    private XmlSchemaValidator xmlSchemaValidator;

    @BeforeEach
    public void init() {
        inputResourceUtil = new TransformInputResourceUtil("schema2xd/input");
        outputResourceUtil = new TransformOutputResourceUtil("schema2xd/output");

        xDefValidator = new XDefValidator(inputResourceUtil, outputResourceUtil);
        xmlSchemaValidator = new XmlSchemaValidator(inputResourceUtil, outputResourceUtil);

        reportWriter = new ArrayReporter();
    }

    protected void initTestCaseDirs(final String suiteName, final String caseName) {
        try {
            Path caseInputDir = inputResourceUtil.getDirResourcePath(Paths.get(suiteName).resolve(caseName).toString());
            Path caseOutputDir = outputResourceUtil.getResourcePath(Paths.get(suiteName).resolve(caseName).toString());
            if (Files.exists(caseOutputDir)) {
                if (Files.isDirectory(caseOutputDir)) {
                    FileUtils.deleteDirectory(caseOutputDir.toFile());
                } else {
                    Files.delete(caseOutputDir);
                }
            }

            caseOutputDir = Files.createDirectories(caseOutputDir);

            inputResourceUtil.setRootDir(caseInputDir);
            outputResourceUtil.setRootDir(caseOutputDir);
        } catch (Exception ex) {
            throw new RuntimeException("Error occurs while initializing test case directories", ex);
        }
    }

    private DefaultXsd2XDefAdapter createXsdAdapter(Set<Xsd2XdFeature> additionalFeatures) {
        final DefaultXsd2XDefAdapter adapter = new DefaultXsd2XDefAdapter();
        final Set<Xsd2XdFeature> features = Xsd2XdUtils.defaultFeatures();

        if (additionalFeatures != null) {
            features.addAll(additionalFeatures);
        }

        adapter.setFeatures(features);
        adapter.setReportWriter(reportWriter);
        return adapter;
    }

    protected void convertXsd2XDef(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, true, null);
    }

    protected void convertXsd2XDefNoRef(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, false, null);
    }

    protected void convertXsd2XDefWithFeatures(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData, Set<Xsd2XdFeature> features) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, false, features);
    }

    protected void convertXsd2XdPool(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, true, null);
    }

    protected void convertXsd2XdPoolNoRef(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, false, null);
    }

    protected void convertXsd2XdPoolWithFeatures(final String schemaFileName, List<String> validTestingData, List<String> invalidTestingData, Set<Xsd2XdFeature> features) {
        convertXsd2XDef(schemaFileName, validTestingData, invalidTestingData, false, features);
    }

    protected void convertXsd2XDef(final String schemaFileName,
                                 @Nullable final List<String> testingDataFileNamesValid,
                                 @Nullable final List<String> testingDataFileNamesInvalid,
                                 boolean doValidationAgainstRefXDef,
                                 @Nullable final Set<Xsd2XdFeature> transformFeatures) {
        LOG.debug(HEADER_LINE);
        LOG.debug("convertXsd2XDef: " +
                        "schemaFileName='{}', validDataCount={}, invalidDataCount={}, " +
                        "doValidationAgainstRefXDef={}, transformFeatures={}\n" +
                        "inputDir={}\n" +
                        "outputDir={}",
                schemaFileName,
                Optional.ofNullable(testingDataFileNamesValid).map(List::size).orElse(0),
                Optional.ofNullable(testingDataFileNamesInvalid).map(List::size).orElse(0),
                doValidationAgainstRefXDef, transformFeatures,
                inputResourceUtil.getRootDir().toAbsolutePath(),
                outputResourceUtil.getRootDir().toAbsolutePath());

        try {
            DefaultXsd2XDefAdapter adapter = createXsdAdapter(transformFeatures);

            // Convert XML Schema -> XD Schema
            XmlSchema inputXmlSchema = inputResourceUtil.createXmlSchema(schemaFileName);
            String outputXDefContent = adapter.createXDefinition(inputXmlSchema, schemaFileName);

            // Compare output X-Definition to X-Definition reference
            if (doValidationAgainstRefXDef) {
                xDefValidator.compareXDefinitions(schemaFileName, outputXDefContent);
            } else {
                outputResourceUtil.writeOutputXDefinition(schemaFileName, outputXDefContent);
            }

            // Validate XML files against input XML Schema
            xmlSchemaValidator.validateXmlAgainstInputXmlSchema(schemaFileName, testingDataFileNamesValid, testingDataFileNamesInvalid);

            xDefValidator.validateXmlAgainstOutputXDef(schemaFileName, testingDataFileNamesValid, testingDataFileNamesInvalid, doValidationAgainstRefXDef);
        } catch (Exception ex) {
            fail(ex);
        }
    }

}
