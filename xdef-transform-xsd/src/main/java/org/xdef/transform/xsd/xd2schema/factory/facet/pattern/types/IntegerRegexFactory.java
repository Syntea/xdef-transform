package org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types;

import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.xd2schema.util.RangeRegexGenerator;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MAX_EXCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MAX_INCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MIN_EXCLUSIVE;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MIN_INCLUSIVE;

public class IntegerRegexFactory extends AbstractRegexFactory {

    @Override
    public String regex(final XDNamedValue[] params) {
        Integer rangeMin = null;
        Integer rangeMax = null;

        for (XDNamedValue param : params) {
            if (XSD_FACET_MAX_EXCLUSIVE.equals(param.getName())) {
                rangeMax = param.getValue().intValue() + 1;
            } else if (XSD_FACET_MAX_INCLUSIVE.equals(param.getName())) {
                rangeMax = param.getValue().intValue();
            } else if (XSD_FACET_MIN_EXCLUSIVE.equals(param.getName())) {
                rangeMin = param.getValue().intValue() - 1;
            } else if (XSD_FACET_MIN_INCLUSIVE.equals(param.getName())) {
                rangeMin = param.getValue().intValue();
            }
        }

        if (rangeMin == null) {
            rangeMin = 0;
        }

        if (rangeMax == null) {
            rangeMax = 1000;
        }

        String pattern = "";

        try {
            // Build regular expression for list of integers
            final RangeRegexGenerator rangeRegexGenerator = new RangeRegexGenerator();
            final List<String> regex = rangeRegexGenerator.getRegex(rangeMin, rangeMax);
            pattern = Xd2XsdUtils.regexCollectionToSingle(regex);
        } catch (NumberFormatException ex) {
            adapterCtx.getReportWriter().error(XSD.XSD045, ex.getMessage());
            LOG.error(StringFormatter.format("{}Exception occurs while converting range to regex.",
                    logHeader(TRANSFORMATION)),
                    ex);
        }

        LOG.debug("{}Pattern created. patternValue='{}'", logHeader(TRANSFORMATION), pattern);
        return pattern;
    }
}
