package org.xdef.transform.xsd.xd2schema.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types.EnumerationRegexFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types.IntegerRegexFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types.RegexFactory;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;


public abstract class AbstractArrayFacetFactory extends DefaultFacetFactory {

    protected Set<Integer> ignoredParams = new HashSet<Integer>();
    protected QName type = null;

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        LOG.info("{}Building facets ...", logHeader(TRANSFORMATION));

        final List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (ignoredParams.contains(i)) {
                    continue;
                }

                build(facets, params[i]);
            }
        }

        createPatternFacets(facets);
        return facets;
    }

    public QName determineBaseType(final XDNamedValue[] parameters) {
        LOG.debug("{}Determination of QName...", logHeader(TRANSFORMATION));

        String parserName = null;
        boolean allParsersSame = true;
        boolean nonItemRestriction = false;

        for (int i = 0; i < parameters.length; i++) {
            XDValue xVal = parameters[i].getValue();
            if (xVal.getItemId() == XD_CONTAINER) {
                XDValue[] values = ((XDContainer) xVal).getXDItems();
                /*if (values.length > 1) {
                    System.out.println("List/Union - multiple parsers - unsupported!");
                    allParsersSame = false;
                    ignoredParams.add(i);
                }
                else*/ if (values.length == 1) {
                    if (parserName == null || parserName.isEmpty()) {
                        parserName = ((XDParser) values[0]).parserName();
                    } else {
                        allParsersSame = checkParser((XDParser) values[0], i, parserName);
                    }
                }
            }
            else if (xVal instanceof XDParser) {
                if (parserName == null || parserName.isEmpty()) {
                    parserName = ((XDParser) xVal).parserName();
                } else {
                    allParsersSame = checkParser((XDParser) xVal, i, parserName);
                }
            } else {
                nonItemRestriction = true;
                ignoredParams.add(i);
            }
        }

        if ((nonItemRestriction == true && parserName != null && !parserName.isEmpty()) || allParsersSame == false) {
            String ignoredParamsStr = "";
            for (Integer paramIndex : ignoredParams) {
                if (!handleIgnoredParam(parameters[paramIndex])) {
                    ignoredParamsStr += parameters[paramIndex].getName() + ", ";
                }
            }

            if (!ignoredParamsStr.isEmpty()) {
                adapterCtx.getReportWriter().warning(XSD.XSD031, ignoredParamsStr);
                LOG.warn("{}List/Union facet - Using of unhandled restrictions found! Following attributes/parsers " +
                                "being ignored: {}",
                        logHeader(TRANSFORMATION), ignoredParamsStr);
            }
        }

        type = null;
        if (parserName == null || parserName.isEmpty()) {
            type = Constants.XSD_STRING;
        } else if (allParsersSame) {
            type = Xd2XsdParserMapping.findDefaultParserQName(parserName, adapterCtx)
                    .orElse(Constants.XSD_STRING);
        }

        return type;
    }

    protected String parserParamsToRegex(final String parserName, final XDNamedValue[] params) {
        QName parserQName = (this instanceof ListRegexFacetFactory)
                ? type
                : Xd2XsdParserMapping.findDefaultParserQName(parserName, adapterCtx).orElse(null);

        RegexFactory regexFactory = null;
        String regex = "";

        if (Constants.XSD_INT.equals(parserQName)) {
            regexFactory = new IntegerRegexFactory();
        } else if (Constants.XSD_STRING.equals(parserQName)) {
            regexFactory = new EnumerationRegexFactory();
        } else {
            adapterCtx.getReportWriter().warning(XSD.XSD032, type);
            LOG.warn("{}Parser params to regex - Unsupported list parser! qName='{}'",
                    logHeader(TRANSFORMATION), type);
        }

        if (regexFactory != null) {
            regexFactory.setAdapterCtx(adapterCtx);
            regex = regexFactory.regex(params);
        }

        return regex;
    }

    protected boolean handleIgnoredParam(XDNamedValue param) {
        return false;
    }

    protected void createPatternFacets(final List<XmlSchemaFacet> facets) { }

    protected void createPatterns(final String parserName, final XDNamedValue[] params) { }

    protected boolean createPatternFromValue(final XDValue xVal) {
        LOG.debug("{}Creating pattern from value", logHeader(TRANSFORMATION));

        if (xVal instanceof XDParser) {
            final XDParser parser = ((XDParser) xVal);
            createPatterns(parser.parserName(), parser.getNamedParams().getXDNamedItems());
            return true;
        } else {
            adapterCtx.getReportWriter().warning(XSD.XSD033, xVal.getItemId());
            LOG.warn("{}Unsupported value of simple content type. valueId=",
                    logHeader(TRANSFORMATION), xVal.getItemId());
        }

        return false;
    }

    private boolean checkParser(final XDParser parser, final int index, final String parserName) {
        if (!parserName.equals(parser.parserName())) {
            LOG.debug("{}List/Union - parsers are not same!", logHeader(TRANSFORMATION));
            ignoredParams.add(index);
            return false;
        }

        return true;
    }

}
