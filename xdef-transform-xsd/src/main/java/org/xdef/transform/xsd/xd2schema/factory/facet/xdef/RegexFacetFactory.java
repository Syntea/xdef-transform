package org.xdef.transform.xsd.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;

import java.util.List;

import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_ARGUMENT;

public class RegexFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "regex";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            facets.add(pattern(param.getValue().stringValue()));
            return true;
        }

        return false;
    }

}
