package org.xdef.transform.xsd.console.impl;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.transform.xsd.console.XDefAdapter;
import org.xdef.transform.xsd.error.FormattedRuntimeException;
import org.xdef.transform.xsd.error.UnexpectedValidationResultException;
import org.xdef.transform.xsd.util.XmlValidator;
import org.xdef.transform.xsd.xd2schema.adapter.impl.XdPool2XsdAdapter;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;

public class DefaultXDefAdapter implements XDefAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommandLineProcessor.class);

    private final XDefAdapterConfig config;

    public DefaultXDefAdapter(XDefAdapterConfig config) {
        this.config = config;
    }

    @Override
    public XDefTransformResult transform() {
        LOG.info("Preparing XDef2XmlSchema transformation ... ");

        final StopWatch watch = StopWatch.createStarted();
        final ReportWriter reportWriter = new ArrayReporter();

        XDPool inputXD;

        try {
            final File[] defFiles = getInputXDefinitionFiles();

            LOG.info("Compiling XDPool from given X-definition. source: {}", Arrays.stream(defFiles).collect(Collectors.toList()));

            final XDBuilder xb = XDFactory.getXDBuilder(reportWriter, null);
            xb.setSource(defFiles);

            inputXD = xb.compileXD();

            LOG.info("Compilation of XDPool done, elapsed {} ms", watch.getTime());
        } catch (Exception ex) {
            throw new FormattedRuntimeException(ex, "Error occurs while compile input X-Definition(s), elapsed {} ms",
                    watch.getTime());
        } finally {
            watch.stop();
        }

        if (reportWriter.errors()) {
            try (PrintStream printStream = org.usefultoys.slf4j.LoggerFactory.getErrorPrintStream(LOG)) {
                printStream.println("");
                reportWriter.getReportReader().printReports(printStream);
            }

            throw new FormattedRuntimeException("Error occurs while compile input X-Definition(s), elapsed {} ms",
                    watch.getTime());
        }

        if (inputXD == null) {
            throw new FormattedRuntimeException("Error occurs while compile input X-Definition(s), elapsed {} ms",
                    watch.getTime());
        }

        return transform(inputXD);
    }

    @Override
    public XDefTransformResult transform(XDPool xdPool) {
        LOG.info("Transforming XDPool to XML Schema ... ");
        StopWatch watch = StopWatch.createStarted();

        final XDefTransformResult transformResult = new XDefTransformResult();
        final Optional<Pair<String, Path>> outputRootSchema;

        try {
            final XdPool2XsdAdapter adapter = createXdPoolAdapter();
            final ReportWriter repWriter = new ArrayReporter();
            adapter.setReportWriter(repWriter);

            // Convert XD -> XML Schema
            final XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(xdPool);
            outputRootSchema = writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames(), transformResult);

            transformResult.setXmlSchemaCollection(outputXmlSchemaCollection);

            LOG.info("Transformation of XDPool done, elapsed {} ms", watch.getTime());
        } catch (Exception ex) {
            throw new FormattedRuntimeException(ex, "Error occurs while transforming XDPool, elapsed {} ms",
                    watch.getTime());
        } finally {
            watch.stop();
        }

        if (config.hasTestingData()) {
            outputRootSchema.orElseThrow(() ->
                    new RuntimeException("Validation is required but no output XML Schema is set"));

            validateXmlData(outputRootSchema.get().getRight());
        }

        return transformResult;
    }

    private File[] getInputXDefinitionFiles() {
        String directory = config.getInputDirectory();
        int dirIndex = config.getInputDirectory().lastIndexOf('\\');
        if (dirIndex > 0 && dirIndex < config.getInputDirectory().length() - 1) {
            directory += '\\';
        } else if (dirIndex == -1) {
            dirIndex = config.getInputDirectory().lastIndexOf('/');
            if (dirIndex > 0 && dirIndex < config.getInputDirectory().length() - 1) {
                directory += '/';
            }
        }

        LOG.debug("Directory: " + (directory + "*.(x)def"));

        return SUtils.getFileGroup(
                directory + "*.xdef",
                directory + "*.def"
        );
    }

    private XdPool2XsdAdapter createXdPoolAdapter() {
        final XdPool2XsdAdapter adapter = new XdPool2XsdAdapter();
        final Set<Xd2XsdFeature> features = config.useDefaultFeatures()
                ? Xd2XsdUtils.defaultFeatures()
                : new HashSet<>();

        if (config.getFeatures() != null) {
            features.addAll(config.getFeatures());
        }

        LOG.debug("All of the following features will be used during the transformation: {}", features);

        adapter.setFeatures(features);
        return adapter;
    }

    private Optional<Pair<String, Path>> writeOutputSchemas(
            final XmlSchemaCollection outputSchemaCollection,
            final Set<String> schemaNames,
            final XDefTransformResult transformResult) {

        final File outputDir = new File(config.getOutputDirectory());
        Pair<String, Path> outputRootSchema = null;

        for (String schemaName : schemaNames) {
            final XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);
            try {
                // Output XML Schema
                for (int i = 0; i < outputSchemas.length; i++) {
                    String outFileName = schemaName;

                    if (config.getOutputFilePrefix() != null && !config.getOutputFilePrefix().isEmpty()) {
                        outFileName = config.getOutputFilePrefix() + outFileName;
                    }

                    if (outputSchemas.length > 1) {
                        outFileName += "_" + i;
                    }

                    if (config.getOutputFileExt() != null && !config.getOutputFileExt().isEmpty()) {
                        outFileName += config.getOutputFileExt();
                    }

                    outFileName = outputDir.getAbsolutePath() + "\\" + outFileName;
                    if (config.getInputRoot().isPresent() && schemaName.equals(config.getInputRoot().get())) {
                        outputRootSchema = Pair.of(config.getInputRoot().get(), Paths.get(outFileName));
                    }

                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(outFileName),
                                    StandardCharsets.UTF_8))) {
                        outputSchemas[i].write(writer);
                    }

                    transformResult.addOutputSchema(schemaName, Paths.get(outFileName));
                }
            } catch (IOException ex) {
                throw new FormattedRuntimeException(ex, "Error occurs while creating XML Schema file." +
                        " xmlSchemaName='{}'", schemaName);
            }
        }

        return Optional.ofNullable(outputRootSchema);
    }

    private void validateXmlData(final Path outputRootSchema) {
        LOG.info(HEADER_LINE);
        LOG.info("Validating testing data against output XML Schema ... ");
        LOG.info(HEADER_LINE);

        final StopWatch watch = StopWatch.createStarted();

        final StreamSource outputRootSchemaFile = new StreamSource(outputRootSchema.toFile());

        if (config.hasPositiveTestingData()) {
            for (String testingFile : config.getTestingDataPos()) {
                validateXmlAgainstXsd(new File(testingFile), outputRootSchemaFile, true);
            }
        }

        if (config.hasNegativeTestingData()) {
            for (String testingFile : config.getTestingDataNeg()) {
                validateXmlAgainstXsd(new File(testingFile), outputRootSchemaFile, false);
            }
        }

        watch.stop();
        LOG.info("Testing data validation against output XML Schema done, elapsed {} ms.", watch.getTime());
    }

    private void validateXmlAgainstXsd(final File xmlDataFile,
                                       final StreamSource outputRootSchema,
                                       boolean expectedResult) {
        final XmlValidator validator = new XmlValidator(new StreamSource(xmlDataFile), outputRootSchema);
        LOG.info("XML data {} validation against XML schema. fileName='{}'",
                expectedResult ? "positive" : "negative", xmlDataFile.getAbsolutePath());

        try {
            validator.validate();
        } catch (Exception ex) {
            if (expectedResult) {
                throw new UnexpectedValidationResultException(ex, "ExpectedResult={}, currResult={}", expectedResult, false);
            }

            throw ex;
        }

        if (!expectedResult) {
            throw new UnexpectedValidationResultException("ExpectedResult={}, currResult={}", expectedResult, true);
        }
    }

}
