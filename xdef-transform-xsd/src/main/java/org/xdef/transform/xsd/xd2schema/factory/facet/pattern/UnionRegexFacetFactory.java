package org.xdef.transform.xsd.xd2schema.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

public class UnionRegexFacetFactory extends AbstractArrayFacetFactory {

    static public final String XD_PARSER_NAME = "union";

    private List<String> facetPatterns = new ArrayList<>();

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        XDValue xVal = param.getValue();
        boolean res = false;
        if (xVal.getItemId() == XD_CONTAINER) {
            XDValue[] values = ((XDContainer) xVal).getXDItems();
            for (XDValue v : values) {
                boolean resTmp = createPatternFromValue(v);
                if (res == false) {
                    res = resTmp;
                }
            }
        } else {
            res = createPatternFromValue(xVal);
        }

        return res;
    }

    @Override
    protected void createPatterns(final String parserName, final XDNamedValue[] params) {
        LOG.debug("{}Creating patterns ...", logHeader(TRANSFORMATION));
        facetPatterns.add(parserParamsToRegex(parserName, params));
    }

    @Override
    protected void createPatternFacets(final List<XmlSchemaFacet> facets) {
        LOG.debug("{}Creating pattern facets ...", logHeader(TRANSFORMATION));

        if (!facetPatterns.isEmpty()) {
            // Enumeration with list inside
            if (false) {
                final StringBuilder sb = new StringBuilder();
                final StringBuilder sb2 = new StringBuilder();

                for (String p : facetPatterns) {
                    if (p != null) {
                        if (sb.length() == 0) {
                            sb.append("(((" + p + ")\\s)*)");
                        } else {
                            sb.append("|(((" + p + ")\\s)*)");
                        }
                    }
                }

                for (String p : facetPatterns) {
                    if (p != null) {
                        if (sb2.length() == 0) {
                            sb2.append("((" + p + "){0,1})");
                        } else {
                            sb2.append("|((" + p + "){0,1})");
                        }
                    }
                }

                if (sb.length() != 0) {
                    final String patternBegin = sb.toString();
                    final String patternEnd = sb2.toString();
                    final String pattern = "(" + patternBegin + ")*" + "(" + patternEnd + ")";
                    facets.add(super.pattern(pattern));
                }
            } else {
                final StringBuilder sb = new StringBuilder();
                for (String p : facetPatterns) {
                    if (p != null) {
                        if (sb.length() == 0) {
                            sb.append("(" + p + ")");
                        } else {
                            sb.append("|(" + p + ")");
                        }
                    }
                }

                if (sb.length() != 0) {
                    final String pattern = sb.toString();
                    facets.add(super.pattern("(" + pattern + ")"));
                }
            }
        }
    }

}
