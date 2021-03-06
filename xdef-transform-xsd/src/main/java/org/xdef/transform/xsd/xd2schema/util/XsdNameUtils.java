package org.xdef.transform.xsd.xd2schema.util;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.model.XMDefinition;
import org.xdef.transform.xsd.xd2schema.model.uc.impl.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.Optional;

import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_URI;
import static org.xdef.transform.xsd.def.XDefConst.XDEF_REF_DELIMITER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_ARGUMENT;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_FACET_FORMAT;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_PARSER_CDATA;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_UNIQUE_CHKID;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_UNIQUE_CHKIDS;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_UNIQUE_ID;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_UNIQUE_IDREF;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdDefinitions.XD_UNIQUE_IDREFS;
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
 * Utils related to working with node name, reference name and qualified name
 */
public class XsdNameUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XsdNameUtils.class);

    /**
     * Parse X-Definition reference node name from given X-Definition reference position
     * @param refPos    X-Definition reference position
     * @return X-Definition reference node name
     */
    public static String getReferenceName(final String refPos) {
        int xDefNamespaceSeparatorPos = refPos.indexOf(NAMESPACE_DELIMITER);
        if (xDefNamespaceSeparatorPos != -1) {
            return refPos.substring(xDefNamespaceSeparatorPos + 1);
        }

        int xDefSystemSeparatorPos = refPos.indexOf(XDEF_REF_DELIMITER);
        if (xDefSystemSeparatorPos != -1) {
            return refPos.substring(xDefSystemSeparatorPos + 1);
        }

        return refPos;
    }

    /**
     * Get X-Definition node position without X-Definition name
     * @param nodePos   X-Definition node position
     * @return  position without X-Definition name
     */
    public static String getXNodePath(final String nodePos) {
        int xDefSystemSeparatorPos = nodePos.indexOf(XDEF_REF_DELIMITER);
        if (xDefSystemSeparatorPos != -1) {
            return nodePos.substring(xDefSystemSeparatorPos + 1);
        }

        return nodePos;
    }

    /**
     * Parse X-Definition node name without target namespace (if using it)
     * @param schema    XML Schema document
     * @param name      X-Definition node name
     * @return  X-Definition node name
     */
    public static String resolveName(final XmlSchema schema, final String name) {
        // Element's name contains target namespace prefix, we can remove this prefix
        if (XsdNamespaceUtils.usingTargetNamespace(schema, name)) {
            return name.substring(schema.getSchemaNamespacePrefix().length() + 1);
        }

        return name;
    }

    /**
     * Resolve XML Schema attribute node name and schema form
     * @param schema    XML Schema document
     * @param attr      XML Schema attribute node
     * @param xName     X-Definition node name
     */
    public static void resolveAttributeQName(final XmlSchema schema, final XmlSchemaAttribute attr, final String xName) {
        if (attr.isRef()) {
            return;
        }

        if (attr.isTopLevel()) {
            attr.setName(getNodeNameWithoutPrefix(xName));
            return;
        }

        String newName = resolveName(schema, xName);
        if (!xName.equals(newName)) {
            attr.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault()) && isUnqualifiedName(schema, xName)) {
            attr.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }

    /**
     * Resolve XML Schema element node name and schema form
     * @param schema        XML Schema document
     * @param xElem         X-Definition element node
     * @param elem          XML Schema element node
     * @param adapterCtx    XML Schema adapter context
     */
    public static void resolveElementQName(final XmlSchema schema, final XElement xElem, final XmlSchemaElement elem, final XsdAdapterCtx adapterCtx) {
        if (elem.isRef()) {
            return;
        }

        if (elem.isTopLevel()) {
            final String name = adapterCtx.getNameFactory().findTopLevelName(xElem)
                    .orElseGet(() -> {
                        String generatedName = getNodeNameWithoutPrefix(elem.getName());
                        generatedName = adapterCtx.getNameFactory().generateTopLevelName(xElem, generatedName);
                        return generatedName;
                    });

            elem.setName(name);
            return;
        }

        final String name = elem.getName();
        final String newName = resolveName(schema, name);

        if (!name.equals(newName)) {
            elem.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault()) && isUnqualifiedName(schema, name)) {
            elem.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }

    /**
     * Resolve XML Schema attribute node type
     * @param schema        XML Schema document
     * @param xsdAttr       XML Schema attribute node
     */
    public static void resolveAttributeSchemaTypeQName(final XmlSchema schema, final XmlSchemaAttribute xsdAttr) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault())) {
            final QName schemaTypeName = xsdAttr.getSchemaTypeName();
            if (schemaTypeName != null && !XML_SCHEMA_DEFAULT_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())) {
                xsdAttr.setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    /**
     * Resolve XML Schema element node type
     * @param schema        XML Schema document
     * @param xsdElem       XML Schema attribute node
     */
    public static void resolveElementSchemaTypeQName(final XmlSchema schema, final XmlSchemaElement xsdElem) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault())) {
            final QName schemaTypeName = xsdElem.getSchemaTypeName();
            if (schemaTypeName != null && !XML_SCHEMA_DEFAULT_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())) {
                xsdElem.setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    /**
     * Check if X-Definition node name is not using namespace prefix while XML Schema document is using target namespace prefix
     * @param schema    XML Schema document
     * @param name      X-Definition node name
     * @return true if X-Definition node name is not using namespace prefix while XML Schema document yes
     */
    public static boolean isUnqualifiedName(final XmlSchema schema, final String name) {
        return !XsdNamespaceUtils.containsNsPrefix(name)
                && schema.getSchemaNamespacePrefix() != null
                && !NAMESPACE_PREFIX_EMPTY.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Parse X-Definition node name without prefix
     * @param nodeName  X-Definition node name
     * @return  X-Definition node name
     */
    public static String getNodeNameWithoutPrefix(final String nodeName) {
        int nsPos = nodeName.indexOf(NAMESPACE_DELIMITER);
        if (nsPos != -1) {
            return nodeName.substring(nsPos + 1);
        }

        return nodeName;
    }

    /**
     * Parse X-Definition element node name without X-Definition type
     * @param xElem     X-Definition element node
     * @return  X-Definition element node name
     */
    public static String getName(final XElement xElem) {
        int typeSepPos = xElem.getName().indexOf('$');
        if (typeSepPos <= 0) {
            return xElem.getName();
        }

        return xElem.getName().substring(0, typeSepPos);
    }

    /**
     * Parse X-Definition unique set variable name
     * @param varTypeName   X-Definition unique set variable type name
     * @return  X-Definition unique set variable name
     */
    public static String getUniqueSetVarName(final String varTypeName) {
        final int pos = varTypeName.lastIndexOf('.');
        if (pos != -1) {
            final String res = varTypeName.substring(pos + 1);
            if (XD_UNIQUE_ID.equals(res)
                    || XD_UNIQUE_IDREF.equals(res)
                    || XD_UNIQUE_IDREFS.equals(res)
                    || XD_UNIQUE_CHKID.equals(res)) {
                return getUniqueSetVarName(varTypeName.substring(0, pos));
            } else {
                return res;
            }
        }

        return "";
    }

    /**
     * Parse X-Definition unique set variable type
     * @param varTypeName   X-Definition unique set variable type name
     * @return  X-Definition unique set variable type
     */
    public static UniqueConstraint.Type getUniqueSetVarType(final String varTypeName) {
        UniqueConstraint.Type ucType = UniqueConstraint.Type.UNK;
        if (varTypeName.endsWith(XD_UNIQUE_CHKID)) {
            ucType = UniqueConstraint.Type.CHKID;
        } else if (varTypeName.endsWith(XD_UNIQUE_ID)) {
            ucType = UniqueConstraint.Type.ID;
        }else if (varTypeName.endsWith(XD_UNIQUE_CHKIDS)) {
            ucType = UniqueConstraint.Type.CHKIDS;
        } else if (varTypeName.endsWith(XD_UNIQUE_IDREF)) {
            ucType = UniqueConstraint.Type.IDREF;
        } else if (varTypeName.endsWith(XD_UNIQUE_IDREFS)) {
            ucType = UniqueConstraint.Type.IDREFS;
        }

        return ucType;
    }

    /**
     * Parse X-Definition unique set name
     * @param varTypeName   X-Definition unique set variable type
     * @return  X-Definition unique set name
     */
    public static String getUniqueSetName(final String varTypeName) {
        final int pos = varTypeName.indexOf('.');
        if (pos != -1) {
            return varTypeName.substring(0, pos);
        }

        return varTypeName;
    }

    /**
     * Creates reference name from given X-Definition node
     * @param xData         X-Definition node
     * @param adapterCtx    XML Schema adapter context
     * @return  reference name
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<String> createRefNameFromParser(final XData xData, final XsdAdapterCtx adapterCtx) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        final String name = Xd2XsdParserMapping.findDefaultParserQName(parserName, adapterCtx)
                .map(QName::getLocalPart)
                .orElse(parserName);

        if (!Constants.XSD_STRING.getLocalPart().equals(name)
                && !XD_PARSER_CDATA.equals(name)
                && !Constants.XSD_INT.getLocalPart().equals(name)
                && !Constants.XSD_LONG.getLocalPart().equals(name)) {
            return Optional.empty();
        }

        final StringBuilder sb = new StringBuilder(name);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            for (XDNamedValue p : parser.getNamedParams().getXDNamedItems()) {
                if (XSD_FACET_MAX_LENGTH.equals(p.getName())) {
                    sb.append("_maxl" + p.getValue().intValue());
                } else if (XSD_FACET_MIN_LENGTH.equals(p.getName())) {
                    sb.append("_minl" + p.getValue().intValue());
                } else if (XSD_FACET_WHITESPACE.equals(p.getName())) {
                    sb.append("_w");
                } else if (XSD_FACET_PATTERN.equals(p.getName()) || XD_FACET_FORMAT.equals(p.getName())) {
                    sb.append("_p");
                } else if (XSD_FACET_MIN_INCLUSIVE.equals(p.getName())) {
                    sb.append("_minI" + p.getValue().intValue());
                } else if (XSD_FACET_MIN_EXCLUSIVE.equals(p.getName())) {
                    sb.append("_minE" + p.getValue().intValue());
                } else if (XSD_FACET_MAX_INCLUSIVE.equals(p.getName())) {
                    sb.append("_maxI" + p.getValue().intValue());
                } else if (XSD_FACET_MAX_EXCLUSIVE.equals(p.getName())) {
                    sb.append("_maxE" + p.getValue().intValue());
                } else if (XD_FACET_ARGUMENT.equals(p.getName()) || XSD_FACET_ENUMERATION.equals(p.getName())) {
                    sb.append("_e");
                } else if (XSD_FACET_LENGTH.equals(p.getName())) {
                    sb.append("_l" + p.getValue().intValue());
                } else if (XSD_FACET_FRACTION_DIGITS.equals(p.getName())) {
                    sb.append("_fd");
                } else if (XSD_FACET_TOTAL_DIGITS.equals(p.getName())) {
                    sb.append("_td");
                }
            }
        }

        return Optional.of(sb.toString());
    }

    /**
     * Creates XML Schema name
     * @param xDef   X-Definition
     * @return  XML Schema name
     */
    public static String getSchemaName(final XMDefinition xDef) {
        if (xDef.getName() == null || xDef.getName().isEmpty()) {
            LOG.warn("{}Initialize XML Schema document - X-definition name is blank.", logHeader(INITIALIZATION, xDef));
            return "blank_name";
        }

        return xDef.getName();
    }

}
