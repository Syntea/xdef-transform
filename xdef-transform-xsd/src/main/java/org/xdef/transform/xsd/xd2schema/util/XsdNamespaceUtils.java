package org.xdef.transform.xsd.xd2schema.util;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.definition.AlgPhase;
import org.xdef.transform.xsd.xd2schema.error.XsdNamespaceException;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;

import java.util.Map;
import java.util.Optional;

import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_URI_EMPTY;
import static org.xdef.transform.xsd.NamespaceConst.XDEF_DEFAULT_NAMESPACE_PREFIX;
import static org.xdef.transform.xsd.XDefConst.XDEF_REF_DELIMITER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_UTILS;

/**
 * Utils related to working with namespaces
 */
public class XsdNamespaceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XsdNamespaceUtils.class);

    /**
     * Add new namespace info to namespace context
     * @param namespaceMap  namespace storage
     * @param nsPrefix      namespace prefix
     * @param nsUri         namespace URI
     * @param systemId      XML Schema document identifier (just for logging purposes)
     * @param phase         transformation algorithm phase (just for logging purposes)
     */
    public static void addNamespaceToCtx(final NamespaceMap namespaceMap,
                                         final String nsPrefix,
                                         final String nsUri,
                                         final String systemId,
                                         final AlgPhase phase) {
        namespaceMap.add(nsPrefix, nsUri);
        LOG.debug("{}Add namespace. nsPrefix='{}', nsUri='{}'", logHeader(phase, systemId), nsPrefix, nsUri);
    }

    /**
     * Checks if X-Definition node name is using different namespace than given XML Schema document (target namespace)
     * @param nodeName          X-Definition node name
     * @param namespaceUri      X-Definition node namespace URI
     * @param schema            target XML Schema document
     * @return  true is X-Definition node is inside different namespace
     */
    public static boolean isNodeInDifferentNamespace(final String nodeName,
                                                     final String namespaceUri,
                                                     final XmlSchema schema) {
        return containsNsPrefix(nodeName) && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    /**
     * Checks if node name is using different namespace prefix than given XML Schema document (target namespace)
     * @param xNode     X-Definition node
     * @param schema    target XML Schema document
     * @return  true if node name is using different namespace prefix
     */
    public static boolean isNodeInDifferentNamespacePrefix(final XMNode xNode, final XmlSchema schema) {
        final String nodeNsPrefix = getNamespacePrefix(xNode.getName()).orElse(null);
        return nodeNsPrefix != null && !nodeNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Checks if reference is using different namespace prefix than given XML Schema document (target namespace prefix)
     * @param nodeRefPos    X-Definition reference node position
     * @param schema        target XML Schema document
     * @return  true if reference is using different namespace prefix
     */
    public static boolean isRefInDifferentNamespacePrefix(final String nodeRefPos, final XmlSchema schema) {
        final String refNsPrefix = getReferenceNamespacePrefix(nodeRefPos);
        return !NAMESPACE_PREFIX_EMPTY.equals(refNsPrefix) && !refNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Checks if reference is in different X-Definition
     * @param nodeRefPos    X-Definition reference node position
     * @param xdPos         X-Definition source node position
     * @return  true if reference is in different X-Definition
     */
    public static boolean isRefInDifferentSystem(final String nodeRefPos, final String xdPos) {
        final String nodeSystemId = getSystemIdFromXPosRequired(xdPos);
        final String refSystemId = getSystemIdFromXPosRequired(nodeRefPos);
        return !nodeSystemId.equals(refSystemId);
    }

    /**
     * Checks if X-Definition node name contains prefix
     * @param name  X-Definition node name
     * @return  true if X-Definition node name contains prefix
     */
    public static boolean containsNsPrefix(final String name) {
        return name.contains(NAMESPACE_DELIMITER);
    }

    /**
     * Checks if X-Definition node name contains reference
     * @param name  X-Definition node name
     * @return  true if X-Definition node name contains reference
     */
    public static boolean containsReference(final String name) {
        return name.contains(XDEF_REF_DELIMITER);
    }

    /**
     * Parse X-Definition name (XML Schema system identifier) from given X-Definition node position
     * @param xPos  X-Definition node pos
     * @return  X-Definition name if it is part of name,
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<String> getSystemIdFromXPos(final String xPos) {
        String result = null;
        int systemSeparatorPos = xPos.indexOf(XDEF_REF_DELIMITER);
        if (systemSeparatorPos > 0) {
            result = xPos.substring(0, systemSeparatorPos);
        }

        return Optional.ofNullable(result);
    }

    /**
     * Parse X-Definition name (XML Schema system identifier) from given X-Definition node position
     * @param xPos  X-Definition node pos
     * @return X-Definition name if it is part of name
     * @throws XsdNamespaceException if X-Definition node pos does not contain name of X-definition
     */
    public static String getSystemIdFromXPosRequired(final String xPos) throws XsdNamespaceException {
        return getSystemIdFromXPos(xPos).orElseThrow(() ->
                new XsdNamespaceException("Given x-Definition position does not contain X-Definition name. xPos='{}'",
                        xPos));
    }

    /**
     * Parse namespace prefix from given X-Definition node name
     * @param name  X-Definition node name
     * @return  namespace prefix if it is part of name
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<String> getNamespacePrefix(final String name) {
        String result = null;
        int nsPos = name.indexOf(NAMESPACE_DELIMITER);
        if (nsPos != -1) {
            result = name.substring(0, nsPos);
        }

        return Optional.ofNullable(result);
    }

    /**
     * Parse namespace prefix from given X-Definition node name
     * @param name  X-Definition node name
     * @return namespace prefix if it is part of name
     * @throws XsdNamespaceException if name does not contain namespace prefix
     */
    public static String getNamespacePrefixRequired(final String name) throws XsdNamespaceException {
        return getNamespacePrefix(name).orElseThrow(() ->
                new XsdNamespaceException("Given name does not contains namespace prefix. name='{}'", name));
    }

    /**
     * Parse namespace prefix from given X-Definition reference node position
     * @param refPos    X-Definition reference node position
     * @return  namespace prefix if it is part of reference node position, otherwise empty string
     */
    public static String getReferenceNamespacePrefix(final String refPos) {
        int xdefNamespaceSeparatorPos = refPos.indexOf(NAMESPACE_DELIMITER);
        if (xdefNamespaceSeparatorPos == -1) {
            return NAMESPACE_PREFIX_EMPTY;
        }

        int xdefSystemSeparatorPos = refPos.indexOf(XDEF_REF_DELIMITER);
        if (xdefSystemSeparatorPos == -1) {
            return NAMESPACE_PREFIX_EMPTY;
        }

        return refPos.substring(xdefSystemSeparatorPos + 1, xdefNamespaceSeparatorPos);
    }

    /**
     * Checks if given namespace prefix is default for X-Definition
     * @param prefix    namespace prefix
     * @return  return true if if given namespace prefix is default
     */
    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XDEF_DEFAULT_NAMESPACE_PREFIX.equals(prefix);
    }

    /**
     * Checks if node name is using XML Schema document target namespace
     * @param schema    XML Schema document
     * @param name      X-Definition node name
     * @return true if node name is using XML Schema document target namespace
     */
    public static boolean usingTargetNamespace(final XmlSchema schema, final String name) {
        return schema.getSchemaNamespacePrefix() != null
                && name.startsWith(schema.getSchemaNamespacePrefix() + NAMESPACE_DELIMITER);
    }

    /**
     * Checks if given namespace URI is valid
     * @param uri   namespace URI
     * @return  true if given namespace URI is valid
     */
    public static boolean isValidNsUri(final String uri) {
        return uri != null && !uri.isEmpty();
    }

    /**
     * Determines target namespace of given X-Definition
     * @param xDef  X-Definition
     * @return target namespace prefix, target namespace URI
     */
    public static Pair<String, String> getSchemaTargetNamespace(final XDefinition xDef, final XsdAdapterCtx adapterCtx) {
        String targetNamespacePrefix = null;
        String targetNamespaceUri = null;
        boolean targetNamespaceError = false;
        // Get target namespace prefix based on root elements
        if (xDef._rootSelection != null) {
            for (Map.Entry<String, XNode> root : xDef._rootSelection.entrySet()) {
                final String rootName = root.getKey();
                String tmpNsPrefix = getNamespacePrefix(rootName).orElse(null);
                if (targetNamespacePrefix == null) {
                    targetNamespacePrefix = tmpNsPrefix;
                } else if (tmpNsPrefix != null && !targetNamespacePrefix.equals(tmpNsPrefix)) {
                    adapterCtx.getReportWriter().error(XSD.XSD001, targetNamespacePrefix, tmpNsPrefix);
                    LOG.error("{}Expected different namespace prefix. expectedTargetNsPrefix='{}', currTargetNsPrefix='{}'",
                            logHeader(XSD_UTILS, xDef), targetNamespacePrefix, tmpNsPrefix);
                    targetNamespaceError = true;
                    break;
                }
            }
        }

        // Find target namespace URI based on X-Definition namespaces
        if (targetNamespacePrefix != null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if (targetNamespacePrefix.equals(entry.getKey())) {
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        if (targetNamespacePrefix != null && targetNamespaceUri == null) {
            adapterCtx.getReportWriter().error(XSD.XSD046, targetNamespacePrefix);
            LOG.error("{}Target namespace URI has been not found for prefix. targetNsPrefix='{}'",
                    logHeader(XSD_UTILS, xDef), targetNamespacePrefix);
            targetNamespaceError = true;
        }

        if (targetNamespaceError) {
            return Pair.of(targetNamespacePrefix, targetNamespaceUri);
        }

        // Try to find default namespace
        if (targetNamespacePrefix == null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if (NAMESPACE_URI_EMPTY.equals(entry.getKey())) {
                    targetNamespacePrefix = entry.getKey();
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        return Pair.of(targetNamespacePrefix, targetNamespaceUri);
    }

    /**
     * Creates namespace URI based on X-Definition name
     * @param name  X-Definition name
     * @return  namespace URI
     */
    public static String createNsUriFromXDefName(final String name) {
        return name;
    }

    /**
     * Creates XML Schema document name based on namespace prefix
     * @param nsPrefix  namespace prefix
     * @return  XML Schema name
     */
    public static String createExtraSchemaNameFromNsPrefix(final String nsPrefix) {
        return "external_" + nsPrefix;
    }

    /**
     * Parse namespace prefix from XML Schema document name
     * @param schemaName    XML Schema document name
     * @return namespace prefix
     */
    public static String getNsPrefixFromExtraSchemaName(final String schemaName) {
        int pos = schemaName.lastIndexOf('_');
        if (pos != -1) {
            return schemaName.substring(pos + 1);
        }

        return schemaName;
    }

    /**
     * Determines X-Definition node namespace URI of given X-Definition node based on XML Schema adapter context
     * @param xData         X-Definition node
     * @param adapterCtx    XML Schema adapter context
     * @param phase         transformation algorithm phase (just for logging purposes)
     * @return namespace URI
     */
    public static String getNodeNamespaceUri(final XNode xData, final XsdAdapterCtx adapterCtx, final AlgPhase phase) {
        final String xDefPos = xData.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xDefPos);
        final XmlSchema refSchema = adapterCtx.findSchemaReq(systemId, phase);
        final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
        return refSchema.getNamespaceContext().getNamespaceURI(nsPrefix);
    }

}
