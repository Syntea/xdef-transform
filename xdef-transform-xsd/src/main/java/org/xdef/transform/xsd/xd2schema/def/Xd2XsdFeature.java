package org.xdef.transform.xsd.xd2schema.def;

import java.util.EnumSet;

/**
 * Features of X-Definition -> XML Schema transform algorithm
 */
public enum Xd2XsdFeature {

    XSD_ANNOTATION,                     // Output XML Schema document will contain additional annotations
    XSD_DECIMAL_ANY_SEPARATOR,          // Output XML Schema document will convert dec parser to string regular pattern if decimal separator is not dot
    XSD_ALL_UNBOUNDED,                  // Output XML Schema document will contain only unbounded xs:choice element, if source of the element is xd:mixed
    XSD_NAME_COLLISION_DETECTOR,        // Generate new name if collision of names has been found on top-level of XML Schema
    XSD_SKIP_DELETE_TOPLEVEL_ELEMENTS,  // Do not delete top level elements if X-Definition has no root element defined
    XSD_ALL_ELEMENTS_TOPLEVEL,          // All top-level elements in X-Definition will be transformed to top-level XML Schema no matter content of xd:root
    XSD_ELEMENT_NO_SIMPLE_TYPE,         // All xs:elements nodes will contain xs:complexType node only (no xs:simpleType occurs)

    POSTPROCESSING,                     // Transform algorithm will execute additional processing of output nodes
    POSTPROCESSING_EXTRA_SCHEMAS,       // Transform algorithm will execute additional processing of nodes that is in different namespace than X-Definition using
    POSTPROCESSING_REFS,                // Transform algorithm will execute additional processing of node's references
    POSTPROCESSING_QNAMES,              // Transform algorithm will execute additional processing of node's QNames
    POSTPROCESSING_ALL_TO_CHOICE,       // Transform algorithm will execute additional processing of node's - checks positions of xs:all and convert them to xs:choice if need it
    POSTPROCESSING_MIXED,               // Transform algorithm will execute additional processing of node's - add mixed flag if need it
    POSTPROCESSING_UNIQUE;              // Transform algorithm will execute additional processing based on gathered information from uniqueSets - creating xs:unique, xs:key and xs:keyref elements from uniqueSet which are using IDREF, IDREFS, CHKID

    public static EnumSet<Xd2XsdFeature> DEFAULT_POSTPROCESSING_FEATURES = EnumSet.of(
            POSTPROCESSING, POSTPROCESSING_EXTRA_SCHEMAS, POSTPROCESSING_REFS, POSTPROCESSING_QNAMES,
            POSTPROCESSING_ALL_TO_CHOICE, POSTPROCESSING_MIXED);

}
