package test.xd2schema;

import org.apache.commons.io.FileUtils;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.transform.xsd.xd2schema.adapter.impl.XdPool2XsdAdapter;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import test.resource.TransformInputResourceUtil;
import test.resource.TransformOutputResourceUtil;
import test.validator.XDefValidator;
import test.validator.XmlSchemaValidator;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;

/**
 * @author smid
 * @since 2021-05-27
 */
public abstract class AbstractXd2SchemaTransformSuite {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected TransformInputResourceUtil inputResourceUtil;
    protected TransformOutputResourceUtil outputResourceUtil;

    protected ReportWriter reportWriter;

    private XDefValidator xDefValidator;
    private XmlSchemaValidator xmlSchemaValidator;

    @BeforeEach
    public void init() {
        inputResourceUtil = new TransformInputResourceUtil("xd2schema/input");
        outputResourceUtil = new TransformOutputResourceUtil("xd2schema/output");

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

    protected XdPool2XsdAdapter createXdPoolAdapter(@Nullable final Set<Xd2XsdFeature> additionalFeatures) {
        final XdPool2XsdAdapter adapter = new XdPool2XsdAdapter();
        final Set<Xd2XsdFeature> features = Xd2XsdUtils.defaultFeatures();

        features.add(Xd2XsdFeature.XSD_ANNOTATION);
        features.add(Xd2XsdFeature.XSD_NAME_COLLISION_DETECTOR);
        features.add(Xd2XsdFeature.POSTPROCESSING_UNIQUE);

        if (additionalFeatures != null) {
            features.addAll(additionalFeatures);
        }

        adapter.setFeatures(features);
        adapter.setReportWriter(reportWriter);

        return adapter;
    }

    protected void transformXdSchemaNoSupport(final String xDefFileName, List<String> validTestingData, List<String> invalidTestingData, String exMsg) {
        transformXd2Schema(xDefFileName, validTestingData, invalidTestingData, false, exMsg, false, null);
    }

    protected void transformXd2SchemaInvalidXsd(final String xDefFileName, List<String> validTestingData, List<String> invalidTestingData) {
        transformXd2Schema(xDefFileName, validTestingData, invalidTestingData, false, null, true, null);
    }

    protected void transformXd2SchemaNoRef(final String xDefFileName, List<String> validTestingData, List<String> invalidTestingData) {
        transformXd2Schema(xDefFileName, validTestingData, invalidTestingData, false, null, false, null);
    }

    protected void transformXd2SchemaWithFeatures(final String xDefFileName, List<String> validTestingData, List<String> invalidTestingData, Set<Xd2XsdFeature> features) {
        transformXd2Schema(xDefFileName, validTestingData, invalidTestingData, false, null, false, features);
    }

    protected void transformXd2Schema(final String xDefFileName, List<String> validTestingData, List<String> invalidTestingData) {
        transformXd2Schema(xDefFileName, validTestingData, invalidTestingData, true, null, false, null);
    }

    protected void transformXd2Schema(final String xDefFileName,
                                      @Nullable final List<String> testingDataFileNamesValid,
                                      @Nullable final List<String> testingDataFileNamesInvalid,
                                      boolean doValidationAgainstRefXmlSchema,
                                      @Nullable String expectedExceptionMsg,
                                      boolean expectedInvalidXmlSchema,
                                      @Nullable final Set<Xd2XsdFeature> transformFeatures) {
        LOG.debug(HEADER_LINE);
        LOG.debug("transformXd2Schema: " +
                        "xDefFileName='{}', validDataCount={}, invalidDataCount={}, " +
                        "doValidationAgainstRefXmlSchema={}, expectedInvalidXmlSchema={}, transformFeatures={}\n" +
                        "inputDir={}\n" +
                        "outputDir={}",
                xDefFileName,
                Optional.ofNullable(testingDataFileNamesValid).map(List::size).orElse(0),
                Optional.ofNullable(testingDataFileNamesInvalid).map(List::size).orElse(0),
                doValidationAgainstRefXmlSchema, expectedInvalidXmlSchema, transformFeatures,
                inputResourceUtil.getRootDir().toAbsolutePath(),
                outputResourceUtil.getRootDir().toAbsolutePath());

        try {
            final XdPool2XsdAdapter adapter = createXdPoolAdapter(transformFeatures);
            final String fileGroupRegex = inputResourceUtil.getRootDir().toAbsolutePath().toString() + '/' + "*.xdef";

            // Load X-Definition files
            File[] defFiles = SUtils.getFileGroup(fileGroupRegex);
            final Properties props = new Properties();
            // Do not check deprecated
            props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
            props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT, XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);

            final XDBuilder xb = XDFactory.getXDBuilder(reportWriter, props);
            xb.setSource(defFiles);
            final XDPool inputXD = xb.compileXD();

            // Transform X-Definition -> XML Schema
            LOG.debug("Transforming ...");
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);
            int expectedSchemaCount = inputXD.getXMDefinitions().length;

            // Compare output XML Schemas to XML Schema references
            if (doValidationAgainstRefXmlSchema) {
                xmlSchemaValidator.compareXmlSchemas(
                        xDefFileName,
                        outputXmlSchemaCollection,
                        adapter.getSchemaNames(),
                        expectedSchemaCount
                );
            } else {
                outputResourceUtil.writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
            }

            xDefValidator.validateXmlAgainstInputXDef(xDefFileName, testingDataFileNamesValid, testingDataFileNamesInvalid);

            // Validate XML files against output XML Schemas and reference XML Schemas
            if ((testingDataFileNamesValid != null && !testingDataFileNamesValid.isEmpty()) || (testingDataFileNamesInvalid != null && !testingDataFileNamesInvalid.isEmpty())) {
                xmlSchemaValidator.validateXmlAgainstOutputXmlSchema(xDefFileName, testingDataFileNamesValid, testingDataFileNamesInvalid, doValidationAgainstRefXmlSchema, expectedInvalidXmlSchema);
            }
        } catch (Exception ex) {
            if (expectedExceptionMsg != null) {
                assertEquals(expectedExceptionMsg, ex.getMessage());
            } else {
                fail(ex);
            }
        }

        if (expectedExceptionMsg != null) {
            fail("Test should failed with message: " + expectedExceptionMsg);
        }
    }

}
