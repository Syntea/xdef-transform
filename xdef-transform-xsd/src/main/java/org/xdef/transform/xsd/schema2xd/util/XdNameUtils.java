package org.xdef.transform.xsd.schema2xd.util;

import org.xdef.transform.xsd.schema2xd.model.impl.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_PREFIX_EMPTY;

/**
 * Utils related to working with node name, reference name and qualified name
 */
public class XdNameUtils {

    static private final Pattern XSD_NAME_PATTERN_1 = Pattern.compile("(.+)\\.xsd");
    static private final Pattern XSD_NAME_PATTERN_2 = Pattern.compile(".*[/|\\\\](.+)\\.xsd");

    /**
     * Parses local part of string qualified name
     * @param qName     qualified name
     * @return local part if is part of qualified name, otherwise whole qualified name
     */
    public static String getLocalName(final String qName) {
        final int nsPrefixPos = qName.indexOf(NAMESPACE_DELIMITER);
        if (nsPrefixPos != -1) {
            return qName.substring(nsPrefixPos + 1);
        }

        return qName;
    }

    /**
     * Creates string qualified name from given arguments.
     *
     * If qualified name is using unknown namespace URI in given X-Definition, then output will not contain namespace prefix.
     *
     * @param qName             qualified name
     * @param xDefName          X-Definition name
     * @param xdAdapterCtx      X-Definition adapter context
     * @return qualified name
     */
    public static String createQualifiedName(final QName qName, final String xDefName, final XdAdapterCtx xdAdapterCtx) {
        final String nsPrefix = xdAdapterCtx.findNamespacePrefix(xDefName, qName.getNamespaceURI())
                .orElse(NAMESPACE_PREFIX_EMPTY);

        if (NAMESPACE_PREFIX_EMPTY.equals(nsPrefix)) {
            return qName.getLocalPart();
        }

        return nsPrefix + NAMESPACE_DELIMITER + qName.getLocalPart();
    }

    /**
     * Creates string qualified name from given qualified name.
     * @param qName     qualified name
     * @return qualified name
     */
    public static String createQualifiedName(final QName qName) {
        final String nsPrefix = qName.getPrefix();
        if (nsPrefix == null || NAMESPACE_PREFIX_EMPTY.equals(nsPrefix)) {
            return qName.getLocalPart();
        }

        return nsPrefix + NAMESPACE_DELIMITER + qName.getLocalPart();
    }

    /**
     * Parses XML Schema document name from XML Schema document location
     * @param schemaLocation    XML Schema document location
     * @return XML Schema document name
     */
    public static String getSchemaName(final String schemaLocation) {
        Matcher matcher = XSD_NAME_PATTERN_2.matcher(schemaLocation);
        if (!matcher.matches()) {
            matcher = XSD_NAME_PATTERN_1.matcher(schemaLocation);
        }

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return schemaLocation;
    }
}
