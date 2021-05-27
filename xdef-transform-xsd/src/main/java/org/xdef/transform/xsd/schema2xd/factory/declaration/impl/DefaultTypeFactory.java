package org.xdef.transform.xsd.schema2xd.factory.declaration.impl;

/**
 * Declaration for transforming any type of value by default implementation
 */
public class DefaultTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public DefaultTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

}
