package org.xdef.transform.xsd.xd2schema.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types.EnumerationRegexFactory;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

public class TokensRegexFacetFactory extends AbstractArrayFacetFactory {

    static public final String XD_PARSER_CI_NAME = "tokensi";

    private String regex = null;

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        regex = EnumerationRegexFactory.containerValuesToPattern(param.getValue().toString().split("\\s*\\|\\s*"));
        regex = Xd2XsdUtils.regex2CaseInsensitive(regex);
        return true;
    }

    @Override
    protected void createPatternFacets(final List<XmlSchemaFacet> facets) {
        LOG.debug("{}Creating pattern facets ...", logHeader(TRANSFORMATION));
        facets.add(super.pattern(regex));
    }

}
