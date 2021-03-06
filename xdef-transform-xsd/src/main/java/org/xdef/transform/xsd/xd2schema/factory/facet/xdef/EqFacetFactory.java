package org.xdef.transform.xsd.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.xd2schema.factory.facet.AbstractXsdFacetFactory;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_ARGUMENT;

public class EqFacetFactory extends AbstractXsdFacetFactory {

    static public final String XD_PARSER_NAME = "eq";
    static public final String XD_PARSER_CI_NAME = "eqi";

    private final boolean isCaseSensitive;

    public EqFacetFactory(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            final String pattern = isCaseSensitive ? param.getValue().stringValue() : Xd2XsdUtils.regex2CaseInsensitive(param.getValue().stringValue());
            facets.add(pattern(pattern));
            return true;
        }

        return false;
    }

}
