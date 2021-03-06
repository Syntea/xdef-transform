package org.xdef.transform.xsd.xd2schema.util;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.facet.DefaultFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.IXsdFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.ListRegexFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.pattern.TokensRegexFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.AnFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.ContainerFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.ContainsFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.DateTimeFormatFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.DecFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.EndsFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.EnumFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.EqFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.ListFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.MD5FacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.NumFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.RegexFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.StartsFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.TokensFacetFactory;
import org.xdef.transform.xsd.xd2schema.factory.facet.xdef.UnionFacetFactory;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_CDATA;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_ISODATE;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_ISODATETIME;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_ISOYEAR;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_ISOYEARMONTH;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_REGEX;

/**
 * Definition of transformation X-Definition data types to XML Schema data types
 */
public class Xd2XsdParserMapping {

    private static final Logger LOG = LoggerFactory.getLogger(Xd2XsdParserMapping.class);

    /**
     * Transformation map of X-Definition data types to XML Schema QNames using XML Schema default facet factory
     */
    private static final Map<String, QName> defaultQNameMap = new HashMap<>();

    /**
     * Transformation map of X-Definition parsers to XML Schema QNames and custom implementation of XML Schema facet factory
     * Some X-Definition types requires specific way how to create simpleType and restrictions
     */
    private static final Map<String, Pair<QName, IXsdFacetFactory>> customFacetMap = new HashMap<>();

    static {
        // Default parsers - custom X-Definition names
        defaultQNameMap.put(XD_PARSER_CDATA, Constants.XSD_STRING);
        defaultQNameMap.put(XD_PARSER_ISODATE, Constants.XSD_DATE);
        defaultQNameMap.put(XD_PARSER_ISODATETIME, Constants.XSD_DATETIME);
        defaultQNameMap.put(XD_PARSER_ISOYEARMONTH, Constants.XSD_YEARMONTH);
        defaultQNameMap.put(XD_PARSER_ISOYEAR, Constants.XSD_YEAR);
        defaultQNameMap.put(XD_PARSER_REGEX, Constants.XSD_STRING);

        // Default parsers
        defaultQNameMap.put(Constants.XSD_BASE64.getLocalPart(), Constants.XSD_BASE64);
        defaultQNameMap.put(Constants.XSD_BOOLEAN.getLocalPart(), Constants.XSD_BOOLEAN);
        defaultQNameMap.put(Constants.XSD_DATE.getLocalPart(), Constants.XSD_DATE);
        defaultQNameMap.put(Constants.XSD_DATETIME.getLocalPart(), Constants.XSD_DATETIME);
        defaultQNameMap.put(Constants.XSD_DAY.getLocalPart(), Constants.XSD_DAY);
        defaultQNameMap.put(Constants.XSD_DOUBLE.getLocalPart(), Constants.XSD_DOUBLE);
        defaultQNameMap.put(Constants.XSD_DURATION.getLocalPart(), Constants.XSD_DURATION);
        defaultQNameMap.put(Constants.XSD_ENTITY.getLocalPart(), Constants.XSD_ENTITY);
        defaultQNameMap.put(Constants.XSD_ENTITIES.getLocalPart(), Constants.XSD_ENTITIES);
        defaultQNameMap.put(Constants.XSD_FLOAT.getLocalPart(), Constants.XSD_FLOAT);
        defaultQNameMap.put(Constants.XSD_HEXBIN.getLocalPart(), Constants.XSD_HEXBIN);
        defaultQNameMap.put(Constants.XSD_ID.getLocalPart(), Constants.XSD_ID);
        defaultQNameMap.put(Constants.XSD_IDREF.getLocalPart(), Constants.XSD_IDREF);
        defaultQNameMap.put(Constants.XSD_IDREFS.getLocalPart(), Constants.XSD_IDREFS);
        defaultQNameMap.put(Constants.XSD_INT.getLocalPart(), Constants.XSD_INT);
        defaultQNameMap.put(Constants.XSD_LANGUAGE.getLocalPart(), Constants.XSD_LANGUAGE);
        defaultQNameMap.put(Constants.XSD_LONG.getLocalPart(), Constants.XSD_LONG);
        defaultQNameMap.put(Constants.XSD_MONTH.getLocalPart(), Constants.XSD_MONTH);
        defaultQNameMap.put(Constants.XSD_MONTHDAY.getLocalPart(), Constants.XSD_MONTHDAY);
        defaultQNameMap.put(Constants.XSD_NCNAME.getLocalPart(), Constants.XSD_NCNAME);
        defaultQNameMap.put(Constants.XSD_NMTOKEN.getLocalPart(), Constants.XSD_NMTOKEN);
        defaultQNameMap.put(Constants.XSD_NMTOKENS.getLocalPart(), Constants.XSD_NMTOKENS);
        defaultQNameMap.put(Constants.XSD_NORMALIZEDSTRING.getLocalPart(), Constants.XSD_NORMALIZEDSTRING);
        defaultQNameMap.put(Constants.XSD_QNAME.getLocalPart(), Constants.XSD_QNAME);
        defaultQNameMap.put(Constants.XSD_STRING.getLocalPart(), Constants.XSD_STRING);
        defaultQNameMap.put(Constants.XSD_TIME.getLocalPart(), Constants.XSD_TIME);
        defaultQNameMap.put(Constants.XSD_TOKEN.getLocalPart(), Constants.XSD_TOKEN);
        defaultQNameMap.put(Constants.XSD_YEAR.getLocalPart(), Constants.XSD_YEAR);

        // Custom static facets
        customFacetMap.put(AnFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new AnFacetFactory()));
        customFacetMap.put(ContainerFacetFactory.XD_PARSER_NAME_LANGUAGES, Pair.of(Constants.XSD_STRING, new ContainerFacetFactory("[a-zA-Z0-9]{2,3}")));
        customFacetMap.put(ContainerFacetFactory.XD_PARSER_NAME_NC_NAMELIST, Pair.of(Constants.XSD_STRING, new ContainerFacetFactory("[a-zA-Z0-9]+")));
        customFacetMap.put(ContainsFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new ContainsFacetFactory(true)));
        customFacetMap.put(ContainsFacetFactory.XD_PARSER_CI_NAME, Pair.of(Constants.XSD_STRING, new ContainsFacetFactory(false)));
        customFacetMap.put(EnumFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new EnumFacetFactory()));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_XDATETIME_NAME, Pair.of(Constants.XSD_STRING, new DateTimeFormatFacetFactory()));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_DATETIME_NAME, Pair.of(Constants.XSD_STRING, new DateTimeFormatFacetFactory("yyyyMMddHHmmss")));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_EMAILDATE_NAME, Pair.of(Constants.XSD_STRING, new DateTimeFormatFacetFactory("EEE, d MMM y HH:mm:ss[ ZZZZZ][ (z)]")));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new EndsFacetFactory(true)));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_CI_NAME, Pair.of(Constants.XSD_STRING, new EndsFacetFactory(false)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new EqFacetFactory(true)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_CI_NAME, Pair.of(Constants.XSD_STRING, new EqFacetFactory(false)));
        customFacetMap.put(MD5FacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new MD5FacetFactory()));
        customFacetMap.put(NumFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new NumFacetFactory()));
        customFacetMap.put(RegexFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new RegexFacetFactory()));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new StartsFacetFactory(true)));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_CI_NAME, Pair.of(Constants.XSD_STRING, new StartsFacetFactory(false)));
        customFacetMap.put(TokensFacetFactory.XD_PARSER_NAME, Pair.of(Constants.XSD_STRING, new TokensFacetFactory()));
        customFacetMap.put(TokensRegexFacetFactory.XD_PARSER_CI_NAME, Pair.of(Constants.XSD_STRING, new TokensRegexFacetFactory()));

    }

    /**
     * Converts X-Definition parser name to XML Schema qualified name
     * @param parserName    X-Definition parser name
     * @return  XML Schema QName if mapping exists,
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<QName> findDefaultParserQName(final String parserName, final XsdAdapterCtx adapterCtx) {
        final QName qName = Optional.ofNullable(defaultQNameMap.get(parserName)).orElseGet(() -> {
            if (DecFacetFactory.XD_PARSER_NAME.equals(parserName)
                    && !adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR)) {
                return Constants.XSD_DECIMAL;
            }

            return null;
        });

        return Optional.ofNullable(qName);
    }

    /**
     * Finds XML Schema qualified name and XML Schema facet factory for specific X-Definition parser
     * @param parserName    X-Definition parser name
     * @return  XML Schema data type with XML Schema facets factory if transformation exists
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<Pair<QName, IXsdFacetFactory>> findCustomFacetFactory(
            final String parserName,
            final XDNamedValue[] parameters,
            final XsdAdapterCtx adapterCtx) {
        Pair<QName, IXsdFacetFactory> res = customFacetMap.get(parserName);
        // Custom dynamic facet factories
        if (res == null) {
            if (DecFacetFactory.XD_PARSER_NAME.equals(parserName) && adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR)) {
                res = Pair.of(Constants.XSD_STRING, new DecFacetFactory());
            } else if (ListFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                final QName qName = determineListBaseType(parameters, adapterCtx);
                res = Pair.of(qName, new ListFacetFactory());
            } else if (UnionFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                res = Pair.of(null, new UnionFacetFactory());
            } else if (/*ListRegexFacetFactory.XD_PARSER_NAME.equals(parserName) || */ListRegexFacetFactory.XD_PARSER_CI_NAME.equals(parserName)) {
                ListRegexFacetFactory facetBuilder = new ListRegexFacetFactory(ListRegexFacetFactory.XD_PARSER_NAME.equals(parserName));
                res = Pair.of(facetBuilder.determineBaseType(parameters), facetBuilder);
            }/* else if (UnionRegexFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                UnionRegexFacetFactory facetBuilder = new UnionRegexFacetFactory();
                res = Pair.of(facetBuilder.determineBaseType(parameters), facetBuilder);
            }*/
        }

        return Optional.ofNullable(res);
    }

    /**
     * Converts given X-Definition parser name to XML Schema qualified name
     * @param parserName    X-Definition parser name
     * @param adapterCtx    XML Schema adapter context
     * @return  XML Schema QName with default facet factory if conversion of X-Definition parser name to XML Schema QName exists
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<Pair<QName, IXsdFacetFactory>> findDefaultFacetFactory(
            final String parserName,
            final XsdAdapterCtx adapterCtx) {
        return findDefaultParserQName(parserName, adapterCtx).map(qName -> Pair.of(qName, new DefaultFacetFactory()));
    }

    /**
     * Get QName for X-Definition parser which can be transformed by default XML Schema facet factory
     * @param xData         X-Definition node
     * @param adapterCtx    XML Schema adapter context
     * @param hasNoFacets   check if parser contains any facet
     * @return  XML Schema QName if transformation without facets exists
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<QName> findDefaultParserQName(final XData xData,
                                                         final XsdAdapterCtx adapterCtx,
                                                         boolean hasNoFacets) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();
        final Optional<QName> defaultQNameOpt = findDefaultParserQName(parserName, adapterCtx);

        if (defaultQNameOpt.isPresent()) {
            if (parseMethod instanceof XDParser) {
                final XDParser parser = ((XDParser) parseMethod);
                final XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
                if (!findCustomFacetFactory(parserName, parameters, adapterCtx).isPresent()) {
                    if (!hasNoFacets || parameters.length == 0)
                        return defaultQNameOpt;
                }
            } else {
                return defaultQNameOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Determine XML Schema list's qualified names by its values (given by X-Definition {@code parameters})
     * @param parameters    X-Definition parser parameters
     * @param adapterCtx    XML Schema adapter context
     * @return  if all parameters are using same known parser, then its QName
     *          if parameters are using different known parser, then string QName
     *          if parameters are using unknown parser, then exception will be raised
     */
    private static QName determineListBaseType(final XDNamedValue[] parameters, final XsdAdapterCtx adapterCtx) {
        LOG.debug("{}Determination of list QName ...", logHeader(TRANSFORMATION));

        String parserName = null;
        boolean allParsersAreSame = true;

        for (XDNamedValue parameter : parameters) {
            XDValue xVal = parameter.getValue();
            if (xVal.getItemId() == XD_CONTAINER) {
                allParsersAreSame = false;
            } else if (xVal instanceof XDParser) {
                if (parserName == null) {
                    parserName = ((XDParser) xVal).parserName();
                } else {
                    if (allParsersAreSame && !parserName.equals(((XDParser) xVal).parserName())) {
                        LOG.debug("{}List/Union - parsers are not same!", logHeader(TRANSFORMATION));
                        allParsersAreSame = false;
                    }
                }
            }
        }

        if (parserName == null || !allParsersAreSame) {
            LOG.error("{}Expected parser type or multiple parsers used!", logHeader(TRANSFORMATION));
            throw new SRuntimeException(XSD.XSD006);
        }

        final String finalParserName = parserName;
        return findDefaultParserQName(parserName, adapterCtx)
                .orElseGet(() -> {
                    adapterCtx.getReportWriter().warning(XSD.XSD026, finalParserName);
                    LOG.warn("{}Unsupported simple content parser! parserName='{}'", logHeader(TRANSFORMATION), finalParserName);
                    return Constants.XSD_STRING;
                });
    }

}
