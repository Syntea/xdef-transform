package org.xdef.transform.xsd.schema2xd.factory.declaration.impl;

/**
 * Declaration for transforming list value
 */
public class ListTypeFactory extends AbstractDeclarationTypeFactory {

    public static final String XD_TYPE = "list";

    @Override
    public String getDataType() {
        return XD_TYPE;
    }

}
