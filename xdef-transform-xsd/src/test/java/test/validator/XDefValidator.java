package test.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.sys.ArrayReporter;
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
import java.util.Arrays;
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
                    XDDocument xdDocument = XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(refXDefFile).toArray(), xDefFileName, reporter);
                    assertTrue(xdDocument != null, "XML is not valid against X-Definition. Test=" + xDefFileName + ", File=" + testingFile);
                    assertFalse(reporter.errors(), "Error occurs on X-Definition validation. Test=" + xDefFileName + ", File=" + testingFile);
                }

                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(xdDocument != null, "XML is not valid against X-Definition. Test=" + xDefFileName + ", File=" + testingFile);
                assertFalse(reporter.errors(), "Error occurs on X-Definition validation. Test=" + xDefFileName + ", File=" + testingFile);
            }
        }

        // Validate invalid XML file against X-Definition
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    ArrayReporter reporter = new ArrayReporter();
                    XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(refXDefFile).toArray(), xDefFileName, reporter);
                    assertTrue(reporter.errors(), "Error does not occurs on X-Definition validation (but it should). Test=" + xDefFileName + ", File=" + testingFile);
                }

                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(reporter.errors(), "Error does not occurs on X-Definition validation (but it should). Test=" + xDefFileName + ", File=" + testingFile);
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
//        props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT, XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);

        File xDefFile = inputResourceUtil.getXDefFile(xDefFileName);

        // Validate valid XML file against X-Definition
        if (validTestingData != null) {
            for (String testingDataFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);

                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(props, xmlDataFile, (File[]) Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(xdDocument != null, "XML is not valid against X-Definition. Test=" + xDefFileName + ", File=" + testingDataFile);
                if (reporter.errors()) {
                    reporter.printReports(System.err);
                }
                assertFalse(reporter.errors(), "Error occurs on X-Definition validation. Test=" + xDefFileName + ", File=" + testingDataFile);
            }
        }

        // Validate invalid XML file against X-Definition
        if (invalidTestingData != null) {
            for (String testingDataFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);

                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(props, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(reporter.errors(), "Error does not occurs on X-Definition validation (but it should). Test=" + xDefFileName + ", File=" + testingDataFile);
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