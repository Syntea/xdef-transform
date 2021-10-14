package test.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.util.XValidate;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import test.resource.TransformInputResourceUtil;
import test.resource.TransformOutputResourceUtil;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test.resource.ResourceConst.XDEFINITION_FILE_EXT;
import static test.validator.ValidatorSetting.LOG_OUTPUT_ENABLED;
import static test.validator.ValidatorSetting.VALIDATE_XML_AGAINST_REF_FILE;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XDefValidator {

    private static final Logger LOG = LoggerFactory.getLogger(XDefValidator.class);

    private final TransformInputResourceUtil inputResourceUtil;
    private final TransformOutputResourceUtil outputResourceUtil;

    public XDefValidator(TransformInputResourceUtil inputResourceUtil, TransformOutputResourceUtil outputResourceUtil) {
        this.inputResourceUtil = inputResourceUtil;
        this.outputResourceUtil = outputResourceUtil;
    }

    public void validateXmlAgainstOutputXDef(final String xDefFileName,
                                              @Nullable final List<String> validTestingData,
                                              @Nullable final List<String> invalidTestingData,
                                              boolean validateRef) {
        LOG.debug("Validating XML data against output X-Definition. " +
                        "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}, validateRef={}",
                xDefFileName,
                Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                Optional.ofNullable(invalidTestingData).map(List::size).orElse(0),
                validateRef);

        // Validate valid XML file against X-Definition
        File xDefFile = outputResourceUtil.getXDefFile(xDefFileName);
        File refXDefFile = validateRef ? inputResourceUtil.getXDefFile(xDefFileName) : null;

        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    ArrayReporter reporter = new ArrayReporter();
                    XDDocument xdDocument = XValidate.validate(
                            null,
                            xmlDataFile,
                            new File[]{refXDefFile},
                            xDefFileName,
                            reporter);

                    if (reporter.errors()) {
                        reporter.printReports(System.err);
                    }

                    assertFalse(
                            reporter.errors(),
                            StringFormatter.format("Error occurs while validating reference X-Definition (positive scenario). " +
                                            "xDefFile='{}', testingFile='{}'",
                                    refXDefFile.getAbsolutePath(),
                                    xmlDataFile.getAbsolutePath())
                    );
                    assertTrue(
                            xdDocument != null,
                            StringFormatter.format("XML is not valid against reference X-Definition (positive scenario). " +
                                            "xDefFile='{}', testingFile='{}'",
                                    refXDefFile.getAbsolutePath(),
                                    xmlDataFile.getAbsolutePath())
                    );
                }

                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(
                        null,
                        xmlDataFile,
                        new File[]{xDefFile},
                        xDefFileName,
                        reporter);

                if (reporter.errors()) {
                    reporter.printReports(System.err);
                }

                assertFalse(
                        reporter.errors(),
                        StringFormatter.format("Error occurs while validating output X-Definition (positive scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefFile.getAbsolutePath(),
                                xmlDataFile.getAbsolutePath())
                );
                assertTrue(
                        xdDocument != null,
                        StringFormatter.format("XML is not valid against output X-Definition (positive scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefFile.getAbsolutePath(),
                                xmlDataFile.getAbsolutePath())
                );
            }
        }

        // Validate invalid XML file against X-Definition
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    ArrayReporter reporter = new ArrayReporter();
                    XValidate.validate(
                            null,
                            xmlDataFile,
                            new File[]{refXDefFile},
                            xDefFileName,
                            reporter);

                    assertTrue(
                            reporter.errors(),
                            StringFormatter.format("No error occurs while validating X-Definition reference (negative scenario). " +
                                            "xDefFile='{}', testingFile='{}'",
                                    refXDefFile.getAbsolutePath(),
                                    xmlDataFile.getAbsolutePath())
                    );
                }

                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(
                        null,
                        xmlDataFile,
                        new File[]{xDefFile},
                        xDefFileName,
                        reporter);

                assertTrue(
                        reporter.errors(),
                        StringFormatter.format("No error occurs while validating X-Definition output (negative scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefFile.getAbsolutePath(),
                                xmlDataFile.getAbsolutePath())
                );
            }
        }
    }

    public void validateXmlAgainstInputXDef(final String xDefFileName,
                                            @Nullable final List<String> validTestingData,
                                            @Nullable final List<String> invalidTestingData) {
        LOG.debug("Validating XML data against input X-Definition. " +
                "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}",
                xDefFileName,
                Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                Optional.ofNullable(invalidTestingData).map(List::size).orElse(0));

        final Properties props = new Properties();
        // Do not check deprecated
        props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);

        final String xDefInputDir = inputResourceUtil.getRootDir().toAbsolutePath().toString();

        final List<String> xDefWildcardNames = new LinkedList<>();
        xDefWildcardNames.add(xDefInputDir + '/' + "*.xdef");
        xDefWildcardNames.add(Paths.get("src/test/resources/xd2schema/input/DefaultExternalMockedMethods.xdef").toAbsolutePath().toString());

        // Load X-Definition files
        File[] xDefFiles = SUtils.getFileGroup(xDefWildcardNames.toArray(new String[xDefWildcardNames.size()]));

        // Validate valid XML file against X-Definition
        if (validTestingData != null) {
            for (String testingDataFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);

                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(
                        props,
                        xmlDataFile,
                        xDefFiles,
                        xDefFileName,
                        reporter);

                if (reporter.errors()) {
                    reporter.printReports(System.err);
                }

                assertFalse(
                        reporter.errors(),
                        StringFormatter.format("Error occurs while validating input X-Definition (positive scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefInputDir, xmlDataFile.getAbsolutePath())
                );
                assertTrue(
                        xdDocument != null,
                        StringFormatter.format("XML is not valid against input X-Definition (positive scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefInputDir, xmlDataFile.getAbsolutePath())
                );
            }
        }

        // Validate invalid XML file against X-Definition
        if (invalidTestingData != null) {
            for (String testingDataFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);

                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(props, xmlDataFile, xDefFiles, xDefFileName, reporter);

                assertTrue(
                        reporter.errors(),
                        StringFormatter.format("No error occurs while validating input X-Definition (negative scenario). " +
                                        "xDefFile='{}', testingFile='{}'",
                                xDefInputDir, xmlDataFile.getAbsolutePath())
                );
            }
        }
    }

    public void compareXDefinitions(final String refXDefFileName, final String outputXDefContent) throws IOException {
        LOG.debug("Comparing X-Definitions. xDefFileName='{}'", refXDefFileName);

        if (LOG_OUTPUT_ENABLED == true) {
            LOG.info(outputXDefContent);
        }

        File refXDefFile = inputResourceUtil.getXDefFile(refXDefFileName);
        String refXDefFileContent = readFile(refXDefFile);

        Diff diff = DiffBuilder.compare(refXDefFileContent)
                .withTest(outputXDefContent)
                .ignoreComments()
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        boolean mismatch = diff.hasDifferences();
        assertFalse(mismatch, "Output X-Definition is different to reference X-Definition. Test=" + refXDefFileName);

        // Reference X-Definition
        {
            String outFileName = refXDefFileName;
            if (mismatch) {
                outFileName += "_err";
            }

            outFileName += "_ref.xdef";
            try (Writer writer = outputResourceUtil.createFileWriter(outFileName)) {
                writer.write(refXDefFileContent);
            }
        }

        // Output X-Definition
        {
            String outFileName = refXDefFileName;
            if (mismatch) {
                outFileName += "_err";
            }

            outFileName += XDEFINITION_FILE_EXT;
            try (Writer writer = outputResourceUtil.createFileWriter(outFileName)) {
                writer.write(outputXDefContent);
            }
        }
    }

    private String readFile(File f) throws IOException {
        try (Reader reader =  outputResourceUtil.createFileReader(f)) {
            return readFile(reader);
        }
    }

    private static String readFile(Reader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                result.append(line + "\n");
            }
        }

        return result.toString();
    }


}
