package org.xdef.transform.xsd.schema2xd.definition;

/**
 * Features of XML Schema -> X-Definition transform algorithm
 */
public enum Xsd2XdFeature {

    XD_EXPLICIT_OCCURRENCE,         // Output X-Definition will contain "occurs 1" in nodes with cardinality 1
    XD_TEXT_REQUIRED,               // Output X-Definition will contain required occurrence for text nodes converted from XML Schema extension
    XD_MIXED_REQUIRED,              // Output X-Definition will contain required occurrence for mixed text in element nodes

}
