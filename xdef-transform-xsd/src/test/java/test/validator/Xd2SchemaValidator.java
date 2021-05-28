package test.validator;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.sys.ArrayReporter;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.util.XValidate;
import test.resource.TransformInputResourceUtil;
import test.resource.TransformOutputResourceUtil;
import test.xdutils.XmlValidator;

import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_URI;

/**
 * @author smid
 * @since 2021-05-28
 */
public class Xd2SchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(Xd2SchemaValidator.class);

    static protected boolean LOG_OUTPUT_ENABLED = false;
    static protected boolean WRITE_OUTPUT_INTO_FILE = true;
    static protected boolean VALIDATE_XML_AGAINST_REF_FILE = true;
    static protected boolean PRINT_XML_VALIDATION_ERRORS = true;

    private final TransformInputResourceUtil inputResourceUtil;
    private final TransformOutputResourceUtil outputResourceUtil;

    public Xd2SchemaValidator(TransformInputResourceUtil inputResourceUtil, TransformOutputResourceUtil outputResourceUtil) {
        this.inputResourceUtil = inputResourceUtil;
        this.outputResourceUtil = outputResourceUtil;
    }

    public void compareXmlSchemas(final String xDefFileName,
                                  final XmlSchemaCollection outputSchemaCollection,
                                  final Set<String> schemaNames,
                                  int schemaCount) throws UnsupportedEncodingException {
        final XmlSchemaCollection refSchemaCollection = inputResourceUtil.createRefXmlSchemaCollection(xDefFileName);

        LOG.debug("Comparing XML schemas. xDefFileName='{}'", xDefFileName);

        XmlSchema[] refSchemasAll = refSchemaCollection.getXmlSchemas();
        XmlSchema[] outputSchemasAll = outputSchemaCollection.getXmlSchemas();

        // TODO: Fix multiple XML Schema validation - How to properly filter circle loaded XML Schemas
        int realRefSchemas = 0;
        boolean xsdRootImported = false;
        for (XmlSchema refSchema : refSchemasAll) {
            if (refSchema.getSourceURI() != null) {
                realRefSchemas++;
            } else if (!XML_SCHEMA_DEFAULT_NAMESPACE_URI.equals(refSchema.getLogicalTargetNamespace()) && !xsdRootImported) {
                realRefSchemas++;
                xsdRootImported = true;
            }
        }

        //assertEquals(realRefSchemas + 1, schemaCount + 1, "Invalid number of reference schemas, fileName: " + fileName);
        assertEquals(outputSchemasAll.length, schemaCount + 1, "Invalid number of output schemas, fileName: " + xDefFileName);
        //assertEquals(realRefSchemas + 1, outputSchemasAll.length, "Expected same number of reference and output schemas, fileName: " + fileName);

        if (LOG_OUTPUT_ENABLED == true) {
            for (XmlSchema outputSchema : outputSchemasAll) {
                if (!XML_SCHEMA_DEFAULT_NAMESPACE_URI.equals(outputSchema.getLogicalTargetNamespace())) {
                    outputSchema.write(System.out);
                }
            }
        }

        boolean xsdRootChecked = false;

        for (String schemaName : schemaNames) {
            String refSourceName = ("file:/" + inputResourceUtil.resolve(schemaName + ".xsd").toAbsolutePath())
                    .replace('\\', '/');
            XmlSchema[] refSchemas = refSchemaCollection.getXmlSchema(refSourceName);
            if (refSchemas.length == 0 && xsdRootChecked == false) {
                refSchemas = refSchemaCollection.getXmlSchema(null);
                xsdRootChecked = true;
            }
            XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);

            assertEquals(1, outputSchemas.length, "Multiple schemas of same system name: " + schemaName);
            assertEquals(outputSchemas.length, refSchemas.length, "Unexpected number of matched schemas name: " + schemaName);

            boolean mismatch = false;
            if (refSchemas.length > 0 && outputSchemas.length > 0) {
                ByteArrayOutputStream refOutputStream = new ByteArrayOutputStream();
                refSchemas[0].write(refOutputStream);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputSchemas[0].write(outputStream);

                mismatch = !refOutputStream.toString().equals(outputStream.toString());
                assertFalse(mismatch, "Same schema by sourceId, but different content. Schema=" + schemaName);
            }

            if (WRITE_OUTPUT_INTO_FILE == true) {
                try {
                    // Reference XSD
                    for (int i = 0; i < refSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (mismatch || refSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (refSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += "_ref";
                        outFileName += ".xsd";

                        try (Writer writer = outputResourceUtil.createFileWriter(outFileName)) {
                            refSchemas[i].write(writer);
                        }
                    }

                    // Output XSD
                    for (int i = 0; i < outputSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (mismatch || outputSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (outputSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += ".xsd";

                        try (Writer writer = outputResourceUtil.createFileWriter(outFileName)) {
                            outputSchemas[i].write(writer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < refSchemasAll.length; i++) {
            String sourceUri = refSchemasAll[i].getSourceURI();
            if (sourceUri != null) {
                File tmpFile = new File(sourceUri);
                String refFileName = tmpFile.getName().replaceFirst("[.][^.]+$", "");
                if (refFileName != null && !schemaNames.contains(refFileName)) {
                    try {
                        try (Writer refWriter = outputResourceUtil.createFileWriter(xDefFileName + "_unk_ref_" + i + ".xsd")) {
                            refSchemasAll[i].write(refWriter);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void validateXmlAgainstXDef(final String xDefFileName,
                                       @Nullable final List<String> validTestingData,
                                       @Nullable final List<String> invalidTestingData) {
        LOG.debug("Validating XML data against X-Definition. " +
                "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}",
                xDefFileName,
                Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                Optional.ofNullable(invalidTestingData).map(List::size).orElse(0));

        final Properties props = new Properties();
        // Do not check deprecated
        props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
//        props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT, XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);

        // Validate valid XML file against XML Schema
        if (validTestingData != null) {
            for (String testingDataFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);
                File xDefFile = inputResourceUtil.getXDefFile(xDefFileName);
                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(props, xmlDataFile, (File[]) Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(xdDocument != null, "XML is not valid against X-Definition. Test=" + xDefFileName + ", File=" + testingDataFile);
                if (reporter.errors()) {
                    reporter.printReports(System.err);
                }
                assertFalse(reporter.errors(), "Error occurs on X-Definition validation. Test=" + xDefFileName + ", File=" + testingDataFile);
            }
        }

        // Validate invalid XML file against XML Schema
        if (invalidTestingData != null) {
            for (String testingDataFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingDataFile);
                File xDefFile = inputResourceUtil.getXDefFile(xDefFileName);
                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(props, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), xDefFileName, reporter);
                assertTrue(reporter.errors(), "Error does not occurs on X-Definition validation (but it should). Test=" + xDefFileName + ", File=" + testingDataFile);
            }
        }
    }

    public void validateXmlAgainstXmlSchemas(final String xDefFileName,
                                             final List<String> validTestingData,
                                             final List<String> invalidTestingData,
                                             final boolean validateRef,
                                             final boolean expectedInvalidXmlSchema) {
        File outputXsdFile = outputResourceUtil.getOutputSchemaFile(xDefFileName);
        File refXsdFile = validateRef ? inputResourceUtil.getRefSchemaFile(xDefFileName) : null;

        if (validateRef) {
            LOG.debug("Validating XML data against output XML Schema and reference XML Schema. " +
                            "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}",
                    xDefFileName,
                    Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                    Optional.ofNullable(invalidTestingData).map(List::size).orElse(0));
        } else {
            LOG.debug("Validating XML data against output XML Schema. " +
                            "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}",
                    xDefFileName,
                    Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                    Optional.ofNullable(invalidTestingData).map(List::size).orElse(0));
        }


        // Validate valid XML file against XML Schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);

                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    validateXmlAgainstXmlSchemas(xDefFileName, xmlDataFile, refXsdFile, true, "ref");
                }

                if (outputXsdFile != null) {
                    validateXmlAgainstXmlSchemas(xDefFileName, xmlDataFile, outputXsdFile, !expectedInvalidXmlSchema, "out");
                }
            }
        }

        // Validate invalid XML file against XML Schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    validateXmlAgainstXmlSchemas(xDefFileName, xmlDataFile, refXsdFile, false, "ref");
                }
                if (outputXsdFile != null) {
                    validateXmlAgainstXmlSchemas(xDefFileName, xmlDataFile, outputXsdFile, false, "out");
                }
            }
        }
    }

    public void validateXmlAgainstXmlSchemas(final String xDefFileName,
                                             final File xmlFile,
                                             final File xsdSchemaFile,
                                             boolean expectedResult,
                                             String type) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(xsdSchemaFile));
        assertEquals(
                expectedResult,
                validator.validate(expectedResult && PRINT_XML_VALIDATION_ERRORS),
                StringFormatter.format("XML validation against XML schema failed. " +
                        "xDefFileName='{}', type='{}', xmlFileName='{}'",
                        xDefFileName, type, xmlFile.getName()));
    }

}
