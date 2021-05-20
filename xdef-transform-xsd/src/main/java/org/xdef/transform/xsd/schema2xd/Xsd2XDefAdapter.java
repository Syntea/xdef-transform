package org.xdef.transform.xsd.schema2xd;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.NodeNamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.adapter.AbstractXsd2XdAdapter;
import org.xdef.transform.xsd.schema2xd.adapter.Xsd2XdTreeAdapter;
import org.xdef.transform.xsd.schema2xd.factory.XdAttributeFactory;
import org.xdef.transform.xsd.schema2xd.factory.XdNodeFactory;
import org.xdef.transform.xsd.schema2xd.model.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;
import org.xdef.transform.xsd.schema2xd.util.XdNamespaceUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdLoggerDefs.XD_ADAPTER;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;

/**
 * Transforms XSD schema to x-definition or x-definiton pool
 */
public class Xsd2XDefAdapter extends AbstractXsd2XdAdapter implements Schema2XDefAdapter<XmlSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(Xsd2XDefAdapter.class);

    /**
     * X-definition XML node factory
     */
    private XdNodeFactory elementFactory;

    @Override
    public String createXDefinition(final XmlSchema rootSchema, final String xDefName) {
        if (rootSchema == null) {
            reportWriter.error(XSD.XSD002);
            LOG.error("{}Input XSD document is not set!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        XmlSchema[] schemas;
        if (rootSchema.getParent() != null) {
            schemas = rootSchema.getParent().getXmlSchemas();
        } else {
            schemas = new XmlSchema[1];
            schemas[0] = rootSchema;
        }

        if (schemas == null || schemas.length < 1) {
            reportWriter.error(XSD.XSD200);
            LOG.error("{}Input XSD document collection is empty!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        adapterCtx = new XdAdapterCtx(features, reportWriter);
        elementFactory = new XdNodeFactory(adapterCtx);

        adapterCtx.init();

        final ArrayList<XmlSchema> schemasToBeProcessed = initializeSchemas(schemas, rootSchema, xDefName);

        if (schemasToBeProcessed.isEmpty()) {
            reportWriter.error(XSD.XSD201);
            LOG.error("{}No XSD document to be processed found!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        initializeNamespaces(schemasToBeProcessed);

        Element xdRootElem;

        if (schemasToBeProcessed.size() > 1) {
            LOG.info("{}Creating x-definition collection ...", logHeader(TRANSFORMATION, XD_ADAPTER));
            xdRootElem = elementFactory.createPool();

            // First transform root XSD document
            xdRootElem.appendChild(createXDef(xDefName, rootSchema, true));

            for (XmlSchema schema : schemasToBeProcessed) {
                if (rootSchema.equals(schema)) {
                    continue;
                }

                final String schemaName = adapterCtx.findXmlSchemaName(schema);
                xdRootElem.appendChild(createXDef(schemaName, schema, true));
            }
        } else {
            LOG.info("{}Creating single x-definition ...", logHeader(TRANSFORMATION, XD_ADAPTER));
            xdRootElem = createXDef(xDefName, rootSchema, false);
        }

        return XdNodeFactory.createFileHeader() + KXmlUtils.nodeToString(xdRootElem, true);
    }

    /**
     * Transforms given XSD node tree into x-definition node tree
     *
     * Output will be saved in {@paramref xdDefRootElem}
     *
     * @param treeAdapter       XSD to x-definition adapter to be used
     * @param xdDefRootElem     x-definition root node
     * @param xDefName          x-definition name
     * @param schema            input XSD document to be transformed
     */
    private void transformXsdTree(final Xsd2XdTreeAdapter treeAdapter,
                                  final Element xdDefRootElem,
                                  final String xDefName,
                                  final XmlSchema schema) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Transformation of XSD tree", logHeader(TRANSFORMATION, xDefName));
        LOG.info(HEADER_LINE);

        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null && !schemaTypeMap.isEmpty()) {
            for (XmlSchemaType xsdSchemaType : schemaTypeMap.values()) {
                treeAdapter.convertTree(xsdSchemaType, xdDefRootElem);
            }
        }

        final Map<QName, XmlSchemaGroup> groupMap = schema.getGroups();
        if (groupMap != null && !groupMap.isEmpty()) {
            for (XmlSchemaGroup xsdGroup : groupMap.values()) {
                treeAdapter.convertTree(xsdGroup, xdDefRootElem);
            }
        }

        final Map<QName, XmlSchemaElement> elementMap = schema.getElements();

        if (elementMap != null && !elementMap.isEmpty()) {
            for (XmlSchemaElement xsdElem : elementMap.values()) {
                treeAdapter.convertTree(xsdElem, xdDefRootElem);
            }
        }
    }

    /**
     * Initializes XSD document's name
     * @param schemas       XSD documents
     * @param rootSchema    XSD root document
     * @param xDefName      output x-definition name
     * @return XSD document name per XSD document
     */
    private Map<XmlSchema, String> initializeSchemaNames(final XmlSchema[] schemas, final XmlSchema rootSchema, final String xDefName) {
        final Map<XmlSchema, String> schemaNames = new HashMap<XmlSchema, String>();

        initializeSchemaName(rootSchema, xDefName, schemaNames);

        for (int i = 0; i < schemas.length; i++) {
            final XmlSchema schema = schemas[i];
            for (XmlSchemaObject xmlNode : schema.getItems()) {
                if (xmlNode instanceof XmlSchemaExternal) {
                    initializeSchemaName(((XmlSchemaExternal) xmlNode).getSchema(), ((XmlSchemaExternal) xmlNode).getSchemaLocation(), schemaNames);
                }
            }
        }

        return schemaNames;
    }

    /**
     * Initializes XSD document name
     * @param schema            XSD document to be initialized
     * @param schemaLocation    XSD document location
     * @param schemaNames       map of XSD document names
     */
    private void initializeSchemaName(final XmlSchema schema, final String schemaLocation, final Map<XmlSchema, String> schemaNames) {
        final String refSchemaSavedName = schemaNames.get(schema);
        final String refSchemaFileName = XdNameUtils.getSchemaName(schemaLocation);
        if (refSchemaSavedName == null) {
            schemaNames.put(schema, refSchemaFileName);
            LOG.info("{}Add schema name. refSchemaSavedName='{}'", logHeader(PREPROCESSING, XD_ADAPTER), refSchemaFileName);
        } else if (!refSchemaFileName.equals(refSchemaSavedName)) {
            reportWriter.warning(XSD.XSD210, refSchemaSavedName, refSchemaFileName);
            LOG.warn("{}Schema already exists, but with different name! originalRefSchemaName='{}', currRefSchemaName='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER), refSchemaSavedName, refSchemaFileName);
        } else {
            LOG.debug("{}Schema already exists. name='{}'", logHeader(PREPROCESSING, XD_ADAPTER), refSchemaFileName);
        }
    }

    /**
     * Gather basic data from input XSD documents and initializes XSD documents
     * @param schemas       XSD documents
     * @param rootSchema    XSD root document
     * @param xDefName      output x-definition name
     * @return
     */
    private ArrayList<XmlSchema> initializeSchemas(final XmlSchema[] schemas, final XmlSchema rootSchema, final String xDefName) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Schemas pre-processing", logHeader(XD_ADAPTER));
        LOG.info(HEADER_LINE);

        final Map<XmlSchema, String> schemaNames = initializeSchemaNames(schemas, rootSchema, xDefName);
        final Map<String, Pair<String, XmlSchema>> xmlSchemaContent = new HashMap<String, Pair<String, XmlSchema>>();
        final ArrayList<XmlSchema> schemasToBeProcessed = new ArrayList<XmlSchema>();

        for (int i = 0; i < schemas.length; i++) {
            final XmlSchema schema = schemas[i];

            if (Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
                continue;
            }

            final ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            final String schemaName = schemaNames.get(schema);

            try {
                schema.write(byteOS);
                final String xsdStr = new String(byteOS.toByteArray());

                final Pair<String, XmlSchema> contentInfo = xmlSchemaContent.get(xsdStr);
                if (contentInfo == null) {
                    xmlSchemaContent.put(xsdStr, Pair.of(schemaName, schema));
                    schemasToBeProcessed.add(schema);
                    LOG.debug("{}Add schema to be processed. schemaName='{}'",
                            logHeader(PREPROCESSING, XD_ADAPTER), schemaName);
                } else {
                    if (rootSchema.equals(schema)) {
                        schemasToBeProcessed.remove(contentInfo.getValue());
                        LOG.debug("{}Remove schema from processing. schemaName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), contentInfo.getKey());
                        xmlSchemaContent.put(xsdStr, Pair.of(schemaName, schema));
                        schemasToBeProcessed.add(schema);
                        LOG.debug("{}Add schema to be processed. schemaName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), schemaName);
                    } else {
                        xmlSchemaContent.put(xsdStr, contentInfo);
                        LOG.debug("{}Schema is already defined. schemaName='{}', originalName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), schemaName, contentInfo.getKey());
                    }
                }

                adapterCtx.addXmlSchemaName(schema, schemaName);
            } catch (UnsupportedEncodingException e) {
                reportWriter.error(XSD.XSD202, schemaName);
                LOG.error("{}Unsuccessful loading of XSD document. schemaName='{}'",
                        logHeader(PREPROCESSING, XD_ADAPTER), schemaName);
            }
        }

        LOG.info("{}Schemas to be processed. count={}, inputCount={}",
                logHeader(PREPROCESSING, XD_ADAPTER),
                schemasToBeProcessed.size(),
                rootSchema.getParent() != null ? schemas.length - 1 : schemas.length
        );

        return schemasToBeProcessed;
    }

    /**
     * Initializes XSD documents used namespaces
     * @param schemaToBeProcessed   XSD documents to be initialized
     */
    private void initializeNamespaces(ArrayList<XmlSchema> schemaToBeProcessed) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Namespace initialization", logHeader(XD_ADAPTER));
        LOG.info(HEADER_LINE);

        for (XmlSchema schema : schemaToBeProcessed) {
            final String xDefName = adapterCtx.findXmlSchemaName(schema);
            final Pair<String, String> targetNamespace = getTargetNamespace(schema);
            if (targetNamespace != null) {
                adapterCtx.addTargetNamespace(xDefName, targetNamespace);
            } else {
                adapterCtx.addTargetNamespace(xDefName, Pair.of("", ""));
            }

            final NodeNamespaceContext namespaceCtx = (NodeNamespaceContext)schema.getNamespaceContext();
            for (String prefix : namespaceCtx.getDeclaredPrefixes()) {
                if (XdNamespaceUtils.isDefaultNamespacePrefix(prefix)) {
                    continue;
                }

                final String uri = namespaceCtx.getNamespaceURI(prefix);
                adapterCtx.addNamespace(xDefName, prefix, uri);
            }
        }
    }

    /**
     * Gets target namespace info of given XSD document
     * @param schema    XSD document
     * @return target namespace info(prefix, URI) if XSD document is using target namespace, otherwise null
     */
    private Pair<String, String> getTargetNamespace(final XmlSchema schema) {
        if (schema.getTargetNamespace() == null || schema.getTargetNamespace().isEmpty()) {
            return null;
        }

        final NamespacePrefixList namespaceCtx = schema.getNamespaceContext();
        final String nsPrefix = namespaceCtx.getPrefix(schema.getTargetNamespace());
        return Pair.of(nsPrefix, schema.getTargetNamespace());
    }

    /**
     * Add namespaces to x-definition root node
     * @param xdRootElem    x-definition root node
     * @param xDefName      x-definition name
     */
    private void addNamespaces(final Element xdRootElem, final String xDefName) {
        Map<String, String> namespaces = adapterCtx.findNamespaces(xDefName);
        if (namespaces != null && !namespaces.isEmpty()) {
            for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
                if (!namespace.getValue().isEmpty()) {
                    XdAttributeFactory.addAttr(xdRootElem, Constants.XMLNS_ATTRIBUTE + ":" + namespace.getValue(), namespace.getKey());
                } else {
                    XdAttributeFactory.addAttr(xdRootElem, Constants.XMLNS_ATTRIBUTE, namespace.getKey());
                }
            }
        }
    }

    /**
     * Creates XML node structure of x-definition from given XSD document
     * @param schemaName    XSD document name (output x-definition name)
     * @param schema        input XSD document
     * @param pool          flag, if x-definition is part of x-definition pool
     * @return root node of x-definition
     */
    private Element createXDef(final String schemaName, final XmlSchema schema, final boolean pool) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Creating x-definition. name='{}'", logHeader(XD_ADAPTER), schemaName);
        LOG.info(HEADER_LINE);

        final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(schemaName, schema, elementFactory, adapterCtx);
        final String rootElements = treeAdapter.loadXsdRootElementNames();
        final Element xdDefRootElem = pool ? elementFactory.createXDefinition(schemaName, rootElements) : elementFactory.createRootXdefinition(schemaName, rootElements);

        addNamespaces(xdDefRootElem, schemaName);
        transformXsdTree(treeAdapter, xdDefRootElem, schemaName, schema);
        return xdDefRootElem;
    }

}
