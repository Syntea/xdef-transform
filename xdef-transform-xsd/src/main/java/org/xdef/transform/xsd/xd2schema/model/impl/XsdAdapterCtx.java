package org.xdef.transform.xsd.xd2schema.model.impl;

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
import org.xdef.transform.xsd.model.impl.OptionalExt;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.xd2schema.def.AlgPhase;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.XsdNameFactory;
import org.xdef.transform.xsd.xd2schema.model.PostProcessXDefNodeMap;
import org.xdef.transform.xsd.xd2schema.model.SchemaFileNameLocationMap;
import org.xdef.transform.xsd.xd2schema.model.SchemaNamespaceLocationMap;
import org.xdef.transform.xsd.xd2schema.model.XmlSchemaNodeMap;
import org.xdef.transform.xsd.xd2schema.model.XmlSchemaUniqueConstraintMap;
import org.xdef.transform.xsd.xd2schema.model.uc.impl.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdLogGroup.XSD_ADAPTER_CTX;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdLogGroup.XSD_REFERENCE;

/**
 * Basic XML Schema context for transformation X-Definition to XML Schema document
 */
public class XsdAdapterCtx {

    private static final Logger LOG = LoggerFactory.getLogger(XsdAdapterCtx.class);

    /**
     * Names of created XML Schema documents
     */
    private Set<String> schemaNames = null;

    /**
     * Schemas location based on X-Definition
     */
    private SchemaNamespaceLocationMap schemaLocationsCtx = null;

    /**
     * Schemas locations which are created in post-processing
     */
    private SchemaNamespaceLocationMap extraSchemaLocationsCtx = null;

    /**
     * Collection of created XML Schema documents
     */
    private XmlSchemaCollection xmlSchemaCollection = null;

    /**
     * Element/attributes nodes per schema
     */
    private XmlSchemaNodeMap xmlSchemaNodeMap = null;

    /**
     * Nodes which will be created in post-procession
     */
    private PostProcessXDefNodeMap processXDefNodeMap;

    /**
     * Names of nodes which can be root of x-definitions
     * Key:     X-Definition name
     * Value:   set of node names
     */
    private Map<String, Set<String>> rootNodeNames = null;

    /**
     * Unique constraint nodes which will be created in post-procession
     */
    private XmlSchemaUniqueConstraintMap xmlSchemaUniqueConstraintMap;

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
     * Initializes XML Schema adapter context
     */
    public void init() {
        schemaNames = new HashSet<>();
        schemaLocationsCtx = new DefaultSchemaNamespaceLocationMap(reportWriter, "schemaLocations");
        extraSchemaLocationsCtx = new DefaultSchemaNamespaceLocationMap(reportWriter, "extraSchemaLocations");
        xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchemaNodeMap = new DefaultXmlSchemaNodeMap();
        processXDefNodeMap = new DefaultPostProcessXDefNodeMap();
        rootNodeNames = new HashMap<>();
        xmlSchemaUniqueConstraintMap = new DefaultXmlSchemaUniqueConstraintMap();
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

    public XmlSchemaNodeMap getXmlSchemaNodeMap() {
        return xmlSchemaNodeMap;
    }

    public PostProcessXDefNodeMap getProcessXDefNodeMap() {
        return processXDefNodeMap;
    }

    public XsdNameFactory getNameFactory() {
        return nameFactory;
    }

    public ReportWriter getReportWriter() {
        return reportWriter;
    }

    /**
     * Add XML Schema document name to name set
     * @param name  X-Definition name
     */
    public void addSchemaName(final String name) throws SRuntimeException {
        if (!schemaNames.add(name)) {
            LOG.error("{}Schema with given name already processed! schemaName='{}'", logHeader(XSD_ADAPTER_CTX), name);
            throw new SRuntimeException(XSD.XSD005);
        }
    }

    /**
     * Add XML Schema document location into map
     * @param nsUri             XML Schema document namespace URI
     * @param importLocation    XML Schema document location definition
     */
    public void addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        schemaLocationsCtx.addSchemaLocation(nsUri, importLocation);
    }

    /**
     * Check if schema with given namespace URI exists
     * @param nsUri     XML Schema document namespace URI
     * @return true if schema exists
     */
    public boolean existsSchemaLocation(final String nsUri, final String xsdName) {
        return schemaLocationsCtx.findSchemaImport(nsUri, xsdName).isPresent();
    }

    /**
     * Finds XML Schema document location if exists by given namespace URI
     * @param nsUri     XML Schema document namespace URI
     * @return  XML Schema document location if exists,
     *          otherwise {@link Optional#empty()}
     */
    public Optional<XsdSchemaImportLocation> findSchemaLocation(final String nsUri, final String xsdName) {
        return schemaLocationsCtx.findSchemaImport(nsUri, xsdName);
    }

    /**
     * Finds XML Schema document locations if exists by given namespace URI
     * @param nsUri     XML Schema document namespace URI
     * @return  XML Schema document location if exists,
     *          otherwise {@link Collections#emptyList()}
     */
    public List<XsdSchemaImportLocation> findSchemaLocations(final String nsUri) {
        return schemaLocationsCtx.findSchemaImports(nsUri);
    }

    /**
     * Finds XML Schema document location if exists by given namespace URI
     * @param nsUri     XML Schema document namespace URI
     * @return  XML Schema document location if exists,
     *          otherwise {@link Optional#empty()}
     */
    public Optional<XsdSchemaImportLocation> findPostProcessingSchemaLocation(final String nsUri,
                                                                              final String schemaName) {
        return extraSchemaLocationsCtx.findSchemaImport(nsUri, schemaName);
    }

    /**
     * Finds XML Schema document location if exists by given namespace URI
     * @param nsUri     XML Schema document namespace URI
     * @return  XML Schema document location if exists,
     *          otherwise {@link Collections#emptyList()}
     */
    public List<XsdSchemaImportLocation> findPostProcessingSchemaLocations(final String nsUri) {
        return extraSchemaLocationsCtx.findSchemaImports(nsUri);
    }

    /**
     * Add XML Schema document into extra map. Internally creates document location.
     * @param nsPrefix          XML Schema document namespace prefix
     * @param nsUri             XML Schema document namespace URI
     */
    public XsdSchemaImportLocation addExtraSchemaLocation(final String nsPrefix, final String nsUri) {
        return extraSchemaLocationsCtx.addSchemaLocation(nsPrefix, nsUri);
    }

    /**
     * Add XML Schema document into extra map. Internally creates document location.
     * @param nsUri             XML Schema document namespace URI
     * @param importLocation    XML Schema document location definition
     */
    public void addExtraSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        extraSchemaLocationsCtx.addSchemaLocation(nsUri, importLocation);
    }

    /**
     * Check if XML Schema document with given namespace URI should be created in post-processing
     * @param nsUri     XML Schema document namespace URI
     * @return true if XML Schema document should be created in post-processing
     */
    public boolean isPostProcessingNamespace(final String nsUri) {
        return extraSchemaLocationsCtx.containsSchemaFileLocationMap(nsUri);
    }

    /**
     * Mark X-Definition node to be converted in post-processing phase
     * @param nsUri     XML Schema document namespace URI
     * @param xNode     X-definition node
     */
    public void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        LOG.info("{}Add node to post-processing.", logHeader(TRANSFORMATION, xNode));

        final PostProcessXDefNodeMap.XDefNodeMap ppNsNodes = processXDefNodeMap.computeIfAbsent(
                nsUri,
                key -> new DefaultPostProcessXDefNodeMap.DefaultXDefNodeMap()
        );
        ppNsNodes.addNode(xNode);
    }

    /**
     * Finds XML Schema document by given system identifier.
     *
     * Throws exception if {@code shouldExists} value is true and XML Schema document does not exist
     * @param systemId      XML Schema document system identifier
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XML Schema document if exists
     * @throws SRuntimeException if XML Schema document does not exist
     */
    public XmlSchema findSchemaReq(final String systemId, final AlgPhase phase) throws SRuntimeException {
        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(systemId);
        if (schemas == null || schemas.length == 0) {
            reportWriter.error(XSD.XSD037, systemId);
            throw new SRuntimeException(XSD.XSD007, systemId);
        }

        if (schemas.length > 1) {
            reportWriter.warning(XSD.XSD038, systemId);
            LOG.warn("{}Multiple schemas with required name have been found! schemaName='{}'",
                    logHeader(phase), systemId);
        }

        return schemas[0];
    }

    /**
     * Finds XML Schema document by given system identifier.
     *
     * Throws exception if {@code shouldExists} value is true and XML Schema document does not exist
     * @param systemId      XML Schema document system identifier
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XML Schema document if exists
     *          {@link Optional#empty()} if XML Schema document does not exist
     */
    public Optional<XmlSchema> findSchemaOpt(final String systemId, final AlgPhase phase) {
        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(systemId);
        if (schemas == null || schemas.length == 0) {
            return Optional.empty();
        }

        if (schemas.length > 1) {
            reportWriter.warning(XSD.XSD038, systemId);
            LOG.warn("{}Multiple schemas with required name have been found! schemaName='{}'",
                    logHeader(phase), systemId);
        }

        return Optional.of(schemas[0]);
    }

    /**
     * Finds XML Schema document name by given namespace URI
     * @param nsUri         XML Schema document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XML Schema document name if XML Schema document exists
     *          null if XML Schema document does not exist and {@code shouldExists} value is false
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
                .map(SchemaFileNameLocationMap::getSchemaFileNames)
                .orElse(null);

        if (schemaNames == null && shouldExists) {
            reportWriter.warning(XSD.XSD039, nsUri);
            throw new SRuntimeException(XSD.XSD009, nsUri);
        }

        return schemaNames;
    }

    /**
     * Finds XML Schema document name by given namespace URI
     * @param nsUri         XML Schema document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XML Schema document name if XML Schema document exists
     *          null if XML Schema document does not exist and {@code shouldExists} value is false
     */
    public boolean hasSchemaNameWithNamespaceAndName(final String nsUri,
                                                     final String schemaName,
                                                     boolean shouldExists,
                                                     final AlgPhase phase) {
        final Set<String> schemaNames = findSchemaNamesByNamespace(nsUri, shouldExists, phase);
        if (schemaNames == null || schemaNames.isEmpty()) {
            return false;
        }

        return schemaNames.contains(schemaName);
    }

    /**
     * Add new or update already existing schema node into XML Schema document defined by namespace of input {@code node}
     * @param node  schema node to be added
     * @return  schema node defined by {@code node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@code node}
     */
    public SchemaNode addOrUpdateNode(final SchemaNode node) {
        final String xPos = node.getXdNodeReq().getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XML Schema document defined by {@code systemId}
     * @param node      schema node to be added
     * @param systemId  XML Schema document identifier
     * @return  schema node defined by {@code node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@code node}
     */
    public SchemaNode addOrUpdateNodeInDiffNs(final SchemaNode node, final String systemId) {
        final String xPos = node.getXdNodeReq().getXDPosition();
        final String nodePath = SchemaNode.getPostProcessingReferenceNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XML Schema document defined by {@code systemId} and {@code nodePath}
     * @param systemId  XML Schema document identifier
     * @param nodePath  X-Definition node path
     * @param node      schema node to be added
     * @return  schema node defined by {@code node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@code node}
     */
    public SchemaNode addOrUpdateNode(final String systemId, final String nodePath, final SchemaNode node) {
        XmlSchemaNodeMap.SchemaNodeMap schemaNodeMap = findOrCreateSchemaNodeMap(systemId);

        final SchemaNode refOrig = schemaNodeMap.findSchemaNode(nodePath).orElse(null);
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
            schemaNodeMap.addNode(nodePath, node);

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
     * Updates XML Schema node of schema node defined by X-Definition node {@code xNode}
     * @param xNode         X-Definition node of schema node
     * @param newXsdNode    new XML Schema document node
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

        final XmlSchemaNodeMap.SchemaNodeMap schemaNodeMap = findOrCreateSchemaNodeMap(systemId);
        OptionalExt.of(schemaNodeMap.findSchemaNode(nodePath))
                .ifPresent(schemaNode -> schemaNode.setXsdNode(newXsdNode))
                .orElse(() -> {
                    reportWriter.warning(XSD.XSD040, systemId, nodePath);
                    LOG.warn("{}Node does not exist in system! system='{}', nodePath='{}'",
                            logHeader(XSD_REFERENCE), systemId, nodePath);
                });
    }

    /**
     * Finds all created schema nodes in XML Schema document
     * @param systemId  XML Schema document identifier
     * @return  map of schema nodes
     */
    public XmlSchemaNodeMap.SchemaNodeMap findOrCreateSchemaNodeMap(final String systemId) {
        return xmlSchemaNodeMap.computeIfAbsent(
                systemId,
                key -> new DefaultXmlSchemaNodeMap.DefaultSchemaNodeMap());
    }

    /**
     * Finds schema node defined by {@code systemId} and {@code nodePath}
     * @param systemId  XML Schema document identifier
     * @param nodePath  X-Definition path
     * @return  schema node if exists,
     *          otherwise {@link Optional#empty()}
     */
    public Optional<SchemaNode> findSchemaNode(final String systemId, final String nodePath) {
        return xmlSchemaNodeMap.findByXmlSchema(systemId).flatMap(schemaNodeMap -> schemaNodeMap.findSchemaNode(nodePath));
    }

    /**
     * Deletes created schema node defined by X-Definition node {@code xNode}
     * @param xNode     X-Definition node
     */
    public void removeNode(final XNode xNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        removeNode(systemId, nodePath);
    }

    /**
     * Deletes created schema node defined by XML Schema document identifier and node path
     * @param systemId      XML Schema document identifier
     * @param nodePath      X-Definition node path
     */
    private void removeNode(final String systemId, final String nodePath) {
        LOG.info("{}Removing xsd node. system='{}', nodePath='{}'", logHeader(XSD_REFERENCE), systemId, nodePath);

        final XmlSchemaNodeMap.SchemaNodeMap schemaNodeMap = findOrCreateSchemaNodeMap(systemId);
        schemaNodeMap.removeNode(nodePath).ifPresent(removedNode ->
                LOG.debug("{}Node has been removed! system='{}', nodePath='{}', nodeName='{}'",
                logHeader(XSD_REFERENCE), systemId, nodePath, removedNode.getXdName()));
    }

    /**
     * Finds XML Schema document root node's names by XML Schema document identifier
     * @param systemId      XML Schema document identifier
     * @return  XML Schema document root node's names if exist,
     *          otherwise empty Set
     */
    public Set<String> findSchemaRootNodeNames(final String systemId) {
        return rootNodeNames.getOrDefault(systemId, Collections.emptySet());
    }

    /**
     * Add XML Schema document root node name
     * @param systemId      XML Schema document identifier
     * @param nodeName      X-Definition name
     */
    public void addRootNodeName(final String systemId, final String nodeName) {
        final Set<String> schemaRootNodeNames = rootNodeNames.computeIfAbsent(systemId, key -> new HashSet<>());
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
     * @param systemId  XML Schema document identifier (can be nullable)
     * @param path      unique constraint path
     * @return Created or found unique constraint
     */
    public UniqueConstraint addOrGetUniqueConst(final String name, @Nullable String systemId, String path) {
        if (systemId == null) {
            systemId = "";
        }

        final XmlSchemaUniqueConstraintMap.XDefUniqueSetMap uniqueSetMap = getOrCreateXDefUniqueSetMap(systemId);
        if (!path.isEmpty()) {
            path = "/" + path;
        }

        final String finalPath = path;
        final String finalSystemId = systemId;

        return OptionalExt.of(findUniqueConstraint(uniqueSetMap, name, path))
                .ifPresent(uniqueConstraint ->
                        LOG.debug("{}Creating unique set - already exists. name='{}', path='{}', system='{}'",
                                logHeader(PREPROCESSING), name, finalPath, finalSystemId))
                .orElseGet(() -> {
                    LOG.info("{}Creating unique set. name='{}', path='{}', system='{}'",
                            logHeader(PREPROCESSING), name, finalPath, finalSystemId);

                    final List<UniqueConstraint> uniqueInfoList = uniqueSetMap.computeIfAbsent(
                            finalPath,
                            key -> new LinkedList<>()
                    );
                    final UniqueConstraint uniqueConstraint = new UniqueConstraint(name, finalSystemId);
                    uniqueInfoList.add(uniqueConstraint);

                    return uniqueConstraint;
                });
    }

    /**
     * Finds unique constraint
     * @param uniqueConstraintMap   unique constraint map
     * @param name                  unique constraint name
     * @param path                  unique constraint path
     * @return  unique constraint if exists inside given map,
     *          otherwise {@link Optional#empty()}
     */
    private static Optional<UniqueConstraint> findUniqueConstraint(
            final XmlSchemaUniqueConstraintMap.XDefUniqueSetMap uniqueConstraintMap,
            final String name,
            final String path) {

        if (!uniqueConstraintMap.isEmpty()) {
            final List<UniqueConstraint> uniqueInfoList = uniqueConstraintMap.findByXPath(path);
            for (UniqueConstraint u : uniqueInfoList) {
                if (u.getName().equals(name)) {
                    return Optional.of(u);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Finds unique constraint by X-Definition node
     * @param xData X-Definition node
     * @return  unique constraint if exists,
     *          otherwise {@link Optional#empty()}
     */
    public Optional<UniqueConstraint> findUniqueConst(final XData xData) {
        LOG.debug("{}Finding unique set. name='{}'", logHeader(TRANSFORMATION, xData), xData.getValueTypeName());

        // TODO: Finding of unique set not using variable name, ie. uniqueSet u int();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xData.getXDPosition());
        final String uniqueInfoName = XsdNameUtils.getUniqueSetName(xData.getValueTypeName());
        final String uniquestSetPath = "/" + XsdNameUtils.getXNodePath(xData.getXDPosition());
        final UniqueConstraint uniqueInfo = findUniqueConst(uniqueInfoName, systemId, uniquestSetPath)
                .orElse(findUniqueConst(uniqueInfoName, "", uniquestSetPath).orElse(null));

        return Optional.ofNullable(uniqueInfo);
    }

    /**
     * Finds unique constrain in tree of unique constraints.
     * Iterates through tree, starting at {@code uniquestSetPath} path and going to root of XML tree
     * @param uniqueInfoName        unique constraint name
     * @param systemId              XML Schema document identifier
     * @param uniquestSetPath       unique constraint path
     * @return  unique constraint if exists inside given map,
     *          otherwise {@link Optional#empty()}
     */
    private Optional<UniqueConstraint> findUniqueConst(final String uniqueInfoName, final String systemId, String uniquestSetPath) {
        LOG.debug("{}Finding unique set. uniqueName='{}', systemId='{}'",
                logHeader(TRANSFORMATION), uniqueInfoName, systemId);

        UniqueConstraint uniqueInfo = null;
        int slashPos;
        final XmlSchemaUniqueConstraintMap.XDefUniqueSetMap uniqueSetMap = getOrCreateXDefUniqueSetMap(systemId);

        if (!uniqueSetMap.isEmpty()) {
            while (uniqueInfo == null && !"".equals(uniquestSetPath)) {
                slashPos = uniquestSetPath.lastIndexOf('/');
                if (slashPos == -1) {
                    uniquestSetPath = "";
                } else {
                    uniquestSetPath = uniquestSetPath.substring(0, slashPos);
                }

                uniqueInfo = findUniqueConstraint(uniqueSetMap, uniqueInfoName, uniquestSetPath).orElse(null);
            }
        }

        return Optional.ofNullable(uniqueInfo);
    }

    /**
     * Get all created unique constraints created in XML Schema document
     * @param systemId  XML Schema document identifier
     * @return  unique constraints
     */
    public Optional<XmlSchemaUniqueConstraintMap.XDefUniqueSetMap> findXDefUniqueSetMap(final String systemId) {
        return xmlSchemaUniqueConstraintMap.findByXmlSchema(systemId);
    }

    /**
     * Create or get unique constraints map in given XML Schema document
     * @param systemId  XML Schema document identifier
     * @return  unique constraints map
     */
    private XmlSchemaUniqueConstraintMap.XDefUniqueSetMap getOrCreateXDefUniqueSetMap(final String systemId) {
        return xmlSchemaUniqueConstraintMap.computeIfAbsent(
                systemId,
                key -> new DefaultXmlSchemaUniqueConstraintMap.DefaultXDefUniqueSetMap()
        );
    }


}
