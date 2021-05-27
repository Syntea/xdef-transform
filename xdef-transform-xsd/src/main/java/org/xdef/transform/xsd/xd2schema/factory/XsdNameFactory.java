package org.xdef.transform.xsd.xd2schema.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature.XSD_NAME_COLLISION_DETECTOR;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_NAME_FACTORY;

/**
 * Creates names of specific types of nodes.
 * Internally stores top-level nodes names.
 */
public class XsdNameFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XsdNameFactory.class);

    final private XsdAdapterCtx adapterCtx;

    /**
     * Real used names of XSD nodes. Used for finding real names of created XSD nodes.
     *
     * Key:     systemId
     * Value:   Key:    xDefPosition
     *          Value:  org.xdef.transform.xsd real name
     */
    private final Map<String, Map<String, String>> topLevelNameMap = new HashMap<>();

    /**
     * Base name of XSD nodes. Used for pairing and storing X-Definition nodes with same name
     * which are projected on top level XSD node.
     *
     * Key:     systemId
     * Value:   Key:    org.xdef.transform.xsd base name
     *          Value:  list of associated x-nodes
     */
    private final Map<String, Map<String, List<XMNode>>> topLevelBaseNameMap = new HashMap<>();

    public XsdNameFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Finds XSD top level element node name.
     * @param xElem     X-Definition element
     * @return  name if X-Definition element node with given path has been stored
     *          otherwise {@link Optional#empty()} or if {@link Xd2XsdFeature.XSD_NAME_COLLISION_DETECTOR} is disabled
     */
    public Optional<String> findTopLevelName(final XElement xElem) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLLISION_DETECTOR)) {
            return Optional.empty();
        }

        return findTopLevelNameByPath(xElem);
    }

    /**
     * Finds XSD top level simple-type node name
     * @param xData     X-Definition node (attribute/text)
     * @param usePath   flag if name should be search by X-Definition node path
     * @return  name if X-Definition node has been stored
     *          otherwise {@link Optional#empty()} or if {@link Xd2XsdFeature.XSD_NAME_COLLISION_DETECTOR} is disabled
     */
    public Optional<String> findTopLevelName(final XData xData, boolean usePath) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLLISION_DETECTOR)) {
            return Optional.empty();
        }

        if (usePath) {
            return findTopLevelNameByPath(xData);
        }

        LOG.debug("{}Finding top level simple-type name ...", logHeader(XSD_NAME_FACTORY, xData));

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xData.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);

        if (mapBaseName.containsKey(xData.getRefTypeName())) {
            return Optional.of(xData.getRefTypeName());
        }

        return Optional.empty();
    }

    /**
     * Finds XSD top level node name based on X-Definition path
     * @param xNode X-Definition node
     * @return  name if X-Definition node with given path has been stored
     *          otherwise {@link Optional#empty()}
     */
    private Optional<String> findTopLevelNameByPath(final XNode xNode) {
        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        LOG.debug("{}Finding top level " + nodeType + "-type name ...", logHeader(XSD_NAME_FACTORY, xNode));

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xNode.getXDPosition());
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);

        String realName = mapName.get(xNode.getXDPosition());
        if (realName != null) {
            return Optional.of(realName);
        }

        return Optional.empty();
    }

    /**
     * Creates new top level XSD element node name based on {@code baseName}
     * and current internal state of name storage.
     *
     * @param xElem         X-Definition element node
     * @param baseName      X-Definition element node base name
     * @return new name
     */
    public String generateTopLevelName(final XElement xElem, final String baseName) {
        return generateTopLevelName((XNode)xElem, baseName);
    }

    /**
     * Creates new top level XSD simple type node name based on {@code baseName}
     * and current internal state of name storage.
     *
     * @param xData         X-Definition node
     * @param baseName      X-Definition node base name
     * @return new name
     */
    public String generateTopLevelName(final XData xData, final String baseName) {
        return generateTopLevelName((XNode)xData, baseName);
    }

    /**
     * Creates new top level XSD node name based on {@code baseName}
     * and current internal state of name storage.
     *
     * @param xNode         X-Definition node
     * @param baseName      X-Definition node base name
     * @return new name
     */
    private String generateTopLevelName(final XNode xNode, final String baseName) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLLISION_DETECTOR) || baseName == null) {
            return baseName;
        }

        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        LOG.debug("{}Generating top level " + nodeType + "-type name ...", logHeader(XSD_NAME_FACTORY, xNode));

        final List<XMNode> nodeList = addNodeWithBaseName(xNode, baseName);
        String realName = baseName;

        if (nodeList.size() > 1) {
            realName += "_" + nodeList.size();
        }

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xNode.getXDPosition());
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);
        mapName.put(xNode.getXDPosition(), realName);

        LOG.info("{}Add top-level " + nodeType + "-type name. realName='{}', systemId='{}'",
                logHeader(XSD_NAME_FACTORY, xNode), realName, systemId);

        return realName;
    }

    /**
     * Saves XSD simple type node name to internal storage
     * @param xData     X-Definition node
     * @param baseName  X-Definition node base name
     */
    public void addTopSimpleTypeName(final XData xData, final String baseName) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLLISION_DETECTOR) || baseName == null) {
            return;
        }

        LOG.debug("{}Saving top level simple-type name ...", logHeader(XSD_NAME_FACTORY, xData));
        addNodeWithBaseName(xData, baseName);
    }

    /**
     * Get name storage map. If map does not exist, then creates and saves empty one.
     * @param systemId  XSD system identified
     * @return name storage map
     */
    private Map<String, String> getOrCreateTopLevelNameMap(final String systemId) {
        return topLevelNameMap.computeIfAbsent(systemId, key -> new HashMap<>());
    }

    /**
     * Get base name storage map. If map does not exist, then creates and saves empty one.
     * @param systemId  XSD system identified
     * @return base name storage map
     */
    private Map<String, List<XMNode>> getOrCreateTopLevelBaseNameMap(final String systemId) {
        return topLevelBaseNameMap.computeIfAbsent(systemId, key -> new HashMap<>());
    }

    /**
     * Get list of X-Definition nodes using same base name. If list does not exist, then creates and saves empty one.
     * @param mapBaseName   map of nodes using base name
     * @param baseName      required base name
     * @return list of X-Definition nodes
     */
    private List<XMNode> getOrCreateListNodesInBaseNameMap(final Map<String, List<XMNode>> mapBaseName, final String baseName) {
        return mapBaseName.computeIfAbsent(baseName, key -> new LinkedList<>());
    }

    /**
     * Saves X-Definition node base name.
     * @param xNode         X-Definition node
     * @param nodeBaseName  X-Definition node base name
     * @return list of X-Definition nodes using same {@code nodeBaseName}
     */
    private List<XMNode> addNodeWithBaseName(final XNode xNode, final String nodeBaseName) {
        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xNode.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);
        final List<XMNode> nodeList = getOrCreateListNodesInBaseNameMap(mapBaseName, nodeBaseName);

        nodeList.add(xNode);
        LOG.info("{}Add top-level " + nodeType + "-type base name. baseName='{}', systemId='{}'",
                logHeader(XSD_NAME_FACTORY, xNode), nodeBaseName, systemId);
        return nodeList;
    }

    /**
     * Creates new name of top level complex type
     * @param name  original complex type name
     * @return new name
     */
    public static String createComplexRefName(final String name) {
        return name;
    }

    /**
     * Creates new name of top level root element's schema type node
     * @param name          original element name
     * @param schemaType    schema type node
     * @return new name
     */
    public static String createRootElemName(final String name, final XmlSchemaType schemaType) {
        return createElementPrefix(schemaType) + "root_" + name;
    }

    /**
     * Creates new name of local simple type
     * @param xData         X-Definition node to be converted to XSD simple type node
     * @return new name
     */
    public static Optional<String> createLocalSimpleTypeName(final XData xData) {
        return Optional.ofNullable(xData.isLocalType() ?
                "refLoc_" + xData.getRefTypeName()
                : xData.getRefTypeName());
    }

    /**
     * Creates new name of union member reference
     * @param nodeName          node name where union is located
     * @param qNameLocal        local part of QName
     * @return new name
     */
    public static String createUnionRefTypeName(final String nodeName, final String qNameLocal) {
        return nodeName + "_union_" + qNameLocal;
    }

    /**
     * Creates new name of local simple type
     * @param nodeName          node name where text definition is located
     * @return new name
     */
    public static String createTextElemName(final String nodeName) {
        return nodeName + "_text";
    }

    /**
     * Creates element node prefix based on schema type
     * @param schemaType    input schema type
     * @return  "ct_" if input schema type is complex
     *          "st_" if input schema type is simple
     *          "" otherwise
     */
    private static String createElementPrefix(XmlSchemaType schemaType) {
        if (schemaType != null) {
            if (schemaType instanceof XmlSchemaComplexType) {
                return "ct_";
            } else {
                return "st_";
            }
        }

        return "";
    }

}
