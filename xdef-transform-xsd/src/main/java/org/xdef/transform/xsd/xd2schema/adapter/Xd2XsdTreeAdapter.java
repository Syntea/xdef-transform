package org.xdef.transform.xsd.xd2schema.adapter;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContentProcessing;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDPool;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import org.xdef.model.XMVariable;
import org.xdef.model.XMVariableTable;
import org.xdef.transform.xsd.model.OptionalExt;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.xd2schema.error.XsdTreeAdapterException;
import org.xdef.transform.xsd.xd2schema.factory.SchemaNodeFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdNameFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.model.SchemaNode;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.XsdSchemaImportLocation;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.model.xsd.AbstractXmlSchemaGroupParticleWrapper;
import org.xdef.transform.xsd.xd2schema.model.xsd.XmlSchemaChoiceWrapper;
import org.xdef.transform.xsd.xd2schema.model.xsd.XmlSchemaSequenceWrapper;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdPostProcessor;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import static org.xdef.impl.compile.CompileBase.UNIQUESET_M_VALUE;
import static org.xdef.impl.compile.CompileBase.UNIQUESET_VALUE;
import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XD_PARSER_EQ;

/**
 * Transforms X-Definition tree node structure to XML Schema tree node structure
 */
public class Xd2XsdTreeAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Xd2XsdTreeAdapter.class);

    /**
     * Output XML Schema document
     */
    final private XmlSchema schema;

    /**
     * Output XML Schema document name
     */
    final private String schemaName;

    /**
     * XML Schema node factory
     */
    final private XsdNodeFactory xsdFactory;

    /**
     * XML Schema adapter context
     */
    final private XsdAdapterCtx adapterCtx;

    /**
     * XML Schema post processor for advanced transformation
     */
    final private XsdPostProcessor postProcessor;

    /**
     * Flag if post processing phase has been reached
     */
    private boolean isPostProcessingPhase = false;

    /**
     * Already processed nodes. We dont want to process same node multiple times (ie. references)
     */
    private Set<XMNode> xdProcessedNodes = null;

    public Xd2XsdTreeAdapter(XmlSchema schema, String schemaName, XsdNodeFactory xsdFactory, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.xsdFactory = xsdFactory;
        this.adapterCtx = adapterCtx;
        this.postProcessor = new XsdPostProcessor(adapterCtx);
    }

    /**
     * Set flag if post processing phase has been reached
     */
    public void setPostProcessing() {
        this.isPostProcessingPhase = true;
    }

    /**
     * Gather names of all X-Definition root element nodes
     * @param xDef  input X-Definition
     */
    public void loadXdefRootNames(final XDefinition xDef) {
        LOG.info("{}Loading root of X-Definition", logHeader(PREPROCESSING, xDef));

        if (xDef._rootSelection != null) {
            for (String rootName : xDef._rootSelection.keySet()) {
                adapterCtx.addRootNodeName(schemaName, rootName);

                LOG.debug("{}Add X-Definition element root name. name='{}'", logHeader(PREPROCESSING, xDef), rootName);
            }
        }
    }

    /**
     * Gather data of all global and local uniqueSets used by X-Definition
     * @param xDef  input X-Definition
     */
    public void loadXdefRootUniqueSets(final XDefinition xDef) {
        loadUniqueSets(xDef, xDef.getXDPool().getVariableTable(), "");
    }

    /**
     * Gather data of uniqueSets used by X-Definition element node
     * @param xElem input X-Definition element node
     */
    public void loadElementUniqueSets(final XElement xElem) {
        loadUniqueSets(xElem.getDefinition(), xElem._vartable, XsdNameUtils.getXNodePath(xElem.getXDPosition()));
    }

    /**
     * Gather data of uniqueSets from variable table
     * @param xNode             container of variable table
     * @param varTable          input variable table
     * @param varTablePath      path of variable table
     */
    private void loadUniqueSets(final XNode xNode, final XMVariableTable varTable, final String varTablePath) {
        if (varTable != null && varTable.size() > 0) {
            LOG.info("{}Loading unique sets of X-Definition.", logHeader(PREPROCESSING, xNode));

            for (XMVariable xmVariable : varTable.toArray()) {
                if ((xmVariable.getType() == UNIQUESET_M_VALUE || xmVariable.getType() == UNIQUESET_VALUE)
                        && xmVariable.getOffset() != -1) {
                    adapterCtx.addOrGetUniqueConst(
                            XsdNameUtils.getReferenceName(xmVariable.getName()),
                            XsdNamespaceUtils.getSystemIdFromXPos(xmVariable.getName()).orElse(null),
                            varTablePath);
                }
            }
        }
    }

    /**
     * Transforms X-Definition node tree into XML Schema node tree.
     * @param xNode     root of X-Definition tree
     * @return root of XML Schema node tree, may return null
     */
    public Optional<XmlSchemaObject> convertTree(XMNode xNode) {
        xdProcessedNodes = new HashSet<>();
        return convertTreeInt(xNode, true);
    }

    /**
     * Transforms X-Definition node tree into XML Schema node tree.
     * @param xNode     root of X-Definition tree
     * @param topLevel  flag if X-Definition node is placed on top level
     * @return root of XML Schema node tree, may return null
     */
    private Optional<XmlSchemaObject> convertTreeInt(final XMNode xNode, boolean topLevel) {
        if (!xdProcessedNodes.add(xNode)) {
            LOG.debug("{}Already processed. This node should be reference definition.", logHeader(TRANSFORMATION, xNode));
            return Optional.empty();
        }

        short xdElemKind = xNode.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                return Optional.of(createAttribute((XData) xNode, topLevel));
            }
            case XNode.XMTEXT: {
                LOG.info("{}Creating simple (text) content ...", logHeader(TRANSFORMATION, xNode));
                return (Optional)xsdFactory.createTextBasedSimpleContent((XData)xNode, topLevel);
            }
            case XNode.XMELEMENT: {
                return Optional.of(createElement((XElement) xNode, topLevel));
            }
            case XNode.XMSELECTOR_END:
                return Optional.empty();
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                LOG.debug("{}Processing Particle node. particle='{}'",
                        logHeader(TRANSFORMATION, xNode), Xd2XsdUtils.particleXKindToString(xdElemKind));
                return Optional.of(xsdFactory.createGroupParticle(xNode));
            case XNode.XMDEFINITION: {
                adapterCtx.getReportWriter().warning(XSD.XSD017);
                LOG.warn("{}X-Definition node can't be transformed, it has to be pre-processed only!",
                        logHeader(TRANSFORMATION, xNode));
                return Optional.empty();
            }
            default: {
                adapterCtx.getReportWriter().warning(XSD.XSD018, xdElemKind);
                LOG.warn("{}Unknown type of node. nodeType='{}'", logHeader(TRANSFORMATION, xNode), xdElemKind);
            }
        }

        return Optional.empty();
    }

    /**
     * Creates XML Schema attribute node based on X-Definition attribute node
     * @param xData     X-Definition source (attribute node)
     * @param topLevel  flag if source node is placed on top level
     * @return XML Schema attribute node
     */
    private XmlSchemaAttribute createAttribute(final XData xData, boolean topLevel) {
        LOG.info("{}Creating attribute ...", logHeader(TRANSFORMATION, xData));

        final XmlSchemaAttribute attr = xsdFactory.createEmptyAttribute(xData, topLevel);
        final String refNsUri = xData.getNSUri();
        final String nodeName = xData.getName();
        if (XsdNamespaceUtils.isNodeInDifferentNamespace(nodeName, refNsUri, schema)) {
            final String localName = XsdNameUtils.getReferenceName(nodeName);
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefixRequired(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            attr.getRef().setTargetQName(new QName(refNsUri, localName));

            LOG.info("{}Creating attribute reference from different namespace. name='{}', refNsUri='{}'",
                    logHeader(TRANSFORMATION, xData), xData.getName(), refNsUri);

            // Attribute is referencing to new namespace, which will be created in post-processing
            if (adapterCtx.isPostProcessingNamespace(nsUri)) {
                adapterCtx.addNodeToPostProcessing(nsUri, xData);
            } else {
                SchemaNode node = SchemaNodeFactory.createAttributeNode(attr, xData);
                adapterCtx.addOrUpdateNode(node);
            }
        } else {
            QName qName = null;
            attr.setName(xData.getName());

            final UniqueConstraint uniqueConstraint = adapterCtx.findUniqueConst(xData).orElse(null);

            if (uniqueConstraint != null) {
                LOG.info("{}Attribute is using unique set. uniqueSet='{}'",
                        logHeader(TRANSFORMATION, xData), uniqueConstraint.getName());

                final UniqueConstraint.Type type = XsdNameUtils.getUniqueSetVarType(xData.getValueTypeName());
                if (UniqueConstraint.isStringConstraint(type)) {
                    qName = Constants.XSD_STRING;
                }

                final String varName = XsdNameUtils.getUniqueSetVarName(xData.getValueTypeName());
                final String nodePath = XsdNameUtils.getXNodePath(xData.getXDPosition());
                uniqueConstraint.addConstraint(varName, attr, nodePath, type, adapterCtx.getReportWriter());

                XsdNodeFactory.createAnnotation(
                        StringFormatter.format("Original part of uniqueSet: {} ({})",
                                uniqueConstraint.getPath(),
                                xData.getValueTypeName()),
                        adapterCtx
                ).ifPresent(attr::setAnnotation);
            }

            if (qName != null) {
                attr.setSchemaTypeName(qName);
            } else if (xData.getRefTypeName() != null && uniqueConstraint == null) {
                final String refTypeName = adapterCtx.getNameFactory().findTopLevelName(xData, false)
                        .orElseGet(() -> {
                            final Optional<String> defaultRefTypeNameOpt = XsdNameFactory.createLocalSimpleTypeName(xData);
                            defaultRefTypeNameOpt.ifPresent(s -> adapterCtx.getNameFactory().addTopSimpleTypeName(xData, s));

                            return defaultRefTypeNameOpt.orElse("");
                        });

                String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refTypeName);
                if (topLevel
                        && isPostProcessingPhase
                        && NAMESPACE_PREFIX_EMPTY.equals(nsPrefix)
                        && XsdNamespaceUtils.containsNsPrefix(xData.getName())) {
                    nsPrefix = schema.getSchemaNamespacePrefix();
                }

                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                attr.setSchemaTypeName(new QName(nsUri, refTypeName));
                LOG.info("{}Creating attribute reference in same namespace/X-Definition. name='{}', type='{}'",
                        logHeader(TRANSFORMATION, xData), xData.getName(), attr.getSchemaTypeName());
            } else if ((qName = Xd2XsdParserMapping.findDefaultParserQName(xData, adapterCtx, true).orElse(null)) != null) {
                attr.setSchemaTypeName(qName);
                LOG.info("{}Content of attribute contains only XML Schema datatype. name='{}', qName='{}'",
                        logHeader(TRANSFORMATION, xData), xData.getName(), qName);
            } else if (XD_PARSER_EQ.equals(xData.getParserName())) {
                qName = Xd2XsdParserMapping.findDefaultParserQName(xData.getValueTypeName(), adapterCtx)
                        .orElse(null);

                final String fixedValue = xData.getFixedValue() != null ? xData.getFixedValue().stringValue() : null;
                if (fixedValue != null) {
                    attr.setFixedValue(fixedValue);
                }

                if (qName != null) {
                    attr.setSchemaTypeName(qName);
                }

                LOG.info("{}Content of attribute contains datatype with fixed value. " +
                                "name='{}', qName='{}', fixedValue='{}'",
                        logHeader(TRANSFORMATION, xData), xData.getName(), qName, fixedValue);
            } else {
                // Attributes using unique set should not contain simple-type with name
                if (uniqueConstraint != null) {
                    final String parserName = xData.getParserName();
                    qName = Xd2XsdParserMapping.findDefaultParserQName(parserName, adapterCtx)
                            .orElseThrow(() -> new XsdTreeAdapterException("No parser has found for unique constraint. " +
                                    "parserName='{}'", parserName)
                    );
                    attr.setSchemaTypeName(qName);
                } else {
                    final XmlSchemaSimpleType simpleType = xsdFactory.createAttributeSimpleType(xData, attr.getName());
                    if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        postProcessor.simpleTypeRestrictionToAttr((XmlSchemaSimpleTypeRestriction)simpleType.getContent(), attr);
                    }
                    if (attr.getSchemaTypeName() == null) {
                        attr.setSchemaType(simpleType);
                    }
                }
            }

            final String defaultValue = xData.getDefaultValue() != null ? xData.getDefaultValue().stringValue() : null;
            if (defaultValue != null) {
                attr.setDefaultValue(defaultValue);
            }

            XsdNameUtils.resolveAttributeQName(schema, attr, xData.getName());

            SchemaNode node = SchemaNodeFactory.createAttributeNode(attr, xData);
            adapterCtx.addOrUpdateNode(node);
        }

        return attr;
    }

    /**
     * Creates XML Schema element node based on X-Definition element node
     * @param xElem     X-Definition source (element node)
     * @param topLevel  flag if source node is placed on top level
     * @return XML Schema element node
     */
    private XmlSchemaObject createElement(final XElement xElem, boolean topLevel) {
        LOG.info("{}Creating element ...", logHeader(TRANSFORMATION, xElem));

        loadElementUniqueSets(xElem);

        final XmlSchemaElement xsdElem = xsdFactory.createEmptyElement(xElem, topLevel);

        if (!Xd2XsdUtils.isAnyElement(xElem) && (xElem.isReference() || xElem.getReferencePos() != null)) {
            final QName referenceQName = getRefQName(xElem);
            if (xElem.isReference()) {
                createElementReference(xElem, referenceQName, xsdElem);
            } else {
                createElementExtendedReference(xElem, referenceQName, topLevel, xsdElem);
            }
        } else {
            // Element is not reference but name contains different namespace -> we will have to create reference in new namespace in post-processing
            if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema)) {
                createElementInDiffNamespace(xElem, xsdElem);
            } else {

                if (Xd2XsdUtils.isAnyElement(xElem)) {
                    final XmlSchemaAny xsdAny = xsdFactory.createAny(xElem);
                    // TODO: should has zero occurs by default?
                    xsdAny.setMinOccurs(0);
                    xsdAny.setProcessContent(XmlSchemaContentProcessing.LAX);
                    if (xElem._attrs.size() > 0 || xElem._childNodes.length > 0) {
                        XsdNodeFactory.createAnnotation(
                                "Original any element contains children nodes/attributes", adapterCtx
                        ).ifPresent(xsdAny::setAnnotation);

                        adapterCtx.getReportWriter().warning(XSD.XSD019);

                        LOG.warn("{}!Lossy transformation! Any type with attributes/children nodes is not supported!",
                                logHeader(TRANSFORMATION, xElem));
                    }

                    return xsdAny;
                } else if (topLevel && Xd2XsdUtils.containsAnyElement(xElem)) {
                    adapterCtx.getReportWriter().warning(XSD.XSD020);
                    LOG.warn("{}Any element cannot be root element of xsd!", logHeader(TRANSFORMATION, xElem));
                }

                fillXsdElement(xElem, topLevel, xsdElem);
            }
        }

        return xsdElem;
    }

    /**
     * Creates XML Schema element node using reference
     * @param xElem     X-Definition source (element node)
     * @param refQName  reference QName
     * @param xsdElem   XML Schema element node which will be filled (output)
     */
    private void createElementReference(final XElement xElem, final QName refQName, final XmlSchemaElement xsdElem) {
        LOG.info("{}Creating element reference ...", logHeader(TRANSFORMATION, xElem));

        final String refXPos = xElem.getReferencePos();
        final String xPos = xElem.getXDPosition();

        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refXPos);
        final String refLocalName = XsdNameUtils.getReferenceName(refXPos);

        if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), xElem.getNSUri(), schema)) {
            xsdElem.getRef().setTargetQName(refQName);
            LOG.info("{}Element referencing to different namespace. name='{}', refQName='{}'",
                    logHeader(TRANSFORMATION, xElem), xElem.getName(), xsdElem.getRef().getTargetQName());
        } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refXPos, schema)) {
            xsdElem.getRef().setTargetQName(refQName);
            LOG.info("{}Element referencing to different X-Definition and namespace. refXDefName='{}', refQName='{}'",
                    logHeader(TRANSFORMATION, xElem), refSystemId, xsdElem.getRef().getTargetQName());
        } else if (XsdNamespaceUtils.isRefInDifferentSystem(refXPos, xPos)) {
            xsdElem.getRef().setTargetQName(refQName);
            LOG.info("{}Element referencing to different X-Definition. refXDefName='{}'",
                    logHeader(TRANSFORMATION, xElem), refSystemId);
        } else if (Xd2XsdUtils.isAnyElement(xElem)) {
            xsdElem.getRef().setTargetQName(refQName);
        } else {
            xsdElem.setName(xElem.getName());
            xsdElem.setSchemaTypeName(refQName);
            XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);

            LOG.info("{}Element referencing to same namespace/X-Definition. refLocalName='{}'",
                    logHeader(TRANSFORMATION, xElem), refLocalName);
        }

        final String refNodePath = XsdNameUtils.getXNodePath(refXPos);
        SchemaNodeFactory.createElemRefAndDef(xElem, xsdElem, refSystemId, refXPos, refNodePath, adapterCtx);
    }

    /**
     * Creates XML Schema element node using reference, where source X-Definition node is extending the reference
     * @param xElem     X-Definition source (element node)
     * @param refQName  reference QName
     * @param topLevel  flag if source node is placed on top level
     * @param xsdElem   XML Schema element node which will be filled (output)
     */
    private void createElementExtendedReference(final XElement xElem,
                                                final QName refQName,
                                                boolean topLevel,
                                                final XmlSchemaElement xsdElem) {
        LOG.info("{}Creating element extended reference ...", logHeader(TRANSFORMATION, xElem));

        final String refXPos = xElem.getReferencePos();
        final XDPool xdPool = xElem.getXDPool();
        boolean usingExtension = false;

        xsdElem.setName(xElem.getName());
        XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);

        if (xdPool == null) {
            adapterCtx.getReportWriter().error(XSD.XSD047);
            LOG.error("{}XDPool is not set!", logHeader(TRANSFORMATION, xElem));
        } else {
            XMNode xRefNode = xdPool.findModel(refXPos);
            if (xRefNode.getKind() != XNode.XMELEMENT) {
                adapterCtx.getReportWriter().error(XSD.XSD003);
                LOG.error("{}Reference to node type element is expected!", logHeader(TRANSFORMATION, xElem));
            } else {
                final XElement xRefElem = (XElement)xRefNode;
                final List<XMData> extAttrs = new ArrayList<>();
                final List<XNode> extNodes = new ArrayList<>();

                // Get only extending attrs and children nodes
                {
                    if (xElem._attrs.size() > xRefElem._attrs.size()) {
                        final XMData[] xRefAttrs = xRefElem.getAttrs();
                        for (XMData xAttr : xElem.getAttrs()) {
                            boolean duplicated = false;
                            for (XMData xRefAttr : xRefAttrs) {
                                if (xRefAttr.getName().equals(xAttr.getName())) {
                                    duplicated = true;
                                    break;
                                }
                            }

                            if (!duplicated) {
                                extAttrs.add(xAttr);
                            }
                        }
                    }

                    if (xElem._childNodes.length > xRefElem._childNodes.length) {
                        final XNode[] xRefNodes = xRefElem._childNodes;
                        for (XNode xNode : xElem._childNodes) {
                            boolean duplicated = false;
                            for (XNode xRefChildNode : xRefNodes) {
                                if (xRefChildNode.getName().equals(xNode.getName())) {
                                    duplicated = true;
                                    break;
                                }
                            }

                            if (!duplicated) {
                                extNodes.add(xNode);
                            }
                        }
                    }
                }

                if (extNodes.size() > 0 || extAttrs.size() > 0) {
                    usingExtension = true;

                    final XmlSchemaComplexContentExtension complexContentExtension =
                            xsdFactory.createEmptyComplexContentExtension(refQName);
                    final XmlSchemaComplexContent complexContent = xsdFactory.createComplexContent(complexContentExtension);
                    final XmlSchemaComplexType complexType = createComplexType(
                            extAttrs.stream().toArray(XMData[]::new),
                            extNodes.stream().toArray(XNode[]::new),
                            xElem,
                            topLevel
                    );

                    complexContentExtension.setParticle(complexType.getParticle());
                    complexContentExtension.getAttributes().addAll(complexType.getAttributes());
                    complexType.setParticle(null);
                    complexType.getAttributes().clear();

                    complexType.setContentModel(complexContent);
                    xsdElem.setSchemaType(complexType);

                    final String refNodePath = XsdNameUtils.getXNodePath(refXPos);
                    final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refXPos);
                    SchemaNodeFactory.createComplexExtRefAndDef(xElem, complexContentExtension, refSystemId, refXPos, refNodePath, adapterCtx);
                }
            }
        }

        if (!usingExtension) {
            fillXsdElement(xElem, topLevel, xsdElem);
        }
    }

    /**
     * Creates XML Schema element where source X-Definition node is using different namespace
     * @param xElem     X-Definition source (element node)
     * @param xsdElem   XML Schema element node which will be filled (output)
     */
    private void createElementInDiffNamespace(final XElement xElem, final XmlSchemaElement xsdElem) {
        LOG.info("{}Creating element in different namespace ...", logHeader(TRANSFORMATION, xElem));

        final String localName = XsdNameUtils.getReferenceName(xElem.getName());
        final String xDefPos = xElem.getXDPosition();
        final String nodePath = XsdNameUtils.getXNodePath(xDefPos);
        String nsPrefix = XsdNamespaceUtils.getNamespacePrefixRequired(xElem.getName());
        String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

        // Post-processing
        if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
            xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
            final String currSchemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
            final OptionalExt<XsdSchemaImportLocation> schemaImportLocationOpt = OptionalExt.of(
                    adapterCtx.findPostProcessingSchemaLocation(nsUri, currSchemaName)
            );

            final String finalNsUri = nsUri;
            schemaImportLocationOpt.ifPresent(importLocation -> {
                        final String refSystemId = importLocation.getFileName();
                        adapterCtx.addNodeToPostProcessing(finalNsUri, xElem);
                        final String refNodePath = SchemaNode.getPostProcessingReferenceNodePath(xElem.getXDPosition());
                        final String refNodePos = SchemaNode.getPostProcessingNodePos(refSystemId, refNodePath);
                        SchemaNodeFactory.createElemRefAndDefDiffNamespace(xElem, xsdElem, currSchemaName, nodePath,
                                refSystemId, refNodePos, refNodePath, adapterCtx);
                    }
            ).orElse(() -> {
                adapterCtx.getReportWriter().warning(XSD.XSD021, finalNsUri);
                LOG.warn("{}Element is in different namespace which is not marked for post-processing! nsUri='{}'",
                        logHeader(TRANSFORMATION, xElem), finalNsUri);
            });
        } else {
            nsUri = XsdNamespaceUtils.getNodeNamespaceUri(xElem, adapterCtx, TRANSFORMATION);

            if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xDefPos);
                xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
                SchemaNodeFactory.createElemRefAndDefDiffNamespace(xElem, xsdElem, schemaName, nodePath, systemId, xDefPos, nodePath, adapterCtx);
            } else {
                nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
                adapterCtx.getReportWriter().error(XSD.XSD004, nsPrefix);
                LOG.error("{}Element referencing to unknown namespace! nsPrefix='{}'",
                        logHeader(TRANSFORMATION, xElem), nsPrefix);
            }
        }
    }

    /**
     * Creates content of standard XML Schema element node
     * @param xElem     X-Definition source (element node)
     * @param topLevel  flag if source node is placed on top level
     * @param xsdElem   XML Schema element node which will be filled (output)
     */
    private void fillXsdElement(final XElement xElem, boolean topLevel, final XmlSchemaElement xsdElem) {
        xsdElem.setName(XsdNameUtils.getName(xElem));
        XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);

        // If element contains only data, we dont have to create complexType
        if (xElem._attrs.size() == 0 && xElem._childNodes.length == 1 && xElem._childNodes[0].getKind() == XNode.XMTEXT) {
            addSimpleTypeToElem(xsdElem, (XData) xElem._childNodes[0]);
        } else {
            final XmlSchemaComplexType complexType = createComplexType(xElem.getAttrs(), xElem._childNodes, xElem, topLevel);
            if (complexType.getContentModel() != null || complexType.getAttributes().size() > 0 || complexType.getParticle() != null) {
                xsdElem.setType(complexType);
            }
        }

        SchemaNode node = SchemaNodeFactory.createElementNode(xsdElem, xElem);
        if (isPostProcessingPhase) {
            String nsPrefix = XsdNamespaceUtils.getNamespacePrefixRequired(xElem.getName());
            String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
            final String currSchemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
            final OptionalExt<XsdSchemaImportLocation> importLocationOpt = OptionalExt.of(
                    adapterCtx.findPostProcessingSchemaLocation(nsUri, currSchemaName));

            importLocationOpt.ifPresent(importLocation -> {
                        final String systemId = importLocation.getFileName();
                        adapterCtx.addOrUpdateNodeInDiffNs(node, systemId);
                    }
            ).orElse(() -> adapterCtx.addOrUpdateNode(node));
        } else {
            adapterCtx.addOrUpdateNode(node);
        }

    }

    /**
     * Determines reference QName based on X-Definition element node
     * @param xElem X-Definition source (element node)
     * @return reference QName
     */
    private QName getRefQName(final XElement xElem) {
        LOG.debug("{}Creating element reference QName ...", logHeader(TRANSFORMATION, xElem));

        final String refXPos = xElem.getReferencePos();
        final String xPos = xElem.getXDPosition();

        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refXPos);
        final String refLocalName = XsdNameUtils.getReferenceName(refXPos);
        final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);

        if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), xElem.getNSUri(), schema)) {
            final String nsUri = xElem.getNSUri();
            final String nsPrefix = schema.getNamespaceContext().getPrefix(nsUri);
            if (nsPrefix == null) {
                final XmlSchema refSchema = adapterCtx.findSchemaReq(refSystemId, TRANSFORMATION);
                final String refNsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                if (!XsdNamespaceUtils.isValidNsUri(refNsUri)) {
                    adapterCtx.getReportWriter().error(XSD.XSD004, nsPrefix);
                    LOG.error("{}Element referencing to unknown namespace! nsPrefix='{}'",
                            logHeader(TRANSFORMATION, xElem), nsPrefix);
                } else {
                    XsdNamespaceUtils.addNamespaceToCtx((NamespaceMap) schema.getNamespaceContext(), refNsPrefix, refNsUri, refSystemId, POSTPROCESSING);
                }
            }

            return new QName(nsUri, refLocalName);
        } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refXPos, schema)) {
            final XmlSchema refSchema = adapterCtx.findSchemaReq(refSystemId, TRANSFORMATION);
            final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
            return new QName(nsUri, refLocalName);
        } else if (XsdNamespaceUtils.isRefInDifferentSystem(refXPos, xPos)) {
            return new QName(NAMESPACE_PREFIX_EMPTY, refLocalName);
        } else if (Xd2XsdUtils.isAnyElement(xElem)) {
            String anyLocalName = refLocalName;
            final int anyPos = anyLocalName.indexOf("$any/$any");
            if (anyPos != -1) {
                anyLocalName = anyLocalName.substring(0, anyPos);
            }
            return new QName(refNsPrefix, anyLocalName);
        }

        if (!NAMESPACE_PREFIX_EMPTY.equals(refNsPrefix)) {
            adapterCtx.getReportWriter().warning(XSD.XSD022, refNsPrefix);
            LOG.warn("{}Element reference namespace prefix should be empty! refNsPrefix='{}'",
                    logHeader(TRANSFORMATION, xElem), refNsPrefix);
        }

        return new QName(refNsPrefix, refLocalName);
    }

    /**
     * Creates simple type from X-Definition text node and fill XML Schema element node by created simple type
     * @param xsdElem       XML Schema element node
     * @param xDataText     X-Definition text node
     */
    private void addSimpleTypeToElem(final XmlSchemaElement xsdElem, final XData xDataText) {
        LOG.info("{}Creating simple type of element. elementName='{}'",
                logHeader(TRANSFORMATION, xDataText), xsdElem.getName());

        final OptionalExt<QName> qNameOpt = OptionalExt.of(Xd2XsdParserMapping.findDefaultParserQName(
                xDataText,
                adapterCtx,
                true));

        qNameOpt.ifPresent(qName -> {
            xsdElem.setSchemaTypeName(qName);
            LOG.debug("{}Content of element contains only XML Schema datatype. elementName='{}', localName='{}'",
                    logHeader(TRANSFORMATION, xDataText), xsdElem.getName(), qName.getLocalPart());
        }).orElse(() -> xsdElem.setType(xsdFactory.createAnonymousSimpleType(xDataText, xsdElem.getName())));
    }

    /**
     * Creates XML Schema complex type node based on input parameters
     * @param xAttrs            X-Definition attribute nodes
     * @param xChildrenNodes    X-Definition children nodes
     * @param xElem             X-Definition element node
     * @param topLevel          flag if source node is placed on top level
     * @return XML Schema complex type node
     */
    private XmlSchemaComplexType createComplexType(final XMData[] xAttrs, final XNode[] xChildrenNodes, final XElement xElem, boolean topLevel) {
        LOG.info("{}Creating complex type of element ...", logHeader(TRANSFORMATION, xElem));

        final XmlSchemaComplexType complexType = xsdFactory.createEmptyComplexType(false);
        final Stack<XmlSchemaParticle> particleStack = new Stack<>();
        AtomicReference<XmlSchemaParticle> currParticle = new AtomicReference<>();
        boolean groupRefNodes = false;
        int stackPopCounter = 0;

        // Convert all children nodes
        for (XNode xnChild : xChildrenNodes) {
            short childrenKind = xnChild.getKind();
            // Skip all reference children nodes
            if (groupRefNodes) {
                if (childrenKind == XNode.XMSELECTOR_END) {
                    groupRefNodes = false;
                }
                continue;
            }
            // Particle nodes (sequence, choice, all)
            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                XmlSchemaParticle newParticle = null;
                // Create group reference
                if (childrenKind == XNode.XMMIXED && !xnChild.getXDPosition().contains(xElem.getXDPosition())) {
                    newParticle = createGroupReference(xChildrenNodes, currParticle.get(), particleStack, xElem);
                }

                if (newParticle == null) {
                    LOG.info("{}Creating particle to complex content of element. particle='{}'",
                            logHeader(TRANSFORMATION, xElem), Xd2XsdUtils.particleXKindToString(childrenKind));

                    final AbstractXmlSchemaGroupParticleWrapper newGroupParticle = (AbstractXmlSchemaGroupParticleWrapper)
                            convertTreeInt(xnChild, false)
                                    .orElseThrow(() -> new XsdTreeAdapterException("Required group particle node not created."));
                    stackPopCounter += updateGroupParticles(particleStack, currParticle.get(), newGroupParticle);
                    currParticle.set(particleStack.peek());
                } else if (newParticle instanceof XmlSchemaGroupRef) {
                    currParticle.set(newParticle);
                    groupRefNodes = true;
                }
            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTreeInt(xnChild, topLevel)
                        .orElse(null);
                if (complexType.getContentModel() != null) {
                    adapterCtx.getReportWriter().warning(XSD.XSD023);
                    LOG.warn("{}Complex type already has simple content!", logHeader(TRANSFORMATION, xElem));
                } else if (simpleContent != null && simpleContent.getContent() != null
                        && ((simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension)
                        || simpleContent.getContent() instanceof XmlSchemaSimpleContentRestriction)) {
                    LOG.debug("{}Add simple content with attributes to complex type.", logHeader(TRANSFORMATION, xElem));

                    XmlSchemaSimpleContentExtension contentForAttrs = null;

                    // Split simple content in case of containing restriction facets and parent owns attributes
                    if (simpleContent.getContent() instanceof XmlSchemaSimpleContentRestriction && xAttrs.length > 0) {
                        final String newSplittedName = XsdNameFactory.createTextElemName(xElem.getName());
                        final XmlSchemaSimpleContentExtension splitted = new XmlSchemaSimpleContentExtension();
                        splitted.setBaseTypeName(
                                ((XmlSchemaSimpleContentRestriction) simpleContent.getContent()).getBaseTypeName()
                        );
                        xsdFactory.createComplexTypeWithSimpleContentTop(newSplittedName, splitted);
                        ((XmlSchemaSimpleContentRestriction) simpleContent.getContent()).setBaseTypeName(
                                new QName(schema.getTargetNamespace(), newSplittedName)
                        );

                        contentForAttrs = splitted;
                        // TODO: create ref by SchemaNodeFactory?
                    } else if (simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension){
                        contentForAttrs = (XmlSchemaSimpleContentExtension)simpleContent.getContent();
                    }

                    complexType.setContentModel(simpleContent);

                    if (contentForAttrs != null) {
                        for (XMData xAttr : xAttrs) {
                            contentForAttrs.getAttributes().add((XmlSchemaAttributeOrGroupRef)
                                    convertTreeInt(xAttr, false)
                                            .orElseThrow(() -> new XsdTreeAdapterException("Required attribute node not created."))
                            );
                        }
                    }
                } else {
                    adapterCtx.getReportWriter().warning(XSD.XSD024);
                    LOG.warn("{}Content of XText is not simple!", logHeader(TRANSFORMATION, xElem));
                }
            } else if (childrenKind == XNode.XMSELECTOR_END) {
                if (stackPopCounter > 0) {
                    stackPopCounter--;
                } else {
                    if (!particleStack.empty()) {
                        currParticle.set(particleStack.pop());
                        if (currParticle.get() != null && currParticle.get() instanceof XmlSchemaChoiceWrapper) {
                            final XmlSchemaChoiceWrapper currParticleChoice = (XmlSchemaChoiceWrapper)currParticle.get();
                            if (currParticleChoice.hasTransformDirection()) {
                                currParticleChoice.updateOccurrence(adapterCtx);
                            }
                        }

                        if (!particleStack.empty()) {
                            currParticle.set(particleStack.peek());
                        }
                    } else {
                        adapterCtx.getReportWriter().warning(XSD.XSD025);
                        LOG.warn("{}Group particle stack is empty, but it should not be!", logHeader(TRANSFORMATION, xElem));
                    }
                }
            } else {

                if (childrenKind == XNode.XMELEMENT
                        && topLevel
                        && !((XElement)xnChild).isReference()
                        && Xd2XsdUtils.isAnyElement((XElement)xnChild)) {
                    XElement xElemChild = ((XElement)xnChild);
                    if (!xElemChild._attrs.isEmpty()) {
                        for (XMData attr : xElemChild.getAttrs()) {
                            complexType.getAttributes().add(
                                    (XmlSchemaAttributeOrGroupRef) convertTreeInt(attr, false)
                                            .orElseThrow(() -> new XsdTreeAdapterException("Required attribute node not created."))
                            );
                        }
                    }
                }

                convertTreeInt(xnChild, false).ifPresent(xsdChild -> {
                    LOG.debug("{}Add child to particle of element.", logHeader(TRANSFORMATION, xElem));
                    currParticle.set(createDefaultParticleGroup(currParticle.get(), particleStack, xElem));
                    addNodeToParticleGroup(currParticle.get(), xsdChild);
                });
            }
        }

        if (currParticle.get() != null) {
            complexType.setParticle(
                    currParticle.get() instanceof AbstractXmlSchemaGroupParticleWrapper
                            ? ((AbstractXmlSchemaGroupParticleWrapper) currParticle.get()).xsd()
                            : currParticle.get());
        }

        postProcessor.elementComplexType(complexType, xElem);

        if (complexType.getContentModel() == null) {
            LOG.debug("{}Add attributes to complex content of element", logHeader(TRANSFORMATION, xElem));

            Arrays.stream(xAttrs).forEach(xdefAttr -> OptionalExt.of(convertTreeInt(xdefAttr, false))
                    .ifPresent(xmlSchemaObject ->
                            complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef)xmlSchemaObject)
                    ).orElse(() ->
                    LOG.debug("{}X-definition attribute not transformed. attrXDefPos='{}'.",
                            logHeader(TRANSFORMATION, xElem), xdefAttr.getXDPosition())));
        }

        return complexType;
    }

    /**
     * Creates xs:group reference based on X-Definition mixed node with reference
     * @param xChildrenNodes        X-Definition children nodes
     * @param currGroup             current XML Schema particle group node
     * @param groups                XML Schema particle group node stack
     * @param defEl                 source X-Definition element
     * @return  null if node is not using reference
     *          instance of XmlSchemaGroupRef if node is only child of element {@code defEl}
     *          instance of XmlSchemaParticle if node is not only child of element {@code defEl}
     */
    private XmlSchemaParticle createGroupReference(final XNode[] xChildrenNodes,
                                                   @Nullable XmlSchemaParticle currGroup,
                                                   final Stack<XmlSchemaParticle> groups,
                                                   final XElement defEl) {
        LOG.info("{}Creating group reference.", logHeader(TRANSFORMATION, defEl));
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xChildrenNodes[0].getXDPosition());
        String refNodePath = XsdNameUtils.getXNodePath(xChildrenNodes[0].getXDPosition());
        if (refNodePath.endsWith("/$mixed")) {
            refNodePath = refNodePath.substring(0, refNodePath.lastIndexOf("/"));
        }

        final SchemaNode refNode = adapterCtx.findSchemaNode(systemId, refNodePath).orElse(null);

        if (refNode == null || !refNode.getXsdNode().isPresent()) {
            adapterCtx.getReportWriter().error(XSD.XSD010, xChildrenNodes[0].getXDPosition());
            LOG.error("{}X-definition mixed type is reference, but no XML Schema node reference has been found internally! " +
                    "xDefNodePath='{}'", logHeader(TRANSFORMATION, defEl), xChildrenNodes[0].getXDPosition());
        } else if (!refNode.isXsdGroup()) {
            adapterCtx.getReportWriter().error(XSD.XSD011, xChildrenNodes[0].getXDPosition());
            LOG.error("{}XML Schema mixed type reference is not complex type! xDefNodePath='{}'",
                    logHeader(TRANSFORMATION, defEl), xChildrenNodes[0].getXDPosition());
        } else {
            final XmlSchemaGroup group = refNode.toXsdGroup();
            final XmlSchemaGroupRef groupRef = xsdFactory.createGroupRef(group.getQName());

            SchemaNodeFactory.createGroupRefNode(defEl, groupRef, refNode, adapterCtx);

            if (refNode.isXdElem() && refNode.toXdElem()._childNodes.length == xChildrenNodes.length) {
                return groupRef;
            } else {
                currGroup = createDefaultParticleGroup(currGroup, groups, defEl);
                addNodeToParticleGroup(currGroup, groupRef);

                if (group.getParticle() instanceof XmlSchemaAll) {
                    final XmlSchemaChoiceWrapper newGroupChoice = new XmlSchemaChoiceWrapper(
                            postProcessor.groupParticleAllToChoice((XmlSchemaAll)group.getParticle(),
                                    false)
                    );

                    group.setParticle(newGroupChoice.xsd());
                    // We have to use occurence on groupRef element
                    groupRef.setMinOccurs(newGroupChoice.xsd().getMinOccurs());
                    groupRef.setMaxOccurs(newGroupChoice.xsd().getMaxOccurs());
                    newGroupChoice.xsd().setMinOccurs(1);
                    newGroupChoice.xsd().setMaxOccurs(1);
                }

                return currGroup;
            }
        }

        return null;
    }

    /**
     * Transforms group particles to be valid by XML Schema rules
     * @param particleStack     group particles stack
     * @param prev              previous particle
     * @param newGroupParticle  currently created new group particle
     * @return number of particles which has been popped-out from group particle stack
     */
    private int updateGroupParticles(final Stack<XmlSchemaParticle> particleStack,
                                     @Nullable XmlSchemaParticle prev,
                                     final AbstractXmlSchemaGroupParticleWrapper newGroupParticle) {
        int stackPopCounter = 0;
        particleStack.push(newGroupParticle);

        do {
            XmlSchemaParticle curr = particleStack.peek();

            if (!(prev instanceof AbstractXmlSchemaGroupParticleWrapper)
                    || !(curr instanceof AbstractXmlSchemaGroupParticleWrapper)) {
                break;
            }

            final AbstractXmlSchemaGroupParticleWrapper<XmlSchemaGroupParticle, XmlSchemaObjectBase> cCurr =
                    (AbstractXmlSchemaGroupParticleWrapper) curr;
            final AbstractXmlSchemaGroupParticleWrapper<XmlSchemaGroupParticle, XmlSchemaObjectBase> cPrev =
                    (AbstractXmlSchemaGroupParticleWrapper) prev;
            boolean merge = false;

            if (cCurr.xsd() instanceof XmlSchemaAll) {
                if (cPrev.xsd() instanceof XmlSchemaAll) {
                    cCurr.addItems(cPrev.getItems());
                    merge = true;
                } else {
                    final XmlSchemaChoiceWrapper newGroupChoice = postProcessor.groupParticleAllToChoice(
                            XmlSchemaChoiceWrapper.TransformDirection.BOTTOM_UP);
                    if (newGroupChoice != null) {
                        replaceLastGroupParticle(particleStack, newGroupChoice);
                    }
                }
            } else if (cPrev.xsd() instanceof XmlSchemaAll) {
                final XmlSchemaChoiceWrapper newGroupChoice = postProcessor.groupParticleAllToChoice(
                        XmlSchemaChoiceWrapper.TransformDirection.TOP_DOWN);
                if (newGroupChoice != null) {
                    particleStack.pop();
                    replaceLastGroupParticle(particleStack, newGroupChoice);
                    pushGroupParticleToStack(particleStack, newGroupChoice, newGroupParticle);
                }
            } else {
                addNodeToParticleGroup(prev, newGroupParticle);
            }

            if (!merge) {
                break;
            }

            stackPopCounter++;
            particleStack.pop();
            prev = replaceLastGroupParticle(particleStack, cCurr);
        } while (!particleStack.empty());

        return stackPopCounter;
    }

    /**
     * Replaces the newest particle in particles stack by {@code newGroupParticle}
     * @param particleStack         particle stack
     * @param newGroupParticle      new group particle which will be inserted into stack
     * @return  previous particle
     */
    private static XmlSchemaParticle replaceLastGroupParticle(final Stack<XmlSchemaParticle> particleStack,
                                                              final AbstractXmlSchemaGroupParticleWrapper newGroupParticle) {
        XmlSchemaParticle prev = null;
        if (!particleStack.empty()) {
            particleStack.pop();
            prev = particleStack.empty() ? null : particleStack.peek();
            if (prev != null) {
                addNodeToParticleGroup(prev, newGroupParticle);
            }
        }

        particleStack.push(newGroupParticle);
        return prev;
    }

    /**
     * Creates default particle if no group particle is created yet
     * @param currParticle      current particle
     * @param particleStack     particle stack
     * @param xElem             parent X-Definition element node
     * @return  if currParticle == null, then newly created instance of CXmlSchemaSequence
     *          else currParticle
     */
    private XmlSchemaParticle createDefaultParticleGroup(@Nullable XmlSchemaParticle currParticle,
                                                         final Stack<XmlSchemaParticle> particleStack,
                                                         final XElement xElem) {
        if (currParticle == null) {
            LOG.debug("{}Particle group is undefined. Creating sequence particle by default.",
                    logHeader(TRANSFORMATION, xElem));
            currParticle = new XmlSchemaSequenceWrapper(new XmlSchemaSequence());
            particleStack.push(currParticle);
        }

        return currParticle;
    }

    /**
     * Push new group particle into particle stack and set the group particle as child of current particle
     * @param particleStack     particle stack
     * @param currParticle      current particle
     * @param newGroupParticle  new group particle
     */
    private static void pushGroupParticleToStack(final Stack<XmlSchemaParticle> particleStack,
                                                 final XmlSchemaParticle currParticle,
                                                 final AbstractXmlSchemaGroupParticleWrapper newGroupParticle) {
        addNodeToParticleGroup(currParticle, newGroupParticle);
        particleStack.push(newGroupParticle);
    }

    /**
     * Insert XML Schema node into current particle
     * @param currParticle  current particle
     * @param xsdNode       XML Schema node
     */
    private static void addNodeToParticleGroup(final XmlSchemaParticle currParticle, XmlSchemaObject xsdNode) {
        if (xsdNode instanceof AbstractXmlSchemaGroupParticleWrapper) {
            xsdNode = ((AbstractXmlSchemaGroupParticleWrapper)xsdNode).xsd();
        }

        if (currParticle instanceof AbstractXmlSchemaGroupParticleWrapper) {
            ((AbstractXmlSchemaGroupParticleWrapper)currParticle).addItem(xsdNode);
        }
    }

}
