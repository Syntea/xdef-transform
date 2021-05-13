package org.xdef.transform.xsd.console.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.console.XDefToXsdOptionsConst;
import org.xdef.transform.xsd.util.SchemaLoggerDefs;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author smid
 * @since 2021-05-13
 */
public class XDefAdapterConfigFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XDefAdapterConfigFactory.class);

    public XDefAdapterConfig create(final CommandLine cmd) {
        LOG.debug("Creating XDef2Xsd adapter configuration ...");

        final XDefAdapterConfig config = new XDefAdapterConfig();

        config.setInputDirectory(cmd.getOptionValue(XDefToXsdOptionsConst.INPUT_DIR));
        config.setOutputDirectory(cmd.getOptionValue(XDefToXsdOptionsConst.OUTPUT_DIR));
        config.setInputRoot(cmd.getOptionValue(XDefToXsdOptionsConst.INPUT_ROOT));
        if (cmd.hasOption(XDefToXsdOptionsConst.OUTPUT_FILE_PREFIX)) {
            config.setOutputFilePrefix(cmd.getOptionValue(XDefToXsdOptionsConst.OUTPUT_FILE_PREFIX));
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.OUTPUT_EXT)) {
            config.setOutputFileExt(cmd.getOptionValue(XDefToXsdOptionsConst.OUTPUT_EXT));
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.NO_DEFAULT_FEATURES)) {
            config.setUseDefaultFeatures(false);
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.FEATURES)) {
            config.setFeatures(parseFeatures(cmd.getOptionValues(XDefToXsdOptionsConst.FEATURES)));
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.VALIDATE_POSITIVE_CASE)) {
            config.setTestingDataPos(Arrays.asList(cmd.getOptionValues(XDefToXsdOptionsConst.VALIDATE_POSITIVE_CASE)));
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.VALIDATE_NEGATIVE_CASE)) {
            config.setTestingDataNeg(Arrays.asList(cmd.getOptionValues(XDefToXsdOptionsConst.VALIDATE_NEGATIVE_CASE)));
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.VERBOSE)) {
            config.setVerbose(getVerboseLevel(cmd.getOptionValue(XDefToXsdOptionsConst.VERBOSE)));
        }

        if (config.getVerbose() >= SchemaLoggerDefs.LOG_INFO) {
            System.out.println("Input configuration");
            System.out.println(config);
        }

        return config;
    }

    private EnumSet<Xd2XsdFeature> parseFeatures(String[] features) {
        Set<Xd2XsdFeature> featureSet = new HashSet<Xd2XsdFeature>();

        if (features == null || features.length == 0) {
            return null;
        }

        CommandLineParser featureParser = new DefaultParser();
        CommandLine cmd = null;

        for (int i = 0; i < features.length; i++) {
            if (features[i].length() < 3) {
                features[i] = "-" + features[i];
            } else {
                features[i] = "--" + features[i];
            }
        }

        try {
            cmd = featureParser.parse(XDefToXsdOptions.features(), features, true);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        if (cmd != null) {
            if (cmd.hasOption(XDefToXsdOptionsConst.F_XSD_ANNOTATION)) { featureSet.add(Xd2XsdFeature.XSD_ANNOTATION); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_XSD_DECIMAL_ANY_SEPARATOR)) { featureSet.add(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_XSD_ALL_UNBOUNDED)) { featureSet.add(Xd2XsdFeature.XSD_ALL_UNBOUNDED); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_XSD_NAME_COLLISION_DETECTOR)) { featureSet.add(Xd2XsdFeature.XSD_NAME_COLISSION_DETECTOR); }

            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_EXTRA_SCHEMAS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_REFS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_REFS); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_QNAMES)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_QNAMES); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_ALL_TO_CHOICE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_MIXED)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_MIXED); }
            if (cmd.hasOption(XDefToXsdOptionsConst.F_POSTPROCESSING_UNIQUE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_UNIQUE); }
        }

        if (featureSet.isEmpty()) {
            return null;
        }

        return EnumSet.copyOf(featureSet);
    }

    public int getVerboseLevel(final String verbose) {
        int res = SchemaLoggerDefs.LOG_INFO;
        try {
            Integer verboseLevel = Integer.valueOf(verbose);
            if (verboseLevel < SchemaLoggerDefs.LOG_NONE || verboseLevel > SchemaLoggerDefs.LOG_TRACE) {
                System.out.println("Unknown verbose level, use default: " + SchemaLoggerDefs.LOG_INFO + " (info)");
            } else {
                res = verboseLevel;
            }
        } catch (NumberFormatException e) {
            // Do nothing
        }

        return res;
    }

}
