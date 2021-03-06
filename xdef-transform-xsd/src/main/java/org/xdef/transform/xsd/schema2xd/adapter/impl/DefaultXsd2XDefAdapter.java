package org.xdef.transform.xsd.schema2xd.adapter.impl;


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
import org.xdef.transform.xsd.model.impl.DefaultNamespace;
import org.xdef.transform.xsd.model.Namespace;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.adapter.Xsd2XDefAdapter;
import org.xdef.transform.xsd.schema2xd.factory.XdAttributeFactory;
import org.xdef.transform.xsd.schema2xd.factory.XdNodeFactory;
import org.xdef.transform.xsd.schema2xd.model.NamespaceMap;
import org.xdef.transform.xsd.schema2xd.model.impl.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;
import org.xdef.transform.xsd.schema2xd.util.XdNamespaceUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_URI;
import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdLoggerDefs.XD_ADAPTER;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

/**
 * Transforms XML Schema to X-Definition or x-definiton pool
 */
public class DefaultXsd2XDefAdapter extends AbstractXsd2XdAdapter implements Xsd2XDefAdapter<XmlSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultXsd2XDefAdapter.class);

    /**
     * X-definition XML node factory
     */
    private XdNodeFactory elementFactory;

    @Override
    public String createXDefinition(final XmlSchema rootSchema, final String xDefName) {
        if (rootSchema == null) {
            reportWriter.error(XSD.XSD002);
            LOG.error("{}Input XML Schema document is not set!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        XmlSchema[] schemas;
        if (rootSchema.getParent() != null) {
            schemas = rootSchema.getParent().getXmlSchemas();
        } else {
            schemas = new XmlSchema[1];
            schemas[0] = rootSchema;
        }

        if (schemas == null || schemas.length <= 0) {
            reportWriter.error(XSD.XSD200);
            LOG.error("{}Input XML Schema document collection is empty!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        final List<XmlSchema> schemaList = Arrays.stream(schemas).collect(Collectors.toList());

        adapterCtx = new XdAdapterCtx(features, reportWriter);
        elementFactory = new XdNodeFactory(adapterCtx);

        adapterCtx.init();

        final List<XmlSchema> schemasToBeProcessed = initializeSchemas(schemaList, rootSchema, xDefName);

        if (schemasToBeProcessed.isEmpty()) {
            reportWriter.error(XSD.XSD201);
            LOG.error("{}No XML Schema document to be processed found!", logHeader(INITIALIZATION, xDefName));
            return "";
        }

        initializeNamespaces(schemasToBeProcessed);

        Element xdRootElem;

        if (schemasToBeProcessed.size() > 1) {
            LOG.info("{}Creating X-Definition collection ...", logHeader(TRANSFORMATION, XD_ADAPTER));
            xdRootElem = elementFactory.createPool();

            // First transform root XML Schema document
            xdRootElem.appendChild(createXDef(xDefName, rootSchema, true));

            for (XmlSchema schema : schemasToBeProcessed) {
                if (rootSchema.equals(schema)) {
                    continue;
                }

                final String schemaName = adapterCtx.findXmlSchemaFileName(schema);
                xdRootElem.appendChild(createXDef(schemaName, schema, true));
            }
        } else {
            LOG.info("{}Creating single X-Definition ...", logHeader(TRANSFORMATION, XD_ADAPTER));
            xdRootElem = createXDef(xDefName, rootSchema, false);
        }

        return XdNodeFactory.createFileHeader() + KXmlUtils.nodeToString(xdRootElem, true);
    }

    /**
     * Transforms given XML Schema node tree into X-Definition node tree
     *
     * Output will be saved in {@code xdDefRootElem}
     *
     * @param treeAdapter       XML Schema to X-Definition adapter to be used
     * @param xdDefRootElem     X-Definition root node
     * @param xDefName          X-Definition name
     * @param schema            input XML Schema document to be transformed
     */
    private void transformXsdTree(final Xsd2XdTreeAdapter treeAdapter,
                                  final Element xdDefRootElem,
                                  final String xDefName,
                                  final XmlSchema schema) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Transformation of XML Schema tree", logHeader(TRANSFORMATION, xDefName));
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
     * Initializes XML Schema document's name
     * @param schemas       XML Schema documents
     * @param rootSchema    XML Schema root document
     * @param xDefName      output X-Definition name
     * @return XML Schema document name per XML Schema document
     */
    private Map<XmlSchema, String> initializeSchemaNames(final List<XmlSchema> schemas,
                                                         final XmlSchema rootSchema,
                                                         final String xDefName) {
        final Map<XmlSchema, String> schemaFileNameMap = new HashMap<>();

        initializeSchemaName(rootSchema, xDefName, schemaFileNameMap);

        schemas.forEach(schema -> {
            for (XmlSchemaObject xmlNode : schema.getItems()) {
                if (xmlNode instanceof XmlSchemaExternal) {
                    final XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal)xmlNode;
                    initializeSchemaName(
                            xmlSchemaExternal.getSchema(),
                            xmlSchemaExternal.getSchemaLocation(),
                            schemaFileNameMap
                    );
                }
            }
        });

        return schemaFileNameMap;
    }

    /**
     * Initializes XML Schema document name
     * @param schema            XML Schema document to be initialized
     * @param schemaLocation    XML Schema document location
     * @param schemaFileNames   map of XML Schema document names
     */
    private void initializeSchemaName(final XmlSchema schema,
                                      final String schemaLocation,
                                      final Map<XmlSchema, String> schemaFileNames) {
        final String currSchemaFileName = schemaFileNames.get(schema);
        final String locationSchemaFileName = XdNameUtils.getSchemaName(schemaLocation);

        if (currSchemaFileName == null) {
            LOG.info("{}Add schema name. locationSchemaFileName='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER), locationSchemaFileName);
            schemaFileNames.put(schema, locationSchemaFileName);
        } else if (!locationSchemaFileName.equals(currSchemaFileName)) {
            reportWriter.warning(XSD.XSD210, currSchemaFileName, locationSchemaFileName);
            LOG.warn("{}Schema already exists, but with different name! currSchemaFileName='{}', newSchemaFileName='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER), currSchemaFileName, locationSchemaFileName);
        } else {
            LOG.debug("{}Schema already exists. name='{}'", logHeader(PREPROCESSING, XD_ADAPTER), locationSchemaFileName);
        }
    }

    /**
     * Gather basic data from input XML Schema documents and initializes XML Schema documents
     * @param schemas       XML Schema documents
     * @param rootSchema    XML Schema root document
     * @param xDefName      output X-Definition name
     * @return Collection of input XML Schemas
     */
    private List<XmlSchema> initializeSchemas(final List<XmlSchema> schemas,
                                              final XmlSchema rootSchema,
                                              final String xDefName) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Schemas pre-processing", logHeader(XD_ADAPTER));
        LOG.info(HEADER_LINE);

        final Map<XmlSchema, String> schemaFileNameMap = initializeSchemaNames(schemas, rootSchema, xDefName);
        /**
         * Key:     serialized XML Schema content
         * Value:
         *      Left:   XML Schema file name
         *      Right:  XML Schema
         */
        final Map<String, Pair<String, XmlSchema>> xmlSchemaContent = new HashMap<>();
        final List<XmlSchema> schemasToBeProcessed = new LinkedList<>();

        schemas.forEach(schema -> {
            if (XML_SCHEMA_DEFAULT_NAMESPACE_URI.equals(schema.getTargetNamespace())) {
                return;
            }

            final ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            final String schemaFileName = schemaFileNameMap.get(schema);

            try {
                schema.write(byteOS);
                final String serializedXmlSchema = byteOS.toString();

                final Pair<String, XmlSchema> contentInfo = xmlSchemaContent.get(serializedXmlSchema);

                if (contentInfo == null) {
                    LOG.debug("{}Add schema to be processed. schemaFileName='{}'",
                            logHeader(PREPROCESSING, XD_ADAPTER), schemaFileName);

                    xmlSchemaContent.put(serializedXmlSchema, Pair.of(schemaFileName, schema));
                    schemasToBeProcessed.add(schema);
                } else {
                    if (rootSchema.equals(schema)) {
                        LOG.debug("{}Remove schema from processing. schemaFileName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), contentInfo.getKey());

                        schemasToBeProcessed.remove(contentInfo.getRight());

                        LOG.debug("{}Add schema to be processed. schemaFileName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), schemaFileName);
                        xmlSchemaContent.put(serializedXmlSchema, Pair.of(schemaFileName, schema));

                        schemasToBeProcessed.add(schema);
                    } else {
                        LOG.warn("{}Schema is already defined. schemaFileName='{}', originalName='{}'",
                                logHeader(PREPROCESSING, XD_ADAPTER), schemaFileName, contentInfo.getKey());

                        xmlSchemaContent.put(serializedXmlSchema, contentInfo);
                    }
                }

                adapterCtx.addXmlSchemaFileName(schema, schemaFileName);
            } catch (UnsupportedEncodingException e) {
                reportWriter.error(XSD.XSD202, schemaFileName);
                LOG.error("{}Unsuccessful loading of XML Schema document. schemaFileName='{}'",
                        logHeader(PREPROCESSING, XD_ADAPTER), schemaFileName);
            }
        });

        LOG.info("{}Schemas to be processed. count={}, inputCount={}",
                logHeader(PREPROCESSING, XD_ADAPTER),
                schemasToBeProcessed.size(),
                Optional.ofNullable(rootSchema.getParent()).map(s -> schemas.size() - 1).orElse(schemas.size())
        );

        return schemasToBeProcessed;
    }

    /**
     * Initializes XML Schema documents used namespaces
     * @param schemaToBeProcessed   XML Schema documents to be initialized
     */
    private void initializeNamespaces(final List<XmlSchema> schemaToBeProcessed) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Namespace initialization", logHeader(XD_ADAPTER));
        LOG.info(HEADER_LINE);

        schemaToBeProcessed.forEach(xmlSchema -> {
            // X-Definition name equals to schema file name
            final String xDefName = adapterCtx.findXmlSchemaFileName(xmlSchema);
            final Namespace targetNamespace = getTargetNamespace(xmlSchema)
                    .orElse(DefaultNamespace.createEmptyNamespace());

            adapterCtx.addTargetNamespace(xDefName, targetNamespace);

            final NodeNamespaceContext namespaceCtx = (NodeNamespaceContext)xmlSchema.getNamespaceContext();
            for (String prefix : namespaceCtx.getDeclaredPrefixes()) {
                if (XdNamespaceUtils.isDefaultNamespacePrefix(prefix)) {
                    continue;
                }

                final String uri = namespaceCtx.getNamespaceURI(prefix);
                adapterCtx.addNamespace(xDefName, prefix, uri);
            }
        });
    }

    /**
     * Gets target namespace info of given XML Schema document
     * @param schema    XML Schema document
     * @return  target namespace info(prefix, URI) if XML Schema document is using target namespace
     *          otherwise {@link Optional#empty()}
     */
    private Optional<Namespace> getTargetNamespace(final XmlSchema schema) {
        if (schema.getTargetNamespace() == null || schema.getTargetNamespace().isEmpty()) {
            return Optional.empty();
        }

        final NamespacePrefixList namespaceCtx = schema.getNamespaceContext();
        final String nsPrefix = namespaceCtx.getPrefix(schema.getTargetNamespace());
        return Optional.of(new DefaultNamespace(nsPrefix, schema.getTargetNamespace()));
    }

    /**
     * Add namespaces to X-Definition root node
     * @param xdRootElem    X-Definition root node
     * @param xDefName      X-Definition name
     */
    private void addNamespaces(final Element xdRootElem, final String xDefName) {
        final List<Namespace> namespaces = adapterCtx.findNamespaces(xDefName)
                .map(NamespaceMap::getNamespaces)
                .orElse(Collections.emptyList());

        namespaces.forEach(namespace -> {
            String attributeName = Constants.XMLNS_ATTRIBUTE;
            if (!namespace.getPrefix().isEmpty()) {
                attributeName += NAMESPACE_DELIMITER + namespace.getPrefix();
            }

            XdAttributeFactory.addAttr(xdRootElem, attributeName, namespace.getUri());
        });
    }

    /**
     * Creates XML node structure of X-Definition from given XML Schema document
     * @param schemaName    XML Schema document name (output X-Definition name)
     * @param schema        input XML Schema document
     * @param pool          flag, if X-Definition is part of X-Definition pool
     * @return root node of X-Definition
     */
    private Element createXDef(final String schemaName, final XmlSchema schema, final boolean pool) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Creating X-Definition. name='{}'", logHeader(XD_ADAPTER), schemaName);
        LOG.info(HEADER_LINE);

        final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(schemaName, schema, elementFactory, adapterCtx);
        final String rootElements = treeAdapter.loadXsdRootElementNames();
        final Element xdDefRootElem = pool
                ? elementFactory.createXDefinition(schemaName, rootElements)
                : elementFactory.createRootXDefinition(schemaName, rootElements);

        addNamespaces(xdDefRootElem, schemaName);
        transformXsdTree(treeAdapter, xdDefRootElem, schemaName, schema);
        return xdDefRootElem;
    }

}
