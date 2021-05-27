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
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.impl.code.DefLong;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.factory.facet.IXsdFacetFactory.ValueType.DECIMAL_FLOATING;
import static org.xdef.transform.xsd.xd2schema.factory.facet.IXsdFacetFactory.ValueType.DECIMAL_INTEGER;

/**
 * Default implementation of transformation facet/restrictions
 */
public class DefaultFacetFactory extends AbstractXsdFacetFactory {

    @Override
    public XmlSchemaMinInclusiveFacet minInclusive(final XDNamedValue param) {
        LOG.debug("{}Add facet minInclusive", logHeader(TRANSFORMATION));
        XmlSchemaMinInclusiveFacet facet = new XmlSchemaMinInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxInclusiveFacet maxInclusive(final XDNamedValue param) {
        LOG.debug("{}Add facet maxInclusive", logHeader(TRANSFORMATION));
        XmlSchemaMaxInclusiveFacet facet = new XmlSchemaMaxInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMinExclusiveFacet minExclusive(final XDNamedValue param) {
        LOG.debug("{}Add facet minExclusive", logHeader(TRANSFORMATION));
        XmlSchemaMinExclusiveFacet facet = new XmlSchemaMinExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxExclusiveFacet maxExclusive(final XDNamedValue param) {
        LOG.debug("{}Add facet maxExclusive", logHeader(TRANSFORMATION));
        XmlSchemaMaxExclusiveFacet facet = new XmlSchemaMaxExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxLengthFacet maxLength(final XDNamedValue param) {
        LOG.debug("{}Add facet maxLength", logHeader(TRANSFORMATION));
        XmlSchemaMaxLengthFacet facet = new XmlSchemaMaxLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaMinLengthFacet minLength(final XDNamedValue param) {
        LOG.debug("{}Add facet minLength", logHeader(TRANSFORMATION));
        XmlSchemaMinLengthFacet facet = new XmlSchemaMinLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaLengthFacet length(final XDNamedValue param) {
        LOG.debug("{}Add facet length", logHeader(TRANSFORMATION));
        XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaWhiteSpaceFacet whitespace(final XDNamedValue param) {
        LOG.debug("{}Add facet whitespace", logHeader(TRANSFORMATION));
        XmlSchemaWhiteSpaceFacet facet = new XmlSchemaWhiteSpaceFacet();
        facet.setValue(param.getValue().stringValue());
        return facet;
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        LOG.debug("{}Add facet pattern", logHeader(TRANSFORMATION));

        List<XmlSchemaPatternFacet> facets = new ArrayList<>();
        String[] patterns = param.getValue().stringValue().split("\n");
        for (String p : patterns) {
            facets.add(super.pattern(p));
        }

        return facets;
    }

    @Override
    public List<XmlSchemaEnumerationFacet> enumeration(XDNamedValue param) {
        LOG.debug("{}Add facet enumeration", logHeader(TRANSFORMATION));

        List<XmlSchemaEnumerationFacet> facets = new ArrayList<>();
        if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
            for (XDValue value : ((XDContainer) param.getValue()).getXDItems()) {
                XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
                // Remove all new lines and leading whitespaces on new line
                String strValue = value.stringValue().replaceAll("\\n *", " ");
                facet.setValue(strValue);
                facets.add(facet);
            }
        }
        return facets;
    }

    @Override
    public XmlSchemaFractionDigitsFacet fractionDigits(final XDNamedValue param) {
        LOG.debug("{}Add facet fractionDigits", logHeader(TRANSFORMATION));
        XmlSchemaFractionDigitsFacet facet = new XmlSchemaFractionDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(final XDNamedValue param) {
        LOG.debug("{}Add facet totalDigits", logHeader(TRANSFORMATION));
        XmlSchemaTotalDigitsFacet facet = new XmlSchemaTotalDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    /**
     * Set given X-Definition value into XML Schema facet
     * @param facet     XML Schema facet
     * @param xdValue   X-Definition value
     */
    protected void setValue(final XmlSchemaFacet facet, final XDValue xdValue) {
        if (DECIMAL_INTEGER.equals(valueType)) {
            facet.setValue((xdValue instanceof DefLong) ? xdValue.longValue() : xdValue.intValue());
        } else if (DECIMAL_FLOATING.equals(valueType)) {
            facet.setValue(xdValue.doubleValue());
        } else {
            facet.setValue(xdValue.stringValue());
        }
    }

}
