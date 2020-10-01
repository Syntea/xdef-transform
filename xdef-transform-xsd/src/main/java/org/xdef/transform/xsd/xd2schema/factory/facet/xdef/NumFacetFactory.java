package org.xdef.transform.xsd.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;

import java.util.List;

public class NumFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "num";

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        facets.add(pattern("([0-9])*"));
    }
}
