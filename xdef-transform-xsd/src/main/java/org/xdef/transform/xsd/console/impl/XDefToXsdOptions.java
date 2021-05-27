package org.xdef.transform.xsd.console.impl;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.xdef.transform.xsd.console.XDefToXsdOptionsConst;

public class XDefToXsdOptions {

    public static Options cli() {
        Options options = new Options();

        Option input = new Option("i", XDefToXsdOptionsConst.INPUT_DIR, true, "input directory path containing X-Definition file(s)");
        input.setRequired(true);
        options.addOption(input);

        Option outputDir = new Option("o", XDefToXsdOptionsConst.OUTPUT_DIR, true, "output directory path, where XML schema(s) will be saved");
        outputDir.setRequired(true);
        options.addOption(outputDir);

        Option schemaPrefix = new Option("sp", XDefToXsdOptionsConst.OUTPUT_FILE_PREFIX, true, "prefix of XML schema output file(s).\nDefault: ''");
        schemaPrefix.setRequired(false);
        options.addOption(schemaPrefix);

        Option schemaFileExt = new Option("se", XDefToXsdOptionsConst.OUTPUT_EXT, true, "extension of XML schema output file(s).\nDefault: '.xsd'");
        schemaFileExt.setRequired(false);
        options.addOption(schemaFileExt);

        Option xDefRootModel = new Option("r", XDefToXsdOptionsConst.INPUT_ROOT, true, "name of root X-Definition (used for validation purposes)");
        xDefRootModel.setRequired(false);
        options.addOption(xDefRootModel);

        Option validatePos = new Option("tp", XDefToXsdOptionsConst.VALIDATE_POSITIVE_CASE, true, "testing XML data file paths (expected positive result)");
        validatePos.setRequired(false);
        validatePos.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(validatePos);

        Option validateNeg = new Option("tn", XDefToXsdOptionsConst.VALIDATE_NEGATIVE_CASE, true, "testing XML data file paths (expected negative result)");
        validateNeg.setRequired(false);
        validateNeg.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(validateNeg);

        Option defaultFeatures = new Option("nf", XDefToXsdOptionsConst.NO_DEFAULT_FEATURES, false, "do not use default transformation features");
        defaultFeatures.setRequired(false);
        options.addOption(defaultFeatures);

        Option features = new Option("f", XDefToXsdOptionsConst.FEATURES, true, "transformation features\n" +
                XDefToXsdOptionsConst.F_XSD_ANNOTATION + " - XSD_ANNOTATION,\n" +
                XDefToXsdOptionsConst.F_XSD_DECIMAL_ANY_SEPARATOR + " - XSD_DECIMAL_ANY_SEPARATOR,\n" +
                XDefToXsdOptionsConst.F_XSD_ALL_UNBOUNDED + " - XSD_ALL_UNBOUNDED,\n" +
                XDefToXsdOptionsConst.F_XSD_NAME_COLLISION_DETECTOR + " - XSD_NAME_COLISSION_DETECTOR,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING + " - POSTPROCESSING,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_EXTRA_SCHEMAS + " - POSTPROCESSING_EXTRA_SCHEMAS,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_REFS + " - POSTPROCESSING_REFS,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_QNAMES + " - POSTPROCESSING_QNAMES,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_ALL_TO_CHOICE + " - POSTPROCESSING_ALL_TO_CHOICE,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_MIXED + " - POSTPROCESSING_MIXED,\n" +
                XDefToXsdOptionsConst.F_POSTPROCESSING_UNIQUE + " - POSTPROCESSING_KEYS_AND_REFS");
        features.setRequired(false);
        features.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(features);

        return options;
    }

    public static Options features() {
        Options options = new Options();

        addFeatureOption(XDefToXsdOptionsConst.F_XSD_ANNOTATION, "annotation", options);
        addFeatureOption(XDefToXsdOptionsConst.F_XSD_DECIMAL_ANY_SEPARATOR, "choiceUnbounded", options);
        addFeatureOption(XDefToXsdOptionsConst.F_XSD_ALL_UNBOUNDED, "nameCollision", options);
        addFeatureOption(XDefToXsdOptionsConst.F_XSD_NAME_COLLISION_DETECTOR, "nameCollision", options);

        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING, "postprocessing", options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_EXTRA_SCHEMAS, "postprocessingExtraSchema", options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_REFS, "postprocessingReference", options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_QNAMES, "postprocessingQNames",  options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_ALL_TO_CHOICE, "postprocessingAll2Choice", options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_MIXED, "postprocessingMixed", options);
        addFeatureOption(XDefToXsdOptionsConst.F_POSTPROCESSING_UNIQUE, "postprocessingUnique", options);

        return options;
    }

    private static void addFeatureOption(final String opt, final String longOpt, final Options options) {
        Option pp = new Option(opt, longOpt, false, "");
        pp.setRequired(false);
        options.addOption(pp);
    }
}
