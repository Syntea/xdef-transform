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
     * @param systemId      XSD document identifier (just for logging purposes)
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
     * Checks if x-definition node name is using different namespace than given XSD document (target namespace)
     * @param nodeName          x-definition node name
     * @param namespaceUri      x-definition node namespace URI
     * @param schema            target XSD document
     * @return  true is x-definition node is inside different namespace
     */
    public static boolean isNodeInDifferentNamespace(final String nodeName,
                                                     final String namespaceUri,
                                                     final XmlSchema schema) {
        return containsNsPrefix(nodeName) && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    /**
     * Checks if node name is using different namespace prefix than given XSD document (target namespace)
     * @param xNode     x-definition node
     * @param schema    target XSD document
     * @return  true if node name is using different namespace prefix
     */
    public static boolean isNodeInDifferentNamespacePrefix(final XMNode xNode, final XmlSchema schema) {
        final String nodeNsPrefix = getNamespacePrefix(xNode.getName()).orElse(null);
        return nodeNsPrefix != null && !nodeNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Checks if reference is using different namespace prefix than given XSD document (target namespace prefix)
     * @param nodeRefPos    x-definition reference node position
     * @param schema        target XSD document
     * @return  true if reference is using different namespace prefix
     */
    public static boolean isRefInDifferentNamespacePrefix(final String nodeRefPos, final XmlSchema schema) {
        final String refNsPrefix = getReferenceNamespacePrefix(nodeRefPos);
        return !NAMESPACE_PREFIX_EMPTY.equals(refNsPrefix) && !refNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Checks if reference is in different x-definition
     * @param nodeRefPos    x-definition reference node position
     * @param xdPos         x-definition source node position
     * @return  true if reference is in different x-definition
     */
    public static boolean isRefInDifferentSystem(final String nodeRefPos, final String xdPos) {
        final String nodeSystemId = getSystemIdFromXPosRequired(xdPos);
        final String refSystemId = getSystemIdFromXPosRequired(nodeRefPos);
        return !nodeSystemId.equals(refSystemId);
    }

    /**
     * Checks if x-definition node name contains prefix
     * @param name  x-definition node name
     * @return  true if x-definition node name contains prefix
     */
    public static boolean containsNsPrefix(final String name) {
        return name.contains(NAMESPACE_DELIMITER);
    }

    /**
     * Checks if x-definition node name contains reference
     * @param name  x-definition node name
     * @return  true if x-definition node name contains reference
     */
    public static boolean containsReference(final String name) {
        return name.contains(XDEF_REF_DELIMITER);
    }

    /**
     * Parse x-definition name (XSD system identifier) from given x-definition node position
     * @param xPos  x-definition node pos
     * @return  x-definition name if it is part of name,
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
     * Parse x-definition name (XSD system identifier) from given x-definition node position
     * @param xPos  x-definition node pos
     * @return x-definition name if it is part of name
     * @throws XsdNamespaceException if x-definition node pos does not contain name of X-definition
     */
    public static String getSystemIdFromXPosRequired(final String xPos) throws XsdNamespaceException {
        return getSystemIdFromXPos(xPos).orElseThrow(() ->
                new XsdNamespaceException("Given x-Definition position does not contain x-definition name. xPos='{}'",
                        xPos));
    }

    /**
     * Parse namespace prefix from given x-definition node name
     * @param name  x-definition node name
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
     * Parse namespace prefix from given x-definition node name
     * @param name  x-definition node name
     * @return namespace prefix if it is part of name
     * @throws XsdNamespaceException if name does not contain namespace prefix
     */
    public static String getNamespacePrefixRequired(final String name) throws XsdNamespaceException {
        return getNamespacePrefix(name).orElseThrow(() ->
                new XsdNamespaceException("Given name does not contains namespace prefix. name='{}'", name));
    }

    /**
     * Parse namespace prefix from given x-definition reference node position
     * @param refPos    x-definition reference node position
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
     * Checks if given namespace prefix is default for x-definition
     * @param prefix    namespace prefix
     * @return  return true if if given namespace prefix is default
     */
    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XDEF_DEFAULT_NAMESPACE_PREFIX.equals(prefix);
    }

    /**
     * Checks if node name is using XSD document target namespace
     * @param schema    XSD document
     * @param name      x-definition node name
     * @return true if node name is using XSD document target namespace
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
     * Determines target namespace of given x-definition
     * @param xDef  x-definition
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

        // Find target namespace URI based on x-definition namespaces
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
     * Creates namespace URI based on x-definition name
     * @param name  x-definition name
     * @return  namespace URI
     */
    public static String createNsUriFromXDefName(final String name) {
        return name;
    }

    /**
     * Creates XSD document name based on namespace prefix
     * @param nsPrefix  namespace prefix
     * @return  XML schema name
     */
    public static String createExtraSchemaNameFromNsPrefix(final String nsPrefix) {
        return "external_" + nsPrefix;
    }

    /**
     * Parse namespace prefix from XSD document name
     * @param schemaName    XSD document name
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
     * Determines x-definition node namespace URI of given x-definition node based on XSD adapter context
     * @param xData         x-definition node
     * @param adapterCtx    XSD adapter context
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
