package test.validator;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.util.XmlValidator;
import test.resource.TransformInputResourceUtil;
import test.resource.TransformOutputResourceUtil;

import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_URI;
import static test.resource.ResourceConst.XML_SCHEMA_FILE_EXT;
import static test.validator.ValidatorSetting.LOG_OUTPUT_ENABLED;
import static test.validator.ValidatorSetting.PRINT_XML_VALIDATION_ERRORS;
import static test.validator.ValidatorSetting.VALIDATE_XML_AGAINST_REF_FILE;
import static test.validator.ValidatorSetting.WRITE_OUTPUT_INTO_FILE;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XmlSchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(XmlSchemaValidator.class);

    private final TransformInputResourceUtil inputResourceUtil;
    private final TransformOutputResourceUtil outputResourceUtil;

    public XmlSchemaValidator(TransformInputResourceUtil inputResourceUtil, TransformOutputResourceUtil outputResourceUtil) {
        this.inputResourceUtil = inputResourceUtil;
        this.outputResourceUtil = outputResourceUtil;
    }

    public void compareXmlSchemas(final String schemaFileName,
                                  final XmlSchemaCollection outputSchemaCollection,
                                  final Set<String> schemaNames,
                                  int schemaCount) throws UnsupportedEncodingException {
        final XmlSchemaCollection refSchemaCollection = inputResourceUtil.createXmlSchemaCollection(schemaFileName);

        LOG.debug("Comparing XML schemas. schemaFileName='{}'", schemaFileName);

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
        assertEquals(outputSchemasAll.length, schemaCount + 1, "Invalid number of output schemas, fileName: " + schemaFileName);
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
            String refSourceName = ("file:/" + inputResourceUtil.resolve(schemaName + XML_SCHEMA_FILE_EXT).toAbsolutePath())
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
                        outFileName += XML_SCHEMA_FILE_EXT;

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

                        outFileName += XML_SCHEMA_FILE_EXT;

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
                        try (Writer refWriter = outputResourceUtil.createFileWriter(schemaFileName + "_unk_ref_" + i + XML_SCHEMA_FILE_EXT)) {
                            refSchemasAll[i].write(refWriter);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void validateXmlAgainstInputXmlSchema(final String xDefFileName,
                                                 @Nullable final List<String> validTestingData,
                                                 @Nullable final List<String> invalidTestingData) {
        File inputXsdFile = inputResourceUtil.getSchemaFile(xDefFileName);

        LOG.debug("Validating XML data against reference XML Schema. " +
                        "xDefFileName='{}', validTestingDataCount={}, invalidTestingDataCount={}",
                xDefFileName,
                Optional.ofNullable(validTestingData).map(List::size).orElse(0),
                Optional.ofNullable(invalidTestingData).map(List::size).orElse(0));

        // Validate valid XML file against XML Schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (inputXsdFile != null) {
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, inputXsdFile, true, "input");
                }
            }
        }

        // Validate invalid XML file against XML Schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (inputXsdFile != null) {
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, inputXsdFile, false, "input");
                }
            }
        }
    }

    public void validateXmlAgainstOutputXmlSchema(final String xDefFileName,
                                                  @Nullable final List<String> validTestingData,
                                                  @Nullable final List<String> invalidTestingData,
                                                  final boolean validateRef,
                                                  final boolean expectedInvalidXmlSchema) {
        File outputXsdFile = outputResourceUtil.getSchemaFile(xDefFileName);
        File refXsdFile = validateRef ? inputResourceUtil.getSchemaFile(xDefFileName) : null;

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
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, refXsdFile, true, "reference");
                }

                if (outputXsdFile != null) {
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, outputXsdFile, !expectedInvalidXmlSchema, "output");
                }
            }
        }

        // Validate invalid XML file against XML Schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = inputResourceUtil.getXmlDataFile(testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, refXsdFile, false, "reference");
                }
                if (outputXsdFile != null) {
                    validateXmlAgainstXmlSchema(xDefFileName, xmlDataFile, outputXsdFile, false, "output");
                }
            }
        }
    }

    private static void validateXmlAgainstXmlSchema(final String xDefFileName,
                                                    final File xmlFile,
                                                    final File xsdSchemaFile,
                                                    boolean expectedResult,
                                                    String type) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(xsdSchemaFile));
        boolean validateResult;
        Exception validateEx = null;
        try {
            validateResult = validator.validate();
        } catch (Exception ex) {
            validateResult = false;
            validateEx = ex;
        }

        if (expectedResult != validateResult && validateEx != null && PRINT_XML_VALIDATION_ERRORS) {
            validateEx.printStackTrace();
        }

        assertEquals(
                expectedResult,
                validateResult,
                StringFormatter.format("XML validation against XML schema failed. " +
                        "xDefFileName='{}', type='{}', xmlFileName='{}'",
                        xDefFileName, type, xmlFile.getName()));
    }

}
