package org.xdef.transform.xsd.xd2schema.definition;

import org.xdef.transform.xsd.util.LoggingGroup;

/**
 * Logging definitions for X-Definition -> XML Schema transformation algorithm
 */
public enum Xd2XsdLogGroup implements LoggingGroup {

    XSD_XDEF_ADAPTER("XsdXDefinitionAdapter"),
    XSD_XDEF_EXTRA_ADAPTER("XsdXDefinitionExtraAdapter"),
    XSD_ELEM_FACTORY("XsdNodeFactory"),
    XSD_XDPOOL_ADAPTER("XsdXPoolAdapter"),
    XSD_PP_ADAPTER("XsdPPAdapter"),
    XSD_PP_PROCESSOR("XsdPPProcessor"),
    XSD_UTILS("XsdUtils"),
    XSD_ADAPTER_CTX("XsdAdapterCtx"),
    XSD_REFERENCE("SchemaNode"),
    XSD_NAME_FACTORY("NameFactory"),
    XSD_KEY_AND_REF("UniqueConstraint");

    private final String name;

    Xd2XsdLogGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
