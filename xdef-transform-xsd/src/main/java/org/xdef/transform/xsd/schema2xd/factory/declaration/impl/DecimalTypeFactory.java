package org.xdef.transform.xsd.schema2xd.factory.declaration.impl;

/**
 * Declaration for transforming floating point values
 */
public class DecimalTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public DecimalTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

}
