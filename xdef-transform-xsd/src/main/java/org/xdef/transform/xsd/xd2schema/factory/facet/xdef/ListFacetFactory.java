package org.xdef.transform.xsd.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;

import java.util.List;

public class ListFacetFactory extends DefaultFacetFactory {
    static public final String XD_PARSER_NAME = "list";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (param.getValue() instanceof XDParser) {
            return true;
        }

        return false;
    }

}
