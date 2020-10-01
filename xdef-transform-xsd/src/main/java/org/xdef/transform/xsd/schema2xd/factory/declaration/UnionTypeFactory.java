package org.xdef.transform.xsd.schema2xd.factory.declaration;

/**
 * Declaration for transforming union value
 */
public class UnionTypeFactory extends AbstractDeclarationTypeFactory {

    public static final String XD_TYPE = "union";

    @Override
    public String getDataType() {
        return XD_TYPE;
    }

}
