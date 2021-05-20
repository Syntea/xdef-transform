package org.xdef.transform.xsd.console;

public interface XDefToXsdOptionsConst {
    // Input directory containing x-definition file(s)
    String INPUT_DIR = "inputDir";
    // output directory
    String OUTPUT_DIR = "outputDir";

    // output XML schema file name prefix
    String OUTPUT_FILE_PREFIX = "schemaPrefix";
    // output XML schema file extension
    String OUTPUT_EXT = "schemaExt";

    // root name of x-definition
    String INPUT_ROOT = "root";
    // XML data file(s) for testing validation of positive case
    String VALIDATE_POSITIVE_CASE = "validatePos";
    // XML data file(s) for testing validation of negative case
    String VALIDATE_NEGATIVE_CASE = "validateNeg";

    // Flag, if default algorithm features will be used
    String NO_DEFAULT_FEATURES = "noFeatures";
    // Algorithm features to be used
    String FEATURES = "features";

    /**
     * Features
     */
    String F_XSD_ANNOTATION = "a";
    String F_XSD_DECIMAL_ANY_SEPARATOR = "ds";
    String F_XSD_ALL_UNBOUNDED = "cu";
    String F_XSD_NAME_COLLISION_DETECTOR = "nc";

    String F_POSTPROCESSING = "p";
    String F_POSTPROCESSING_EXTRA_SCHEMAS = "pe";
    String F_POSTPROCESSING_REFS = "pr";
    String F_POSTPROCESSING_QNAMES = "pq";
    String F_POSTPROCESSING_ALL_TO_CHOICE = "pa";
    String F_POSTPROCESSING_MIXED = "pm";
    String F_POSTPROCESSING_UNIQUE = "pu";

}
