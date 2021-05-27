package org.xdef.transform.xsd.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;

import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_ARGUMENT;

public class TokensFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "tokens";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            enumeration(param.getValue().toString().split("\\s*\\|\\s*"), facets);
            return true;
        }

        return false;
    }

    protected void enumeration(final String[] values, final List<XmlSchemaFacet> facets) {
        LOG.debug("{}Add facet enumeration", logHeader(TRANSFORMATION));
        for (String value : values) {
            XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
            // Remove all new lines and leading whitespaces on new line
            String strValue = value.replaceAll("\\n *", " ");
            facet.setValue(strValue);
            facets.add(facet);
        }
        return;
    }
}
