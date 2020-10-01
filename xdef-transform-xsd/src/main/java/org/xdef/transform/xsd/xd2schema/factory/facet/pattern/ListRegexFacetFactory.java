package org.xdef.transform.xsd.xd2schema.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.transform.xsd.util.SchemaLogger;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types.EnumerationRegexFactory;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_DEBUG;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_LENGTH;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MAX_LENGTH;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MIN_LENGTH;

public class ListRegexFacetFactory extends AbstractArrayFacetFactory {

    static public final String XD_PARSER_NAME = "list";
    static public final String XD_PARSER_CI_NAME = "listi";

    private final boolean isCaseSensitive;

    private Integer minItems = null;
    private Integer maxItems = null;
    private String regex = null;

    public ListRegexFacetFactory(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
            regex = EnumerationRegexFactory.containerValuesToPattern((XDContainer) param.getValue());
            if (!isCaseSensitive) {
                regex = Xd2XsdUtils.regex2CaseInsensitive(regex);
            }
            return true;
        }

        return createPatternFromValue(param.getValue());
    }

    @Override
    protected void createPatterns(final String parserName, final XDNamedValue[] params) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating patterns ...");
        regex = parserParamsToRegex(parserName, params);
        if (!isCaseSensitive) {
            regex = Xd2XsdUtils.regex2CaseInsensitive(regex);
        }
    }

    @Override
    protected void createPatternFacets(final List<XmlSchemaFacet> facets) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern facets ...");

        if (regex != null && !regex.isEmpty()) {
            String pattern = "((" + regex + ")\\s)";
            if (minItems != null || maxItems != null) {
                if (minItems != null) {
                    if (--minItems < 0) {
                        minItems = 0;
                    }
                }

                if (maxItems != null) {
                    if (--maxItems < 0) {
                        maxItems = 0;
                    }
                }

                if (minItems != null && minItems.equals(maxItems)) {
                    pattern += "{" + minItems + "}";
                } else {
                    pattern += "{" + (minItems == null ? 0 : minItems) + ", " + (maxItems == null ? "" : maxItems) + "}";
                }
            } else {
                pattern += "*";
            }

            pattern += "(" + regex + "){0,1}";

            facets.add(super.pattern(pattern));
        }
    }

    @Override
    protected boolean handleIgnoredParam(XDNamedValue param) {
        if (XSD_FACET_LENGTH.equals(param.getName())) {
            minItems = param.getValue().intValue();
            maxItems = param.getValue().intValue();
            return true;
        } else if (XSD_FACET_MAX_LENGTH.equals(param.getName())) {
            maxItems = param.getValue().intValue();
            return true;
        } else if (XSD_FACET_MIN_LENGTH.equals(param.getName())) {
            minItems = param.getValue().intValue();
            return true;
        }

        return false;
    }

}
