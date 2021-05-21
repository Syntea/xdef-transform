package org.xdef.transform.xsd.xd2schema.model;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XData;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.xd2schema.definition.AlgPhase;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.XsdNameFactory;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_ADAPTER_CTX;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_REFERENCE;

/**
 * Basic XSD context for transformation x-definition to XSD document
 */
public class XsdAdapterCtx {

    private static final Logger LOG = LoggerFactory.getLogger(XsdAdapterCtx.class);

    /**
     * Names of created XSD documents
     */
    private Set<String> schemaNames = null;

    /**
     * Schemas location based on x-definition
     */
    private SchemaNamespaceLocationMap schemaLocationsCtx = null;

    /**
     * Schemas locations which are created in post-processing
     */
    private SchemaNamespaceLocationMap extraSchemaLocationsCtx = null;

    /**
     * Collection of created XSD documents
     */
    private XmlSchemaCollection xmlSchemaCollection = null;

    /**
     * Element/attributes nodes per schema
     * Key:     schema name
     * Value:   node path, schema node
     */
    private Map<String, Map<String, SchemaNode>> nodes = null;

    /**
     * Nodes which will be created in post-procession
     * Key:     namespace URI
     * Value:
     *          Value: node name
     *          Key: x-definition node
     */
    private Map<String, Map<String, XNode>> nodesToBePostProcessed;

    /**
     * Names of nodes which can be root of x-definitions
     * Key:     x-definition name
     * Value:   set of node names
     */
    private Map<String, Set<String>> rootNodeNames = null;

    /**
     * Nodes which will be created in post-procession
     * Key:     schema name
     * Value:   xpath to uniqueSet, unique info
     */
    private Map<String, Map<String, List<UniqueConstraint>>> uniqueRestrictions;

    private XsdNameFactory nameFactory;

    /**
     * Enabled algorithm features
     */
    final private Set<Xd2XsdFeature> features;

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;

    public XsdAdapterCtx(Set<Xd2XsdFeature> features, ReportWriter reportWriter) {
        this.features = features;
        this.reportWriter = reportWriter;
    }

    /**
     * Initializes XSD adapter context
     */
    public void init() {
        schemaNames = new HashSet<>();
        schemaLocationsCtx = new DefaultSchemaNamespaceLocationMap(reportWriter, "schemaLocations");
        extraSchemaLocationsCtx = new DefaultSchemaNamespaceLocationMap(reportWriter, "extraSchemaLocations");
        xmlSchemaCollection = new XmlSchemaCollection();
        nodes = new HashMap<>();
        nodesToBePostProcessed = new HashMap<>();
        rootNodeNames = new HashMap<>();
        uniqueRestrictions = new HashMap<>();
        nameFactory = new XsdNameFactory(this);
    }

    public Set<String> getSchemaNames() {
        return schemaNames;
    }

    public SchemaNamespaceLocationMap getExtraSchemaLocationsCtx() {
        return extraSchemaLocationsCtx;
    }

    public XmlSchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }

    public Map<String, Map<String, SchemaNode>> getNodes() {
        return nodes;
    }

    public Map<String, Map<String, XNode>> getNodesToBePostProcessed() {
        return nodesToBePostProcessed;
    }

    public XsdNameFactory getNameFactory() {
        return nameFactory;
    }

    public ReportWriter getReportWriter() {
        return reportWriter;
    }

    /**
     * Add XSD document name to name set
     * @param name  x-definition name
     */
    public void addSchemaName(final String name) throws SRuntimeException {
        if (!schemaNames.add(name)) {
            LOG.error("{}Schema with given name already processed! schemaName='{}'", logHeader(XSD_ADAPTER_CTX), name);
            throw new SRuntimeException(XSD.XSD005);
        }
    }

    /**
     * Add XSD document location into map
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public void addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        schemaLocationsCtx.addSchemaLocation(nsUri, importLocation);
    }

    /**
     * Check if schema with given namespace URI exists
     * @param nsUri     XSD document namespace URI
     * @return true if schema exists
     */
    public boolean existsSchemaLocation(final String nsUri, final String xsdName) {
        return schemaLocationsCtx.findSchemaImport(nsUri, xsdName).isPresent();
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public Optional<XsdSchemaImportLocation> findSchemaLocation(final String nsUri, final String xsdName) {
        return schemaLocationsCtx.findSchemaImport(nsUri, xsdName);
    }

    /**
     * Finds XSD document locations if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public List<XsdSchemaImportLocation> findSchemaLocations(final String nsUri) {
        return schemaLocationsCtx.findSchemaImports(nsUri);
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public Optional<XsdSchemaImportLocation> findPostProcessingSchemaLocation(final String nsUri, final String schemaName) {
        return extraSchemaLocationsCtx.findSchemaImport(nsUri, schemaName);
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public List<XsdSchemaImportLocation> findPostProcessingSchemaLocations(final String nsUri) {
        return extraSchemaLocationsCtx.findSchemaImports(nsUri);
    }

    /**
     * Add XSD document into extra map. Internally creates document location.
     * @param nsPrefix          XSD document namespace prefix
     * @param nsUri             XSD document namespace URI
     */
    public XsdSchemaImportLocation addExtraSchemaLocation(final String nsPrefix, final String nsUri) {
        return extraSchemaLocationsCtx.addSchemaLocation(nsPrefix, nsUri);
    }

    /**
     * Add XSD document into extra map. Internally creates document location.
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public void addExtraSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        extraSchemaLocationsCtx.addSchemaLocation(nsUri, importLocation);
    }

    /**
     * Check if XSD document with given namespace URI should be created in post-processing
     * @param nsUri     XSD document namespace URI
     * @return true if XSD document should be created in post-processing
     */
    public boolean isPostProcessingNamespace(final String nsUri) {
        return extraSchemaLocationsCtx.containsSchemaFileLocationMap(nsUri);
    }

    /**
     * Mark x-definition node to be converted in post-processing phase
     * @param nsUri     XSD document namespace URI
     * @param xNode     X-definition node
     */
    public void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        LOG.info("{}Add node to post-processing.", logHeader(TRANSFORMATION, xNode));

        final String nodeName = xNode.getName();
        Map<String, XNode> ppNsNodes = nodesToBePostProcessed.get(nsUri);

        if (ppNsNodes == null) {
            ppNsNodes = new HashMap<String, XNode>();
            nodesToBePostProcessed.put(nsUri, ppNsNodes);
        }

        if (ppNsNodes.containsKey(nodeName)) {
            LOG.info("{}Node is already marked for post-processing.", logHeader(TRANSFORMATION, xNode));
        } else {
            ppNsNodes.put(nodeName, xNode);
        }
    }

    /**
     * Finds XSD document by given system identifier.
     *
     * Throws exception if {@paramref shouldExists} value is true and XSD document does not exist
     * @param systemId      XSD document system identifier
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document if exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public XmlSchema findSchema(final String systemId, boolean shouldExists, final AlgPhase phase) {
        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(systemId);
        if (schemas == null || schemas.length == 0) {
            if (shouldExists == true) {
                reportWriter.error(XSD.XSD037, systemId);
                throw new SRuntimeException(XSD.XSD007, systemId);
            }

            return null;
        }

        if (schemas.length > 1) {
            reportWriter.warning(XSD.XSD038, systemId);
            LOG.warn("{}Multiple schemas with required name have been found! schemaName='{}'",
                    logHeader(phase), systemId);
        }

        return schemas[0];
    }

    /**
     * Finds XSD document name by given namespace URI
     * @param nsUri         XSD document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document name if XSD document exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public Set<String> findSchemaNamesByNamespace(final String nsUri, boolean shouldExists, final AlgPhase phase) {
        LOG.debug("{}Finding schema names by namespace. nsUri='{}', shouldExists={}",
                logHeader(phase), nsUri, shouldExists);

        final Set<String> schemaNames = Stream.of(
                schemaLocationsCtx.findSchemaFileLocationMap(nsUri),
                extraSchemaLocationsCtx.findSchemaFileLocationMap(nsUri)
        ).filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(schemaLocations -> schemaLocations.getSchemaFileNames())
                .orElse(null);

        if (schemaNames == null && shouldExists) {
            reportWriter.warning(XSD.XSD039, nsUri);
            throw new SRuntimeException(XSD.XSD009, nsUri);
        }

        return schemaNames;
    }

    /**
     * Finds XSD document name by given namespace URI
     * @param nsUri         XSD document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document name if XSD document exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public boolean hasSchemaNameWithNamespaceAndName(final String nsUri, final String schemaName, boolean shouldExists, final AlgPhase phase) {
        final Set<String> schemaNames = findSchemaNamesByNamespace(nsUri, shouldExists, phase);
        if (schemaNames == null || schemaNames.isEmpty()) {
            return false;
        }

        return schemaNames.contains(schemaName);
    }

    /**
     * Add new or update already existing schema node into XSD document defined by namespace of input {@paramref node}
     * @param node  schema node to be added
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNode(final SchemaNode node) {
        final String xPos = node.getXdNodeReq().getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XSD document defined by {@paramref systemId}
     * @param node      schema node to be added
     * @param systemId  XSD document identifier
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNodeInDiffNs(final SchemaNode node, final String systemId) {
        final String xPos = node.getXdNodeReq().getXDPosition();
        final String nodePath = SchemaNode.getPostProcessingReferenceNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XSD document defined by {@paramref systemId} and {@paramref nodePath}
     * @param systemId  XSD document identifier
     * @param nodePath  x-definition node path
     * @param node      schema node to be added
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNode(final String systemId, final String nodePath, final SchemaNode node) {
        Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);

        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode().isPresent()) {
            LOG.debug("{}Node with this name is already defined. system='{}', nodePath='{}', nodePos='{}'",
                    logHeader(XSD_REFERENCE), systemId, nodePath, node.getXdPosition());
            return node;
        }

        final StringBuilder sb = new StringBuilder();
        if (refOrig != null) {
            refOrig.copyNodes(node);

            sb.append("Updating node");
            if (node.hasReference()) {
                sb.append(" (with reference)");
            }
            sb.append(".");

            sb.append(StringFormatter.format("systemId='{}', nodeXDefPath='{}', nodeXDefPos='{}'",
                    systemId, nodePath, node.getXdPosition()));

            node.getXsdNode().ifPresent(xmlNode -> sb.append(StringFormatter.format(", Xsd='{}'",
                    node.getXsdNode().get().getClass().getSimpleName())));

            LOG.info("{}{}", logHeader(XSD_REFERENCE), sb);
            return refOrig;
        } else {
            xsdSystemRefs.put(nodePath, node);

            sb.append("Creating node");
            if (node.hasReference()) {
                sb.append(" (with reference)");
            }
            sb.append(".");

            sb.append(StringFormatter.format("systemId='{}', nodeXDefPath='{}', nodeXDefPos='{}'",
                    systemId, nodePath, node.getXdPosition()));

            node.getXsdNode().ifPresent(xmlNode -> sb.append(StringFormatter.format(", Xsd='{}'",
                    node.getXsdNode().get().getClass().getSimpleName())));

            LOG.info("{}{}", logHeader(XSD_REFERENCE), sb);
            return node;
        }
    }

    /**
     * Updates XSD node of schema node defined by x-definition node {@paramref xNode}
     * @param xNode         x-definition node of schema node
     * @param newXsdNode    new XSD document node
     */
    public void updateNode(final XMNode xNode, final XmlSchemaNamed newXsdNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        updateNode(systemId, nodePath, newXsdNode);
    }

    private void updateNode(final String systemId, String nodePath, final XmlSchemaNamed newXsdNode) {
        LOG.info("{}Updating xsd content of node. system='{}', nodePath='{}', newXsdName='{}'",
                logHeader(XSD_REFERENCE), systemId, nodePath, newXsdNode.getClass().getSimpleName());

        final Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);

        if (refOrig == null) {
            reportWriter.warning(XSD.XSD040, systemId, nodePath);
            LOG.warn("{}Node does not exist in system! system='{}', nodePath='{}'",
                    logHeader(XSD_REFERENCE), systemId, nodePath);
            return;
        }

        refOrig.setXsdNode(newXsdNode);
    }

    /**
     * Finds all created schema nodes in XSD document
     * @param systemId  XSD document identifier
     * @return  map of schema nodes
     */
    public Map<String, SchemaNode> findSchemaNodes(final String systemId) {
        Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            xsdSystemRefs = new HashMap<String, SchemaNode>();
            nodes.put(systemId, xsdSystemRefs);
        }

        return xsdSystemRefs;
    }

    /**
     * Finds schema node defined by {@paramref systemId} and {@paramref nodePath}
     * @param systemId  XSD document identifier
     * @param nodePath  x-definition path
     * @return  schema node if exists, otherwise null
     */
    public SchemaNode findSchemaNode(final String systemId, final String nodePath) {
        final Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            return null;
        }

        return xsdSystemRefs.get(nodePath);
    }

    /**
     * Deletes created schema node defined by x-definition node {@paramref xNode}
     * @param xNode     x-definition node
     */
    public void removeNode(final XNode xNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        removeNode(systemId, nodePath);
    }

    /**
     * Deletes created schema node defined by XSD document identifier and node path
     * @param systemId      XSD document identifier
     * @param nodePath      x-definition node path
     */
    private void removeNode(final String systemId, final String nodePath) {
        LOG.info("{}Removing xsd node. system='{}', nodePath='{}'", logHeader(XSD_REFERENCE), systemId, nodePath);

        final Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.remove(nodePath);
        if (refOrig != null) {
            LOG.debug("{}Node has been removed! system='{}', nodePath='{}', nodeName='{}'",
                    logHeader(XSD_REFERENCE), systemId, nodePath, refOrig.getXdName());
        }
    }

    /**
     * Finds XSD document root node's names by XSD document identifier
     * @param systemId      XSD document identifier
     * @return XSD document root node's names if exist, otherwise null
     */
    public Set<String> findSchemaRootNodeNames(final String systemId) {
        return rootNodeNames.get(systemId);
    }

    /**
     * Add XSD document root node name
     * @param systemId      XSD document identifier
     * @param nodeName      x-definition name
     */
    public void addRootNodeName(final String systemId, final String nodeName) {
        Set<String> schemaRootNodeNames = findSchemaRootNodeNames(systemId);
        if (schemaRootNodeNames == null) {
            schemaRootNodeNames = new HashSet<String>();
            rootNodeNames.put(systemId, schemaRootNodeNames);
        }

        schemaRootNodeNames.add(nodeName);
    }

    /**
     * Check if algorithm feature is enabled
     * @param feature       algorithm feature
     * @return  true if algorithm feature is enabled
     */
    public boolean hasEnableFeature(final Xd2XsdFeature feature) {
        return features.contains(feature);
    }

    /**
     * Creates and saves unique constraint based on input parameters if does not already exist
     * @param name      unique constraint name
     * @param systemId  XSD document identifier (can be nullable)
     * @param path      unique constraint path
     * @return Created or found unique constraint
     */
    public UniqueConstraint addOrGetUniqueConst(final String name, @Nullable String systemId, String path) {
        if (systemId == null) {
            systemId = "";
        }

        final Map<String, List<UniqueConstraint>> uniqueInfoMap = getOrCreateSchemaUniqueInfo(systemId);
        if (!path.isEmpty()) {
            path = "/" + path;
        }

        UniqueConstraint uniqueConstraint = findUniqueConstraint(uniqueInfoMap, name, path);

        if (uniqueConstraint == null) {
            LOG.info("{}Creating unique set. name='{}', path='{}', system='{}'",
                    logHeader(PREPROCESSING), name, path, systemId);

            List<UniqueConstraint> uniqueInfoList = uniqueInfoMap.get(path);
            if (uniqueInfoList == null) {
                uniqueInfoList = new LinkedList<UniqueConstraint>();
                uniqueInfoMap.put(path, uniqueInfoList);
            }

            uniqueConstraint = new UniqueConstraint(name, systemId);
            uniqueInfoList.add(uniqueConstraint);
        } else {
            LOG.debug("{}Creating unique set - already exists. name='{}', path='{}', system='{}'",
                    logHeader(PREPROCESSING), name, path, systemId);
        }

        return uniqueConstraint;
    }

    /**
     * Finds unique constraint
     * @param uniqueConstraintMap   unique constraint map
     * @param name                  unique constraint name
     * @param path                  unique constraint path
     * @return unique constraint if exists inside given map, otherwise null
     */
    private static UniqueConstraint findUniqueConstraint(final Map<String, List<UniqueConstraint>> uniqueConstraintMap, final String name, final String path) {
        if (!uniqueConstraintMap.isEmpty()) {
            final List<UniqueConstraint> uniqueInfoList = uniqueConstraintMap.get(path);
            if (uniqueInfoList != null && !uniqueInfoList.isEmpty()) {
                for (UniqueConstraint u : uniqueInfoList) {
                    if (u.getName().equals(name)) {
                        return u;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds unique constraint by x-definition node
     * @param xData x-definition node
     * @return  unique constraint if exists, otherwise null
     */
    public UniqueConstraint findUniqueConst(final XData xData) {
        LOG.debug("{}Finding unique set. name='{}'", logHeader(TRANSFORMATION, xData), xData.getValueTypeName());

        // TODO: Finding of unique set not using variable name, ie. uniqueSet u int();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xData.getXDPosition());
        final String uniqueInfoName = XsdNameUtils.getUniqueSetName(xData.getValueTypeName());
        final String uniquestSetPath = "/" + XsdNameUtils.getXNodePath(xData.getXDPosition());
        UniqueConstraint uniqueInfo = findUniqueConst(uniqueInfoName, systemId, uniquestSetPath);
        if (uniqueInfo == null) {
            uniqueInfo = findUniqueConst(uniqueInfoName, "", uniquestSetPath);
        }
        return uniqueInfo;
    }

    /**
     * Finds unique constrain in tree of unique constraints.
     * Iterates through tree, starting at {@paramref uniquestSetPath} path and going to root of XML tree
     * @param uniqueInfoName        unique constraint name
     * @param systemId              XSD document identifier
     * @param uniquestSetPath       unique constraint path
     * @return unique constraint if exists inside given map, otherwise null
     */
    private UniqueConstraint findUniqueConst(final String uniqueInfoName, final String systemId, String uniquestSetPath) {
        LOG.debug("{}Finding unique set. uniqueName='{}', systemId='{}'",
                logHeader(TRANSFORMATION), uniqueInfoName, systemId);

        UniqueConstraint uniqueInfo = null;
        int slashPos;
        final Map<String, List<UniqueConstraint>> uniqueInfoMap = getOrCreateSchemaUniqueInfo(systemId);

        if (!uniqueInfoMap.isEmpty()) {
            while (uniqueInfo == null && !"".equals(uniquestSetPath)) {
                slashPos = uniquestSetPath.lastIndexOf('/');
                if (slashPos == -1) {
                    uniquestSetPath = "";
                } else {
                    uniquestSetPath = uniquestSetPath.substring(0, slashPos);
                }
                uniqueInfo = findUniqueConstraint(uniqueInfoMap, uniqueInfoName, uniquestSetPath);
            }
        }

        return uniqueInfo;
    }

    /**
     * Get all created unique constraints created in XSD document
     * @param systemId  XSD document identifier
     * @return  unique constraints
     */
    public Map<String, List<UniqueConstraint>> getSchemaUniqueConstraints(final String systemId) {
        return uniqueRestrictions.get(systemId);
    }

    /**
     * Create or get unique constraints map in givet XSD document
     * @param systemId  XSD document identifier
     * @return  unique constraints map
     */
    private Map<String, List<UniqueConstraint>> getOrCreateSchemaUniqueInfo(final String systemId) {
        Map<String, List<UniqueConstraint>> uniqueInfo = uniqueRestrictions.get(systemId);
        if (uniqueInfo == null) {
            uniqueInfo = new HashMap<String, List<UniqueConstraint>>();
            uniqueRestrictions.put(systemId, uniqueInfo);
        }

        return uniqueInfo;
    }


}
