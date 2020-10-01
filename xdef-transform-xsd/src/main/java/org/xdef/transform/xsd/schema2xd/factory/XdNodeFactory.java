package org.xdef.transform.xsd.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.model.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;
import org.xdef.transform.xsd.schema2xd.util.XdNamespaceUtils;
import org.xdef.transform.xsd.util.SchemaLogger;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;

import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_NAME;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_ROOT_ELEMT;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_ANY;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_CHOICE;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_DECLARATION;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_MIXED;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_POOL;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_SEQUENCE;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ELEM_XDEF;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_NAMESPACE_URI;
import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_INFO;
import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;

public class XdNodeFactory {

    /**
     * X-definition adapter context
     */
    private final XdAdapterCtx adapterCtx;

    /**
     * Output x-definition document
     */
    private Document doc;

    public XdNodeFactory(XdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates file header of output XML x-definition document
     * @return file header
     */
    public static String createFileHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
    }

    /**
     * Creates x-definition pool node in root of document
     * @return x-definition pool node
     */
    public Element createPool() {
        doc = KXmlUtils.newDocument(XD_NAMESPACE_URI, XD_ELEM_POOL, null);
        return doc.getDocumentElement();
    }

    /**
     * Creates and initializes x-definition node in root of document
     * @param xDefName          X-definition name
     * @param rootNodesName     X-definition root node's names
     * @return x-definition node
     */
    public Element createRootXdefinition(final String xDefName, final String rootNodesName) {
        SchemaLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "X-definition node in root");
        doc = KXmlUtils.newDocument(XD_NAMESPACE_URI, XD_ELEM_XDEF, null);
        final Element xDefRoot = doc.getDocumentElement();
        initializeXDefinitionNode(xDefRoot, xDefName, rootNodesName);
        return xDefRoot;
    }

    /**
     * Creates and initializes x-definition node
     * @param xDefName          X-definition name
     * @param rootNodesName     X-definition root node's names
     * @return x-definition node
     */
    public Element createXDefinition(final String xDefName, final String rootNodesName) {
        SchemaLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "X-definition node");
        final Element xDef = doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_XDEF);
        initializeXDefinitionNode(xDef, xDefName, rootNodesName);
        return xDef;
    }

    /**
     * Creates x-definition element node based on XSD element node
     * @param xsdElem       XSD element node
     * @param xDefName      X-definition name
     * @return x-definition element node
     */
    public Element createElement(final XmlSchemaElement xsdElem, final String xDefName) {
        if (xsdElem.isRef()) {
            final QName xsdQName = xsdElem.getRef().getTargetQName();
            if (xsdQName != null) {
                final Element xdElem = doc.createElementNS(xsdQName.getNamespaceURI(), XdNameUtils.createQualifiedName(xsdQName));
                final String refXDef = XdNamespaceUtils.findReferenceSchemaName(xsdElem.getParent().getParent(), xsdQName, adapterCtx, false);
                XdAttributeFactory.addAttrRefInDiffXDef(xdElem, refXDef, xsdQName);
                return xdElem;
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD215);
                SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdElem, "Unknown element reference QName!");
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
     * Creates empty x-definition element node based on XSD complex type node
     * @param xsdComplex        XSD complex type node
     * @param xDefName          X-definition name
     * @return x-definition element node
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
     * Creates empty x-definition declaration node
     * @return <xd:declaration/>
     */
    public Element createEmptyDeclaration() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_DECLARATION);
    }

    /**
     * Creates empty x-definition sequence node
     * @return <xd:sequence/>
     */
    public Element createEmptySequence() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_SEQUENCE);
    }

    /**
     * Creates empty x-definition choice node
     * @return <xd:choice/>
     */
    public Element createEmptyChoice() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_CHOICE);
    }

    /**
     * Creates empty x-definition mixed node
     * @return <xd:mixed/>
     */
    public Element createEmptyMixed() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_MIXED);
    }

    /**
     * Creates empty x-definition named mixed node. Used for transformation of group of elements
     * @param name      Name of mixed node
     * @return <xd:mixed name="{@paramref name}"/>
     */
    public Element createEmptyNamedMixed(final String name) {
        final Element elem = doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_MIXED);
        XdAttributeFactory.addAttr(elem, XD_ATTR_NAME, name);
        return elem;
    }

    /**
     * Creates empty x-definition any node
     * @return <xd:any/>
     */
    public Element createEmptyAny() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_ANY);
    }

    /**
     * Initializes given root node of x-definition. Set name and root node names
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
