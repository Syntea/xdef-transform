package org.xdef.transform.xsd.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.model.impl.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;
import org.xdef.transform.xsd.schema2xd.util.XdNamespaceUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;

import static org.xdef.transform.xsd.def.NamespaceConst.XDEF_DEFAULT_NAMESPACE_URI;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ATTR_NAME;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ATTR_ROOT_ELEMT;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_ANY;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_CHOICE;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_DECLARATION;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_MIXED;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_POOL;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_SEQUENCE;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdDefinitions.XD_ELEM_XDEF;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

public class XdNodeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XdNodeFactory.class);

    /**
     * X-definition adapter context
     */
    private final XdAdapterCtx adapterCtx;

    /**
     * Output X-Definition document
     */
    private Document doc;

    public XdNodeFactory(XdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates file header of output XML X-Definition document
     * @return file header
     */
    public static String createFileHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
    }

    /**
     * Creates X-Definition pool node in root of document
     * @return X-Definition pool node
     */
    public Element createPool() {
        doc = KXmlUtils.newDocument(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_POOL, null);
        return doc.getDocumentElement();
    }

    /**
     * Creates and initializes X-Definition node in root of document
     * @param xDefName          X-definition name
     * @param rootNodeName      X-definition root node's names
     * @return X-Definition node
     */
    public Element createRootXDefinition(final String xDefName, final String rootNodeName) {
        LOG.info("{}X-definition node in root. rootNodeName='{}'", logHeader(TRANSFORMATION, xDefName), rootNodeName);
        doc = KXmlUtils.newDocument(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_XDEF, null);
        final Element xDefRoot = doc.getDocumentElement();
        initializeXDefinitionNode(xDefRoot, xDefName, rootNodeName);
        return xDefRoot;
    }

    /**
     * Creates and initializes X-Definition node
     * @param xDefName          X-definition name
     * @param rootNodeName      X-definition root node's names
     * @return X-Definition node
     */
    public Element createXDefinition(final String xDefName, final String rootNodeName) {
        LOG.info("{}Creating X-definition node. rootNodeName='{}'", logHeader(TRANSFORMATION, xDefName), rootNodeName);
        final Element xDef = doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_XDEF);
        initializeXDefinitionNode(xDef, xDefName, rootNodeName);
        return xDef;
    }

    /**
     * Creates X-Definition element node based on XML Schema element node
     * @param xsdElem       XML Schema element node
     * @param xDefName      X-definition name
     * @return X-Definition element node
     */
    public Element createElement(final XmlSchemaElement xsdElem, final String xDefName) {
        if (xsdElem.isRef()) {
            final QName xsdQName = xsdElem.getRef().getTargetQName();
            if (xsdQName != null) {
                final Element xdElem = doc.createElementNS(
                        xsdQName.getNamespaceURI(),
                        XdNameUtils.createQualifiedName(xsdQName));
                final String refXDef = XdNamespaceUtils.findReferenceSchemaName(
                        xsdElem.getParent().getParent(),
                        xsdQName,
                        adapterCtx,
                        false
                ).orElse(null);

                XdAttributeFactory.addAttrRefInDiffXDef(xdElem, refXDef, xsdQName);
                return xdElem;
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD215);
                LOG.warn("{}Unknown element reference QName!", logHeader(TRANSFORMATION, xsdElem));
            }
        } else {
            final QName xsdQName = xsdElem.getQName();
            if (xsdQName == null) {
                return doc.createElement(xsdElem.getName());
            } else if (xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdElem.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                return doc.createElementNS(xsdQName.getNamespaceURI(), qualifiedName);
            }
        }

        return doc.createElement(xsdElem.getName());
    }

    /**
     * Creates empty X-Definition element node based on XML Schema complex type node
     * @param xsdComplex        XML Schema complex type node
     * @param xDefName          X-definition name
     * @return X-Definition element node
     */
    public Element createEmptyElement(final XmlSchemaComplexType xsdComplex, final String xDefName) {
        final QName xsdQName = xsdComplex.getQName();
        if (xsdQName.getNamespaceURI() != null) {
            final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
            return doc.createElementNS(xsdQName.getNamespaceURI(), qualifiedName);
        } else {
            return doc.createElement(xsdQName.getLocalPart());
        }
    }

    /**
     * Creates empty X-Definition declaration node
     * @return <xd:declaration/>
     */
    public Element createEmptyDeclaration() {
        return doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_DECLARATION);
    }

    /**
     * Creates empty X-Definition sequence node
     * @return <xd:sequence/>
     */
    public Element createEmptySequence() {
        return doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_SEQUENCE);
    }

    /**
     * Creates empty X-Definition choice node
     * @return <xd:choice/>
     */
    public Element createEmptyChoice() {
        return doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_CHOICE);
    }

    /**
     * Creates empty X-Definition mixed node
     * @return <xd:mixed/>
     */
    public Element createEmptyMixed() {
        return doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_MIXED);
    }

    /**
     * Creates empty X-Definition named mixed node. Used for transformation of group of elements
     * @param name      Name of mixed node
     * @return <xd:mixed name="{@code name}"/>
     */
    public Element createEmptyNamedMixed(final String name) {
        final Element elem = doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_MIXED);
        XdAttributeFactory.addAttr(elem, XD_ATTR_NAME, name);
        return elem;
    }

    /**
     * Creates empty X-Definition any node
     * @return <xd:any/>
     */
    public Element createEmptyAny() {
        return doc.createElementNS(XDEF_DEFAULT_NAMESPACE_URI, XD_ELEM_ANY);
    }

    /**
     * Initializes given root node of X-Definition. Set name and root node names
     * @param xDef              X-definition root node
     * @param xDefName          X-definition name
     * @param rootNodesName     X-definition root node's names
     */
    private void initializeXDefinitionNode(final Element xDef, final String xDefName, final String rootNodesName) {
        XdAttributeFactory.addAttr(xDef, XD_ATTR_NAME, xDefName);
        if (rootNodesName != null && !rootNodesName.isEmpty()) {
            XdAttributeFactory.addAttr(xDef, XD_ATTR_ROOT_ELEMT, rootNodesName);
        }
    }
}
