package org.xdef.transform.xsd.xd2schema.factory;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaUse;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.model.XMOccurrence;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.StringFormatter;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.model.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.xsd.CXmlSchemaAll;
import org.xdef.transform.xsd.xd2schema.model.xsd.CXmlSchemaChoice;
import org.xdef.transform.xsd.xd2schema.model.xsd.CXmlSchemaGroupParticle;
import org.xdef.transform.xsd.xd2schema.model.xsd.CXmlSchemaSequence;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_FACET_MIN_LENGTH;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_ELEM_FACTORY;

/**
 * Basic factory for creating XSD nodes
 */
public class XsdNodeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XsdNodeFactory.class);

    /**
     * Output XSD document
     */
    private final XmlSchema schema;

    /**
     * XSD adapter context
     */
    private final XsdAdapterCtx adapterCtx;

    public XsdNodeFactory(XmlSchema schema, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates empty XSD element node with occurrence
     * @param xElem         x-definition element node
     * @param topLevel      flag if x-definition node is placed on top level
     * @return <xs:element/>
     */
    public XmlSchemaElement createEmptyElement(final XElement xElem, boolean topLevel) {
        LOG.trace("{}Empty element. topLevel={}", logHeader(XSD_ELEM_FACTORY), topLevel);
        XmlSchemaElement elem = new XmlSchemaElement(schema, topLevel);

        if (topLevel == false) {
            elem.setMinOccurs(xElem.getOccurence().minOccurs());
            elem.setMaxOccurs((xElem.isUnbounded() || xElem.isMaxUnlimited()) ? Long.MAX_VALUE : xElem.getOccurence().maxOccurs());
        }

        if (xElem._nillable == 'T') {
            elem.setNillable(true);
        }

        return elem;
    }

    /**
     * Creates empty XSD attribute node with use
     * @param xData         x-definition attribute node
     * @param topLevel      flag if x-definition node is placed on top level
     * @return <xs:attribute/>
     */
    public XmlSchemaAttribute createEmptyAttribute(final XData xData, boolean topLevel) {
        LOG.trace("{}Attribute element. topLevel={}", logHeader(XSD_ELEM_FACTORY), topLevel);
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, topLevel);

        if (topLevel == false) {
            if (xData.isRequired() || xData.getOccurence().isRequired()) {
                attr.setUse(XmlSchemaUse.REQUIRED);
            }
        }

        return attr;
    }

    /**
     * Creates empty XSD complex-type node
     * @param topLevel          flag if x-definition node is placed on top level
     * @return <xs:complexType/>
     */
    public XmlSchemaComplexType createEmptyComplexType(boolean topLevel) {
        LOG.trace("{}Empty complex-type. topLevel={}", logHeader(XSD_ELEM_FACTORY), topLevel);
        return new XmlSchemaComplexType(schema, topLevel);
    }

    /**
     * Creates empty XSD simple-type node
     * @param topLevel          flag if x-definition node is placed on top level
     * @return <xs:simpleType/>
     */
    public XmlSchemaSimpleType createEmptySimpleType(boolean topLevel) {
        LOG.trace("{}Empty simple-type. topLevel={}", logHeader(XSD_ELEM_FACTORY), topLevel);
        return new XmlSchemaSimpleType(schema, topLevel);
    }

    /**
     * Creates XSD any node with occurrence
     * @param xElem             x-definition element node
     * @return <xs:any/>
     */
    public XmlSchemaAny createAny(final XElement xElem) {
        LOG.trace("{}Any", logHeader(XSD_ELEM_FACTORY));
        final XmlSchemaAny any = new XmlSchemaAny();
        any.setMinOccurs(xElem.getOccurence().minOccurs());
        any.setMaxOccurs((xElem.isUnbounded() || xElem.isMaxUnlimited()) ? Long.MAX_VALUE : xElem.getOccurence().maxOccurs());
        return any;
    }

    /**
     * Creates XSD simple-type node on top level of XSD document
     * @param xData             x-definition attribute/text node
     * @param refTypeName       reference type name
     */
    public void createSimpleTypeTop(final XData xData, final String refTypeName) {
        LOG.trace("{}Simple-type top. refTypeName='{}'", logHeader(XSD_ELEM_FACTORY, xData), refTypeName);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(true);
        itemType.setName(refTypeName);
        itemType.setContent(createSimpleTypeContent(xData, refTypeName));
    }

    /**
     * Creates XSD simple-type node (attribute)
     * @param xData             x-definition attribute/text node
     * @param nodeName          simple-type name
     * @return <xs:simpleType>...</xs:simpleType>
     */
    public XmlSchemaSimpleType createAttributeSimpleType(final XData xData, final String nodeName) {
        LOG.trace("{}Simple-type attribute. nodeName='{}'", logHeader(XSD_ELEM_FACTORY, xData), nodeName);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setContent(createSimpleTypeContent(xData, nodeName));
        itemType.setName(XsdNameFactory.createLocalSimpleTypeName(xData));
        return itemType;
    }

    /**
     * Creates XSD anonymous simple-type node
     * @param xData             x-definition attribute/text node
     * @param nodeName          simple-type name
     * @return <xs:simpleType>...</xs:simpleType>
     */
    public XmlSchemaSimpleType createAnonymousSimpleType(final XData xData, final String nodeName) {
        LOG.trace("{}Simple-type anonymous (text node). nodeName='{}'=", logHeader(XSD_ELEM_FACTORY, xData), nodeName);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setContent(createSimpleTypeContent(xData, nodeName));
        return itemType;
    }

    /**
     * Creates XSD simple content node
     * @param xDataText         x-definition text node
     * @return  if reference and parser are unknown, then null
     *          else <xs:simpleContent><xs:extension base="...">...</xs:extension></xs:simpleContent>
     */
    public XmlSchemaSimpleContent createTextBasedSimpleContent(final XData xDataText, boolean topLevel) {
        LOG.trace("{}Simple-content with extension. topLevel='{}'", logHeader(XSD_ELEM_FACTORY, xDataText), topLevel);

        QName qName = null;

        final UniqueConstraint uniqueConstraint = adapterCtx.findUniqueConst(xDataText);
        if (uniqueConstraint != null) {
            LOG.info("{}Simple-content is using unique set. uniqueSet='{}'",
                    logHeader(TRANSFORMATION, xDataText), uniqueConstraint.getName());
            final UniqueConstraint.Type type = XsdNameUtils.getUniqueSetVarType(xDataText.getValueTypeName());
            if (UniqueConstraint.isStringConstraint(type)) {
                qName = Constants.XSD_STRING;
            }
        }

        boolean extension = true;

        if (qName == null) {
            if (xDataText.getRefTypeName() != null && topLevel) {
                final String refTypeName = XsdNameFactory.createLocalSimpleTypeName(xDataText);
                final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refTypeName);
                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                qName = new QName(nsUri, refTypeName);
                LOG.debug("{}Simple-content using reference. nsUri='{}', localName='{}'",
                        logHeader(XSD_ELEM_FACTORY, xDataText), nsUri, refTypeName);
            } else {
                if (!topLevel) {
                    LOG.debug("{}Simple-content using reference on non top-level - anonymous will be created.",
                            logHeader(XSD_ELEM_FACTORY, xDataText));
                }

                qName = Xd2XsdParserMapping.getDefaultParserQName(xDataText, adapterCtx, false);
                final XDValue parseMethod = xDataText.getParseMethod();
                if (parseMethod instanceof XDParser) {
                    // Restriction facets
                    final XDNamedValue[] items = ((XDParser) parseMethod).getNamedParams().getXDNamedItems();
                    // Check if restriction contains only min-length equals 1 and string is optional => then use xs:extension instead of xs:restriction
                    if (items.length == 0 ||
                            (items.length == 1 && Constants.XSD_STRING.equals(qName) &&
                            XSD_FACET_MIN_LENGTH.equals(items[0].getName()) && items[0].getValue().intValue() == 1 && xDataText.isOptional())) {
                        extension = true;
                    } else {
                        extension = false;
                    }
                }
            }

            if (qName == null) {
                final String refParserName = XsdNameUtils.createRefNameFromParser(xDataText, adapterCtx);
                if (refParserName != null) {
                    qName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, refParserName);
                    LOG.debug("{}Simple-content using parser. refParserName='{}'",
                            logHeader(XSD_ELEM_FACTORY, xDataText), refParserName);
                }
            } else {
                LOG.debug("{}Simple-content using simple parser. localName='{}'",
                        logHeader(XSD_ELEM_FACTORY, xDataText), qName.getLocalPart());
            }
        }

        if (qName != null) {
            XmlSchemaContent schemaContent;
            if (extension) {
                schemaContent = createEmptySimpleContentExtension(qName);
                LOG.info("{}Simple-content extension type. qName='{}'", logHeader(XSD_ELEM_FACTORY, xDataText), qName);
            } else {
                schemaContent = createEmptySimpleContentRestriction(qName);
                LOG.info("{}Simple-content restriction type. qName='{}'", logHeader(XSD_ELEM_FACTORY, xDataText), qName);

                // Copy facets
                final XmlSchemaSimpleTypeContent simpleTypeContent = createSimpleTypeContent(xDataText, null);
                if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
                    ((XmlSchemaSimpleContentRestriction)schemaContent).getFacets().addAll(
                            ((XmlSchemaSimpleTypeRestriction)simpleTypeContent).getFacets());
                }
            }

            final XmlSchemaSimpleContent content = createSimpleContent(schemaContent);
            if (uniqueConstraint != null) {
                schemaContent.setAnnotation(XsdNodeFactory.createAnnotation(
                        StringFormatter.format("Original part of uniqueSet: {} ({})",
                                uniqueConstraint.getPath(),  xDataText.getValueTypeName()),
                        adapterCtx)
                );
            }
            return content;
        }

        return null;
    }

    /**
     * Creates XSD group particle node with occurrence
     * @param xNode             x-definition group node
     * @return based on {@paramref xNode}
     *          <xs:sequence/>
     *          <xs:choice/>
     *          <xs:all/>
     */
    public CXmlSchemaGroupParticle createGroupParticle(final XMNode xNode) {
        final short groupType = xNode.getKind();
        final XMOccurrence occurrence = xNode.getOccurence();

        LOG.trace("{}createGroupParticle. particle='{}'",
                logHeader(XSD_ELEM_FACTORY), Xd2XsdUtils.particleXKindToString(groupType));

        CXmlSchemaGroupParticle particle;
        switch (groupType) {
            case XNode.XMSEQUENCE: {
                particle = new CXmlSchemaSequence(new XmlSchemaSequence());
                break;
            }
            case XNode.XMMIXED: {
                particle = new CXmlSchemaAll(new XmlSchemaAll());
                break;
            }
            case XNode.XMCHOICE: {
                particle = new CXmlSchemaChoice(new XmlSchemaChoice());
                break;
            }
            default: {
                LOG.error("{}createGroupParticle. particle='{}'",
                        logHeader(XSD_ELEM_FACTORY), Xd2XsdUtils.particleXKindToString(groupType));
                throw new SRuntimeException(XSD.XSD044, Xd2XsdUtils.particleXKindToString(groupType));
            }
        }

        particle.xsd().setMinOccurs(occurrence.minOccurs());
        particle.xsd().setMaxOccurs((occurrence.isUnbounded() || occurrence.isMaxUnlimited()) ? Long.MAX_VALUE : occurrence.maxOccurs());

        return particle;
    }

    /**
     * Creates empty XSD group node
     * @param name          group name
     * @return <xs:group name="@{paramref name}"/>
     */
    public XmlSchemaGroup createEmptyGroup(final String name) {
        LOG.trace("{}createEmptyGroup. name='{}'", logHeader(XSD_ELEM_FACTORY), name);
        final XmlSchemaGroup group = new XmlSchemaGroup(schema);
        group.setName(name);
        return group;
    }

    /**
     * Creates XSD group reference node
     * @param qName         reference QName
     * @return <xs:group ref="@{paramref qName}"/>
     */
    public XmlSchemaGroupRef createGroupRef(final QName qName) {
        LOG.trace("{}createGroupRef. qName='{}'", logHeader(XSD_ELEM_FACTORY), qName);
        final XmlSchemaGroupRef groupRef = new XmlSchemaGroupRef();
        groupRef.setRefName(qName);
        return groupRef;
    }

    /**
     * Creates XSD complex type node with complex extension on top level of XSD document
     * @param complexTypeName       complex type name
     * @param extQName              complex extension QName
     * @return  <xs:complexType name="@{paramref complexTypeName}">
     *              <xs:complexContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:complexContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithComplexExtensionTop(final String complexTypeName, final QName extQName) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaComplexContent complexContent = createComplexContentWithComplexExtension(extQName);
        complexType.setContentModel(complexContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex content node with extension
     * @param qName             complex extension QName
     * @return  <xs:complexContent>
     *              <xs:extension base="@{paramref qName}"></xs:extension>
     *          </xs:complexContent>
     */
    private XmlSchemaComplexContent createComplexContentWithComplexExtension(final QName qName) {
        final XmlSchemaComplexContentExtension complexContentExtension = createEmptyComplexContentExtension(qName);
        final XmlSchemaComplexContent complexContent = createComplexContent(complexContentExtension);
        return complexContent;
    }

    /**
     * Creates XSD complex type node with simple content on top level of XSD document
     * @param complexTypeName       complex type name
     * @param schemaContent         simple content
     * @return  <xs:complexType name="@{paramref simpleTypeName}">
     *              <xs:simpleContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:simpleContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithSimpleContentTop(final String complexTypeName, final XmlSchemaContent schemaContent) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaSimpleContent simpleContent = createSimpleContent(schemaContent);
        complexType.setContentModel(simpleContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex type node with simple extension on top level of XSD document
     * @param complexTypeName       complex type name
     * @param extQName              simple extension QName
     * @return  <xs:complexType name="@{paramref simpleTypeName}">
     *              <xs:simpleContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:simpleContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithSimpleExtensionTop(final String complexTypeName, final QName extQName) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaSimpleContentExtension simpleContentExtension = createEmptySimpleContentExtension(extQName);
        final XmlSchemaSimpleContent simpleContent = createSimpleContent(simpleContentExtension);
        complexType.setContentModel(simpleContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex content node
     * @param content       schema content
     * @return  <xs:complexContent>@{paramref content}</xs:complexContent>
     */
    public XmlSchemaComplexContent createComplexContent(final XmlSchemaContent content) {
        LOG.trace("{}Complex content. contentType='{}'", logHeader(XSD_ELEM_FACTORY), content.getClass().getSimpleName());
        final XmlSchemaComplexContent complexContent = new XmlSchemaComplexContent();
        complexContent.setContent(content);
        return complexContent;
    }

    /**
     * Creates XSD simple content node
     * @param content       schema content
     * @return  <xs:simpleContent>@{paramref content}</xs:simpleContent>
     */
    public XmlSchemaSimpleContent createSimpleContent(final XmlSchemaContent content) {
        LOG.trace("{}Simple content. contentType='{}'", logHeader(XSD_ELEM_FACTORY), content.getClass().getSimpleName());
        final XmlSchemaSimpleContent simpleContent = new XmlSchemaSimpleContent();
        simpleContent.setContent(content);
        return simpleContent;
    }

    /**
     * Creates empty XSD complex content extension node
     * @param baseType      context extension base
     * @return <xs:extension base="@{paramref baseType}"/>
     */
    public XmlSchemaComplexContentExtension createEmptyComplexContentExtension(final QName baseType) {
        LOG.trace("{}Complex content extension. baseType='{}'", logHeader(XSD_ELEM_FACTORY), baseType);
        final XmlSchemaComplexContentExtension contentExtension = new XmlSchemaComplexContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates empty XSD simple content extension node
     * @param baseType      simple extension base
     * @return <xs:extension base="@{paramref baseType}"/>
     */
    public XmlSchemaSimpleContentExtension createEmptySimpleContentExtension(final QName baseType) {
        LOG.trace("{}Simple content extension. baseType='{}'", logHeader(XSD_ELEM_FACTORY), baseType);
        final XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates empty XSD simple content restriction node
     * @param baseType      simple restriction base
     * @return <xs:restriction base="@{paramref baseType}"/>
     */
    public XmlSchemaSimpleContentRestriction createEmptySimpleContentRestriction(final QName baseType) {
        LOG.trace("{}Simple content restriction. baseType='{}'", logHeader(XSD_ELEM_FACTORY), baseType);
        final XmlSchemaSimpleContentRestriction contentExtension = new XmlSchemaSimpleContentRestriction();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates XSD simple content node
     * @param xData             x-definition attribute/text node
     * @param nodeName          node name (required for <xs:union/> node)
     * @return  if x-definition node has known parser, then based on parser
     *              <xs:restriction base="...">...</xs:restriction>
     *              <xs:list itemType="...">...</xs:list>
     *              <xs:union memberTypes="...">...</xs:union>
     *          else <xs:restriction base="xs:string"/>
     */
    private XmlSchemaSimpleTypeContent createSimpleTypeContent(final XData xData, final String nodeName) {
        LOG.trace("{}Simple-type content. nodeName='{}'", logHeader(XSD_ELEM_FACTORY, xData), nodeName);

        final XDValue parseMethod = xData.getParseMethod();
        final XsdSimpleContentFactory simpleContentFactory = new XsdSimpleContentFactory(this, adapterCtx, xData);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            simpleContentFactory.setParameters(parser.getNamedParams().getXDNamedItems());
            return simpleContentFactory.createSimpleContent(nodeName, xData.getKind() == XMATTRIBUTE);
        }

        return simpleContentFactory.createDefaultRestriction();
    }

    /**
     * Creates XSD document import node
     * @param schema        output XSD document
     * @param nsUri         import namespace URI
     * @param location      import schema location
     */
    public void createSchemaImport(final XmlSchema schema, final String nsUri, final String location) {
        final XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(nsUri);
        schemaImport.setSchemaLocation(location);
    }

    /**
     * Creates XSD document include node
     * @param schema        output XSD document
     * @param location      include schema location
     */
    public void createSchemaInclude(final XmlSchema schema, final String location) {
        final XmlSchemaInclude schemaImport = new XmlSchemaInclude(schema);
        schemaImport.setSchemaLocation(location);
    }

    /**
     * Creates XSD annotation node with single documentation node
     * @param annotationValue   annotation value
     * @param adapterCtx        XSD adapter context
     * @return <xs:annotation><xs:documentation>@{paramref annotationValue}</xs:documentation></xs:annotation>
     */
    public static XmlSchemaAnnotation createAnnotation(final String annotationValue, final XsdAdapterCtx adapterCtx) {
        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ANNOTATION)) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            annotation.getItems().add(createAnnotationItem(annotationValue, adapterCtx));
            return annotation;
        }

        return null;
    }

    /**
     * Creates XSD annotation node with multiple documentation nodes
     * @param annotationValues  list of annotation values
     * @param adapterCtx        XSD adapter context
     * @return  <xs:annotation>
     *              <xs:documentation>@{paramref annotationValue[0]}</xs:documentation>
     *              <xs:documentation>@{paramref annotationValue[1]}</xs:documentation>
     *              ...
     *              <xs:documentation>@{paramref annotationValue[n-1]}</xs:documentation>
     *          </xs:annotation>
     */
    public static XmlSchemaAnnotation createAnnotation(final List<String> annotationValues, final XsdAdapterCtx adapterCtx) {
        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ANNOTATION)) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            for (String value : annotationValues) {
                annotation.getItems().add(createAnnotationItem(value, adapterCtx));
            }
            return annotation;
        }

        return null;
    }

    /**
     * Creates XSD documentation node
     * @param docValue      documentation value
     * @return <xs:documentation>@{paramref docValue}</xs:documentation>
     */
    private static XmlSchemaDocumentation createAnnotationItem(final String docValue, final XsdAdapterCtx adapterCtx) {
        if (docValue == null || docValue.isEmpty()) {
            return null;
        }

        final XmlSchemaDocumentation annotationItem = new XmlSchemaDocumentation();

        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();
            final Element rootElement = doc.createElement("documentation");
            doc.appendChild(rootElement);
            rootElement.appendChild(doc.createTextNode(docValue));
            annotationItem.setMarkup(rootElement.getChildNodes());
        } catch (ParserConfigurationException ex) {
            adapterCtx.getReportWriter().warning(XSD.XSD035, ex.getMessage());
            LOG.warn(StringFormatter.format("{}Error occurs while creating XSD documentation node: ",
                    logHeader(TRANSFORMATION)),
                    ex);
        }

        return annotationItem;
    }
}
