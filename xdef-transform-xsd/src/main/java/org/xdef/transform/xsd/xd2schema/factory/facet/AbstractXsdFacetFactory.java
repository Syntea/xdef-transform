package org.xdef.transform.xsd.xd2schema.factory.facet;

import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaFractionDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.apache.ws.commons.schema.XmlSchemaTotalDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_FORMAT;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_INTERNAL_FACET_OUTFORMAT;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_ENUMERATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_FRACTION_DIGITS;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_LENGTH;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MAX_EXCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MAX_INCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MAX_LENGTH;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MIN_EXCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MIN_INCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_MIN_LENGTH;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_PATTERN;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_TOTAL_DIGITS;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XSD_FACET_WHITESPACE;

/**
 * Base class for transformation of facets
 */
public abstract class AbstractXsdFacetFactory implements IXsdFacetFactory {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * XML Schema adapter context
     */
    protected XsdAdapterCtx adapterCtx;

    /**
     * X-definition value type
     */
    protected ValueType valueType;

    @Override
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        LOG.debug("{}\"Building facets ...", logHeader(TRANSFORMATION));

        List<XmlSchemaFacet> facets = new ArrayList<>();
        if (params != null && params.length > 0) {
            for (XDNamedValue param : params) {
                build(facets, param);
            }
        } else {
            LOG.debug("{}No basic facets will be built - no input params",
                    logHeader(TRANSFORMATION));
        }

        LOG.debug("{}Building extra facets ...", logHeader(TRANSFORMATION));
        extraFacets(facets);
        return facets;
    }

    @Override
    public XmlSchemaMinExclusiveFacet minExclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("minExclusive");
    }

    @Override
    public XmlSchemaMinInclusiveFacet minInclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("minInclusive");
    }

    @Override
    public XmlSchemaMaxExclusiveFacet maxExclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("maxExclusive");
    }

    @Override
    public XmlSchemaMaxInclusiveFacet maxInclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("maxInclusive");
    }

    @Override
    public XmlSchemaMinLengthFacet minLength(XDNamedValue param) {
        throw new UnsupportedOperationException("minLength");
    }

    @Override
    public XmlSchemaMaxLengthFacet maxLength(XDNamedValue param) {
        throw new UnsupportedOperationException("maxLength");
    }

    @Override
    public XmlSchemaLengthFacet length(XDNamedValue param) {
        throw new UnsupportedOperationException("length");
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(XDNamedValue param) {
        throw new UnsupportedOperationException("pattern");
    }

    @Override
    public XmlSchemaPatternFacet pattern(String value) {
        LOG.debug("{}Pattern. value='{}'", logHeader(TRANSFORMATION), value);
        XmlSchemaPatternFacet facet = new XmlSchemaPatternFacet();
        facet.setValue(value);
        return facet;
    }

    @Override
    public List<XmlSchemaEnumerationFacet> enumeration(XDNamedValue param) {
        throw new UnsupportedOperationException("enumeration");
    }

    @Override
    public XmlSchemaFractionDigitsFacet fractionDigits(XDNamedValue param) {
        throw new UnsupportedOperationException("fractionDigits");
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(XDNamedValue param) {
        throw new UnsupportedOperationException("totalDigits");
    }

    @Override
    public XmlSchemaWhiteSpaceFacet whitespace(XDNamedValue param) {
        throw new UnsupportedOperationException("whitespace");
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        return false;
    }

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
    }

    @Override
    public void setValueType(final ValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Creates facet from given X-Definition parameter
     * @param facets    list of facets, where newly created facet will be inserted
     * @param param     X-Definition parameter
     */
    protected void build(final List<XmlSchemaFacet> facets, final XDNamedValue param) {
        LOG.debug("{}Creating Facet. typeName='{}'",
                logHeader(TRANSFORMATION), param.getName());

        XmlSchemaFacet facet = null;

        if (XSD_FACET_ENUMERATION.equals(param.getName())) {
            facets.addAll(enumeration(param));
        } else if (XSD_FACET_MAX_EXCLUSIVE.equals(param.getName())) {
            facet = maxExclusive(param);
        } else if (XSD_FACET_MAX_INCLUSIVE.equals(param.getName())) {
            facet = maxInclusive(param);
        } else if (XSD_FACET_MIN_EXCLUSIVE.equals(param.getName())) {
            facet = minExclusive(param);
        } else if (XSD_FACET_MIN_INCLUSIVE.equals(param.getName())) {
            facet = minInclusive(param);
        } else if (XSD_FACET_LENGTH.equals(param.getName())) {
            facet = length(param);
        } else if (XSD_FACET_MAX_LENGTH.equals(param.getName())) {
            facet = maxLength(param);
        } else if (XSD_FACET_MIN_LENGTH.equals(param.getName())) {
            facet = minLength(param);
        } else if (XSD_FACET_FRACTION_DIGITS.equals(param.getName())) {
            facet = fractionDigits(param);
        } else if (XSD_FACET_PATTERN.equals(param.getName()) || XD_FACET_FORMAT.equals(param.getName())) {
            facets.addAll(pattern(param));
        } else if (XSD_FACET_TOTAL_DIGITS.equals(param.getName())) {
            facet = totalDigits(param);
        } else if (XSD_FACET_WHITESPACE.equals(param.getName())) {
            facet = whitespace(param);
        } else if (isInternalFacet(param)) {
            // Do nothing
        } else if (!customFacet(facets, param)) {
            adapterCtx.getReportWriter().warning(XSD.XSD032, param.getName());
            LOG.warn("{}Unsupported restriction parameter. parameterName='{}'",
                    logHeader(TRANSFORMATION), param.getName());
        }

        if (facet != null) {
            facets.add(facet);
        }
    }

    /**
     * Check if given X-Definition parameter is internal and should not be transformed
     * @param param     X-Definition parameter
     * @return true, if given parameter is internal
     */
    private boolean isInternalFacet(XDNamedValue param) {
        return XD_INTERNAL_FACET_OUTFORMAT.equals(param.getName());
    }
}
