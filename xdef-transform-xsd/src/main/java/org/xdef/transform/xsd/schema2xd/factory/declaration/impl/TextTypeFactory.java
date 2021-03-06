package org.xdef.transform.xsd.schema2xd.factory.declaration.impl;

/**
 * Declaration for transforming string based values
 */
public class TextTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public TextTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

    @Override
    protected void buildFacets(final StringBuilder sb) {
        if (hasFacet(FACET_LENGTH)) {
            facetBuilder(sb, useFacet(FACET_LENGTH));
        } else if (hasFacet(FACET_MIN_LENGTH) && hasFacet(FACET_MAX_LENGTH)) {
            facetBuilder(sb, useFacet(FACET_MIN_LENGTH) + ", " + useFacet(FACET_MAX_LENGTH));
        }
        else if (!hasFacet(FACET_MIN_LENGTH) && hasNoMultipleFacet() && "string".equals(xdType)) {
            facetBuilder(sb, "0, *");
        }
    }

}
