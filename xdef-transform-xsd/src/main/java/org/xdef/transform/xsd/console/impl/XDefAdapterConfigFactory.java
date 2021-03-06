package org.xdef.transform.xsd.console.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.console.XDefToSchemaOptionsConst;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author smid
 * @since 2021-05-13
 */
public class XDefAdapterConfigFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XDefAdapterConfigFactory.class);

    public XDefAdapterConfig create(final CommandLine cmd) {
        LOG.debug("Creating XDef2Xsd adapter configuration ...");

        final XDefAdapterConfig config = new XDefAdapterConfig();

        config.setInputDirectory(cmd.getOptionValue(XDefToSchemaOptionsConst.INPUT_DIR));
        config.setOutputDirectory(cmd.getOptionValue(XDefToSchemaOptionsConst.OUTPUT_DIR));
        config.setInputRoot(cmd.getOptionValue(XDefToSchemaOptionsConst.INPUT_ROOT));
        if (cmd.hasOption(XDefToSchemaOptionsConst.OUTPUT_FILE_PREFIX)) {
            config.setOutputFilePrefix(cmd.getOptionValue(XDefToSchemaOptionsConst.OUTPUT_FILE_PREFIX));
        }

        if (cmd.hasOption(XDefToSchemaOptionsConst.OUTPUT_EXT)) {
            config.setOutputFileExt(cmd.getOptionValue(XDefToSchemaOptionsConst.OUTPUT_EXT));
        }

        if (cmd.hasOption(XDefToSchemaOptionsConst.NO_DEFAULT_FEATURES)) {
            config.setUseDefaultFeatures(false);
        }

        if (cmd.hasOption(XDefToSchemaOptionsConst.FEATURES)) {
            config.setFeatures(parseFeatures(cmd.getOptionValue(XDefToSchemaOptionsConst.FEATURES)));
        }

        if (cmd.hasOption(XDefToSchemaOptionsConst.VALIDATE_POSITIVE_CASE)) {
            config.setTestingDataPos(Arrays.asList(cmd.getOptionValues(XDefToSchemaOptionsConst.VALIDATE_POSITIVE_CASE)));
        }

        if (cmd.hasOption(XDefToSchemaOptionsConst.VALIDATE_NEGATIVE_CASE)) {
            config.setTestingDataNeg(Arrays.asList(cmd.getOptionValues(XDefToSchemaOptionsConst.VALIDATE_NEGATIVE_CASE)));
        }

        return config;
    }

    private EnumSet<Xd2XsdFeature> parseFeatures(String featuresPlain) {
        LOG.debug("Parsing features ...");

        if (featuresPlain == null || StringUtils.isBlank(featuresPlain)) {
            return null;
        }

        CommandLineParser featureParser = new DefaultParser();
        CommandLine cmd;

        final Set<String> featurePlainSet = Arrays.stream(featuresPlain.split(",|\\s")).map(feature -> {
            if (feature.length() < 5) {
                return "-" + feature;
            } else {
                return "--" + feature;
            }
        }).collect(Collectors.toSet());

        try {
            cmd = featureParser.parse(XDefToSchemaOptions.features(), featurePlainSet.stream().toArray(String[]::new), true);
        } catch (ParseException ex) {
            throw new RuntimeException("Error occurs while parsing input commands", ex);
        }

        final Set<Xd2XsdFeature> featureSet = new HashSet<>();

        if (cmd != null) {
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_ANNOTATION)) { featureSet.add(Xd2XsdFeature.XSD_ANNOTATION); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_DECIMAL_ANY_SEPARATOR)) { featureSet.add(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_ALL_UNBOUNDED)) { featureSet.add(Xd2XsdFeature.XSD_ALL_UNBOUNDED); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_NAME_COLLISION_DETECTOR)) { featureSet.add(Xd2XsdFeature.XSD_NAME_COLLISION_DETECTOR); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_SKIP_DELETE_TOPLEVEL_ELEMENTS)) { featureSet.add(Xd2XsdFeature.XSD_SKIP_DELETE_TOPLEVEL_ELEMENTS); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_ALL_ELEMENTS_TOPLEVEL)) { featureSet.add(Xd2XsdFeature.XSD_ALL_ELEMENTS_TOPLEVEL); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_XSD_ELEMENT_NO_SIMPLE_TYPE)) { featureSet.add(Xd2XsdFeature.XSD_ELEMENT_NO_SIMPLE_TYPE); }

            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_EXTRA_SCHEMAS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_REFS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_REFS); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_QNAMES)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_QNAMES); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_ALL_TO_CHOICE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_MIXED)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_MIXED); }
            if (cmd.hasOption(XDefToSchemaOptionsConst.F_POSTPROCESSING_UNIQUE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_UNIQUE); }
        }

        LOG.debug("Command line enabled features: {}", featureSet);

        if (featureSet.isEmpty()) {
            return null;
        }

        return EnumSet.copyOf(featureSet);
    }

}
