package org.xdef.transform.xsd.schema2xd.adapter;


import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAllMember;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.model.Namespace;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.factory.XdAttributeFactory;
import org.xdef.transform.xsd.schema2xd.factory.XdDeclarationBuilder;
import org.xdef.transform.xsd.schema2xd.factory.XdDeclarationFactory;
import org.xdef.transform.xsd.schema2xd.factory.XdNodeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.transform.xsd.schema2xd.model.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNamespaceUtils;
import org.xdef.transform.xsd.schema2xd.util.Xsd2XdUtils;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;

/**
 * Transforms XSD tree node structure to x-definition tree node structure
 */
public class Xsd2XdTreeAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Xsd2XdTreeAdapter.class);

    /**
     * Output x-definition name
     */
    final private String xDefName;

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML node factory
     */
    final private XdNodeFactory xdFactory;

    /**
     * X-definition adapter context
     */
    final private XdAdapterCtx adapterCtx;

    /**
     * X-definition XML attribute factory
     */
    final private XdAttributeFactory xdAttrFactory;

    /**
     * X-definition XML declaration factory
     */
    final private XdDeclarationFactory xdDeclarationFactory;

    public Xsd2XdTreeAdapter(String xDefName, XmlSchema schema, XdNodeFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.xDefName = xDefName;
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
        xdDeclarationFactory = new XdDeclarationFactory(schema, xdFactory, adapterCtx);
        xdAttrFactory = new XdAttributeFactory(adapterCtx, xdDeclarationFactory);
    }

    /**
     * Gathers names of all XSD top level element nodes
     * @return concatenate names in required format of x-definition
     */
    public String loadXsdRootElementNames() {
        LOG.info("{}Loading root elements of XSD", logHeader(PREPROCESSING, xDefName));

        String targetNsPrefix = NAMESPACE_PREFIX_EMPTY;
        final Optional<Namespace> targetNamespaceOpt = adapterCtx.findTargetNamespace(xDefName);
        if (targetNamespaceOpt.isPresent() && !targetNamespaceOpt.get().isEmptyPrefix()) {
            targetNsPrefix = targetNamespaceOpt.get().getPrefix() + NAMESPACE_DELIMITER;
        }

        final Map<QName, XmlSchemaElement> rootElements = schema.getElements();
        if (rootElements == null || rootElements.isEmpty()) {
            return "";
        }

        final String finalTargetNsPrefix = targetNsPrefix;
        final String rootElement = rootElements.values().stream()
                .map(xmlSchemaElement -> finalTargetNsPrefix + xmlSchemaElement.getName())
                .collect(Collectors.joining(" | "));

        return rootElement;
    }

    /**
     * Transforms XSD node tree into x-definition node tree.
     * @param xsdNode       XSD document node
     * @param parentNode    parent x-definition node
     */
    public void convertTree(final XmlSchemaObjectBase xsdNode, final Element parentNode) {
        if (xsdNode instanceof XmlSchemaElement) {
            createElement((XmlSchemaElement)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaType) {
            if (xsdNode instanceof XmlSchemaSimpleType) {
                final XdDeclarationBuilder builder = xdDeclarationFactory.createBuilder()
                        .setSimpleType((XmlSchemaSimpleType)xsdNode)
                        .setParentNode(parentNode)
                        .setType(IDeclarationTypeFactory.Type.TOP_DECL);

                xdDeclarationFactory.createDeclaration(builder);
            } else if (xsdNode instanceof XmlSchemaComplexType) {
                createTopNonRootElement((XmlSchemaComplexType)xsdNode, parentNode);
            }
        } else if (xsdNode instanceof XmlSchemaGroupParticle) {
            createGroupParticle((XmlSchemaGroupParticle)xsdNode, parentNode, false);
        } else if (xsdNode instanceof XmlSchemaGroup) {
            createElementGroup((XmlSchemaGroup)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaGroupRef) {
            createElementGroupRef((XmlSchemaGroupRef)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaAny) {
            createAny((XmlSchemaAny)xsdNode, parentNode);
        }
    }

    /**
     * Creates x-definition element node based od XSD element node
     * @param xsdElementNode    XSD element node
     * @param parentNode        parent x-definition node
     */
    private void createElement(final XmlSchemaElement xsdElementNode, final Element parentNode) {
        LOG.info("{}Creating element ...", logHeader(TRANSFORMATION, xsdElementNode));

        final Element xdElem = xdFactory.createElement(xsdElementNode, xDefName);

        final QName xsdElemQName = xsdElementNode.getSchemaTypeName();
        if (xsdElemQName != null && NAMESPACE_PREFIX_EMPTY.equals(xsdElemQName.getPrefix())) {
            if (xsdElementNode.getSchemaType() != null) {
                if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                    LOG.info("{}Element is referencing to complex type. reference='{}'",
                            logHeader(TRANSFORMATION, xsdElementNode), xsdElemQName);

                    if (!externalRef(xsdElemQName, xdElem, false)) {
                        xdAttrFactory.addAttrRef(xdElem, xsdElemQName);
                    }
                } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                    LOG.info("{}Element is referencing to simple type. reference='{}'",
                            logHeader(TRANSFORMATION, xsdElementNode), xsdElemQName);

                    final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) xsdElementNode.getSchemaType();
                    if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        final XdDeclarationBuilder b = xdDeclarationFactory.createBuilder()
                                .setSimpleType(simpleType)
                                .setBaseType(xsdElemQName)
                                .setType(IDeclarationTypeFactory.Type.TEXT_DECL);

                        xdElem.setTextContent(xdDeclarationFactory.createDeclarationContent(b));
                    }
                }
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD211, xsdElemQName);
                LOG.warn("{}Element reference has not found! reference='{}'", logHeader(TRANSFORMATION, xsdElementNode), xsdElemQName);
            }
        } else if (xsdElementNode.getSchemaType() != null) {
            if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                createElementFromComplex(xdElem, (XmlSchemaComplexType)xsdElementNode.getSchemaType());
            } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)xsdElementNode.getSchemaType();
                if (xsdElemQName != null
                        && (Constants.XSD_NMTOKENS.equals(xsdElemQName) || Constants.XSD_IDREFS.equals(xsdElemQName))) {
                    xdElem.setTextContent(xdDeclarationFactory.createSimpleTextDeclaration(xsdElemQName));
                } else {
                    final XdDeclarationBuilder builder = xdDeclarationFactory.createBuilder()
                            .setSimpleType(simpleType)
                            .setBaseType(xsdElemQName)
                            .setType(IDeclarationTypeFactory.Type.TEXT_DECL);

                    xdElem.setTextContent(xdDeclarationFactory.createDeclarationContent(builder));
                }
            }
        }

        if (xdElem != null) {
            xdAttrFactory.addOccurrence(xdElem, xsdElementNode);
            xdAttrFactory.addAttrNillable(xdElem, xsdElementNode);
        }

        parentNode.appendChild(xdElem);
    }

    /**
     * Creates x-definition element node based od XSD complex schema type node
     * This transformation is always used only for top level XSD nodes
     * @param xsdComplexNode    XSD top level complex schema type node
     * @param parentNode        parent x-definition node
     */
    private void createTopNonRootElement(final XmlSchemaComplexType xsdComplexNode, final Element parentNode) {
        LOG.info("{}Creating top level non-root element ...", logHeader(TRANSFORMATION, xsdComplexNode));

        final Element xdElem = xdFactory.createEmptyElement(xsdComplexNode, xDefName);
        createElementFromComplex(xdElem, xsdComplexNode);
        parentNode.appendChild(xdElem);
    }

    /**
     * Creates x-definition element node based od XSD complex schema type node
     * @param xdElem            x-definition node, which will be filled
     * @param xsdComplexNode    XSD complex schema type node
     */
    private void createElementFromComplex(final Element xdElem, final XmlSchemaComplexType xsdComplexNode) {
        addAttrsToElem(xdElem, xsdComplexNode.getAttributes());

        if (xsdComplexNode.getParticle() != null) {
            if (xsdComplexNode.getParticle() instanceof XmlSchemaGroupParticle) {
                createGroupParticle((XmlSchemaGroupParticle)xsdComplexNode.getParticle(), xdElem, xsdComplexNode.isMixed());
            } else {
                convertTree(xsdComplexNode.getParticle(), xdElem);
            }
        }

        if (xsdComplexNode.isMixed()) {
            xdAttrFactory.addAttrText(xdElem);
        }

        if (xsdComplexNode.getContentModel() != null) {
            if (xsdComplexNode.getContentModel() instanceof XmlSchemaSimpleContent) {
                final XmlSchemaSimpleContent xsdSimpleContent = (XmlSchemaSimpleContent)xsdComplexNode.getContentModel();
                if (xsdSimpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    final XmlSchemaSimpleContentExtension xsdSimpleExtension = (XmlSchemaSimpleContentExtension)xsdSimpleContent.getContent();
                    final QName baseType = xsdSimpleExtension.getBaseTypeName();
                    if (baseType != null) {
                        if (!externalRef(baseType, xdElem, false)) {
                            if (adapterCtx.hasEnableFeature(XD_TEXT_REQUIRED)) {
                                xdElem.setTextContent("required " + baseType.getLocalPart() + "()");
                            } else {
                                xdElem.setTextContent("optional " + baseType.getLocalPart() + "()");
                            }
                        }
                    }

                    addAttrsToElem(xdElem, xsdSimpleExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions?
            } else if (xsdComplexNode.getContentModel() instanceof XmlSchemaComplexContent) {
                final XmlSchemaComplexContent xsdComplexContent = (XmlSchemaComplexContent)xsdComplexNode.getContentModel();
                if (xsdComplexContent.getContent() instanceof XmlSchemaComplexContentExtension) {
                    final XmlSchemaComplexContentExtension xsdComplexExtension = (XmlSchemaComplexContentExtension)xsdComplexContent.getContent();
                    final QName baseType = xsdComplexExtension.getBaseTypeName();
                    if (baseType != null) {
                        if (!externalRef(baseType, xdElem, false)) {
                            XdAttributeFactory.addAttrRef(xdElem, baseType);
                        }
                    }

                    if (xsdComplexExtension.getParticle() != null) {
                        if (xsdComplexExtension.getParticle() instanceof XmlSchemaGroupParticle) {
                            createGroupParticle((XmlSchemaGroupParticle)xsdComplexExtension.getParticle(), xdElem, false);
                        }
                    }

                    addAttrsToElem(xdElem, xsdComplexExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions?
            }
        }
    }

    /**
     * Creates x-definition particle node based od XSD particle node
     *
     * Possible created output nodes: xd:sequence, xd:choice, xd:mixed
     *
     * @param xsdParticleNode   XSD group particle node
     * @param parentNode        x-definition node, which will be filled
     * @param mixed             flag, if attribute xd:text should be created
     */
    private void createGroupParticle(final XmlSchemaGroupParticle xsdParticleNode, final Element parentNode, final boolean mixed) {
        LOG.info("{}Creating group particle ...", logHeader(TRANSFORMATION, xsdParticleNode));

        Element xdParticle = null;
        if (xsdParticleNode instanceof XmlSchemaSequence) {
            final XmlSchemaSequence xsdSequence = (XmlSchemaSequence)xsdParticleNode;
            xdParticle = xdFactory.createEmptySequence();
            final List<XmlSchemaSequenceMember> xsdSequenceMembers = xsdSequence.getItems();
            if (xsdSequenceMembers != null && !xsdSequenceMembers.isEmpty()) {
                // If xs:sequence contains only element nodes, then remove sequence from x-definition
                boolean onlyElems = true;
                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (!(xsdSequenceMember instanceof XmlSchemaElement)) {
                        onlyElems = false;
                        break;
                    }
                }

                if (onlyElems) {
                    xdParticle = parentNode;
                }

                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (xsdSequenceMember instanceof XmlSchemaParticle) {
                        convertTree(xsdSequenceMember, xdParticle);
                    }
                }

                if (onlyElems) {
                    xdParticle = null;
                }
            }
        } else if (xsdParticleNode instanceof XmlSchemaChoice) {
            final XmlSchemaChoice xsdChoice = (XmlSchemaChoice)xsdParticleNode;
            xdParticle = xdFactory.createEmptyChoice();
            final List<XmlSchemaChoiceMember> xsdChoiceMembers = xsdChoice.getItems();
            if (xsdChoiceMembers != null && !xsdChoiceMembers.isEmpty()) {
                for (XmlSchemaChoiceMember xsdChoiceMember : xsdChoiceMembers) {
                    if (xsdChoiceMember instanceof XmlSchemaParticle) {
                        convertTree(xsdChoiceMember, xdParticle);
                    }
                }
            }
        } else if (xsdParticleNode instanceof XmlSchemaAll) {
            final XmlSchemaAll xsdAll = (XmlSchemaAll)xsdParticleNode;
            xdParticle = xdFactory.createEmptyMixed();
            final List<XmlSchemaAllMember> xsdAllMembers = xsdAll.getItems();
            if (xsdAllMembers != null && !xsdAllMembers.isEmpty()) {
                for (XmlSchemaAllMember xsdAllMember : xsdAllMembers) {
                    if (xsdAllMember instanceof XmlSchemaElement) {
                        convertTree(xsdAllMember, xdParticle);
                    }
                }
            }
        }

        if (xdParticle != null) {
            xdAttrFactory.addOccurrence(xdParticle, xsdParticleNode);
            if (mixed) {
                xdAttrFactory.addAttrText(xdParticle);
            }

            parentNode.appendChild(xdParticle);
        }
    }

    /**
     * Creates x-definition group of elements (xd:mixed) node based od XSD group node
     * @param xsdGroupNode      XSD group node
     * @param parentNode        x-definition node, which will be filled
     */
    private void createElementGroup(final XmlSchemaGroup xsdGroupNode, final Element parentNode) {
        LOG.debug("{}Creating group.", logHeader(TRANSFORMATION, xsdGroupNode));

        final Element group = xdFactory.createEmptyNamedMixed(xsdGroupNode.getName());
        if (xsdGroupNode.getParticle() != null) {
            createGroupParticle(xsdGroupNode.getParticle(), group, false);
        }

        parentNode.appendChild(group);
    }

    /**
     * Creates x-definition group reference (xd:mixed) node based od XSD group reference node
     * @param xsdGroupRefNode       XSD group reference node
     * @param parentNode            x-definition node, which will be filled
     */
    private void createElementGroupRef(final XmlSchemaGroupRef xsdGroupRefNode, final Element parentNode) {
        LOG.debug("{}Creating group reference.", logHeader(TRANSFORMATION, xsdGroupRefNode));

        // TODO: mixed ref cannot be part of sequence/choice/mixed? requires advanced processing
        final XmlSchemaGroup group = Xsd2XdUtils.findGroupByQName(schema, xsdGroupRefNode.getRefName()).orElse(null);
        if (group != null) {
            // Copy group content into element
            convertTree(group.getParticle(), parentNode);
            xdAttrFactory.addOccurrence((Element) parentNode.getLastChild(), xsdGroupRefNode);
        } else {
            final Element groupRef = xdFactory.createEmptyMixed();
            XdAttributeFactory.addAttrRef(groupRef, xsdGroupRefNode.getRefName());

            adapterCtx.getReportWriter().warning(XSD.XSD212);
            LOG.warn("{}Group reference possible inside sequence/choice/mixed node",
                    logHeader(TRANSFORMATION, xsdGroupRefNode));
            if (xsdGroupRefNode.getMaxOccurs() > 1) {
                adapterCtx.getReportWriter().error(XSD.XSD203);
                LOG.error("{}Group reference is using multiple occurence - prohibited in x-definition.",
                        logHeader(TRANSFORMATION, xsdGroupRefNode));
            }

            parentNode.appendChild(groupRef);
        }
    }

    /**
     * Creates x-definition any node based od XSD any node
     * @param xsdAnyNode        XSD any node
     * @param parentNode        x-definition node, which will be filled
     */
    private void createAny(final XmlSchemaAny xsdAnyNode, final Element parentNode) {
        LOG.debug("{}Creating any.", logHeader(TRANSFORMATION, xsdAnyNode));

        final Element xdAny = xdFactory.createEmptyAny();
        xdAttrFactory.addOccurrence(xdAny, xsdAnyNode);
        parentNode.appendChild(xdAny);
    }

    /**
     * Transform and add given XSD attributes {@code xsdAttrs} to x-definition element node.
     * @param xdElem        x-definition element node
     * @param xsdAttrs      XSD attribute nodes
     */
    private void addAttrsToElem(final Element xdElem, final @Nullable List<XmlSchemaAttributeOrGroupRef> xsdAttrs) {
        if (xsdAttrs != null) {
            for (XmlSchemaAttributeOrGroupRef xsdAttrRef : xsdAttrs) {
                if (xsdAttrRef instanceof XmlSchemaAttribute) {
                    xdAttrFactory.addAttr(xdElem, (XmlSchemaAttribute)xsdAttrRef, xDefName);
                }
            }
        }
    }

    /**
     * Creates x-definition attribute defining reference
     * @param baseType      reference qualified name
     * @param xdNode        x-definition element node
     * @param simple        flag, if reference is originally pointing to simple schema type in XSD document
     * @return  true if reference attribute has been successfully created
     *          false otherwise
     */
    private boolean externalRef(final QName baseType, final Element xdNode, final boolean simple) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            final String xDefRefName = XdNamespaceUtils.findReferenceSchemaName(
                    schema.getParent(),
                    baseType,
                    adapterCtx,
                    simple
            ).orElse(null);

            return externalRef(baseType, xDefRefName, xdNode);
        }

        return false;
    }

    /**
     * Creates x-definition attribute defining reference
     * @param baseType      reference qualified name
     * @param xDefRefName   name of reference x-definition
     * @param xdNode        x-definition element node
     * @return  true if x-definition attribure reference has been created
     *          false otherwise
     */
    private boolean externalRef(final QName baseType, final @Nullable String xDefRefName, final Element xdNode) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            if (xDefRefName != null && !xDefRefName.equals(xDefName)) {
                XdAttributeFactory.addAttrRefInDiffXDef(xdNode, xDefRefName, baseType);
                return true;
            }
        }

        return false;
    }
}
