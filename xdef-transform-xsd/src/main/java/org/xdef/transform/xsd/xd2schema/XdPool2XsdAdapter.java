package org.xdef.transform.xsd.xd2schema;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.model.XMDefinition;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.adapter.AbstractXd2XsdAdapter;
import org.xdef.transform.xsd.xd2schema.adapter.Xd2XsdPostProcessingAdapter;
import org.xdef.transform.xsd.xd2schema.factory.XsdSchemaFactory;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.XsdSchemaImportLocation;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdDefinitions.XSD_NAMESPACE_URI_EMPTY;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_XDPOOL_ADAPTER;

/**
 * Transformation of given x-definition pool to collection of XSD documents
 */
public class XdPool2XsdAdapter extends AbstractXd2XsdAdapter implements XdPool2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input XDPool used for transformation
     */
    private XDPool xdPool = null;

    /**
     * Target namespace per x-definition
     * Key:     x-definition name
     * Value:   namespace prefix, namespace URI
     */
    private Map<String, Pair<String, String>> xDefTargetNs = null;

    /**
     * X-definition without target namespace
     */
    private Set<String> xDefsWithoutNs = null;

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool) {
        if (xdPool == null) {
            throw new SRuntimeException(XSD.XSD047);
        }

        this.xdPool = xdPool;
        adapterCtx = new XsdAdapterCtx(features, reportWriter);
        adapterCtx.init();

        init();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final XDef2XsdAdapter adapter = createXDefAdapter();
            adapter.createSchema(xDef);
        }

        final Xd2XsdPostProcessingAdapter postProcessingAdapter = new Xd2XsdPostProcessingAdapter();
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.process(xdPool);

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Initializes transformation algorithm
     */
    private void init() {
        LOG.info(HEADER_LINE);
        LOG.info("{}Initialize", logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER));
        LOG.info(HEADER_LINE);

        initTargetNamespaces();
        initXsdSchemas();
        initSchemaLocations();
    }

    /**
     * Gathers target namespaces for all x-definitions from source x-definition pool
     */
    private void initTargetNamespaces() {
        LOG.info("{}Initialize target namespaces ...", logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER));

        xDefsWithoutNs = new HashSet<String>();
        xDefTargetNs = new HashMap<String, Pair<String, String>>();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final String xDefName = xDef.getName();
            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace((XDefinition)xDef, adapterCtx);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                if (xDefTargetNs.containsKey(xDefName)) {
                    reportWriter.warning(XSD.XSD014, xDefName);
                    LOG.warn("{}Target namespace of x-definition is already defined. xDefName='{}'",
                            logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER), xDefName);
                } else {
                    xDefTargetNs.put(xDefName, Pair.of(targetNamespace.getKey(), targetNamespace.getValue()));
                    LOG.info("{}Add target namespace to x-definition. xDefName='{}', nsPrefix='{}', nsUri='{}'",
                            logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER),
                            xDefName,
                            targetNamespace.getKey(),
                            targetNamespace.getValue());
                }
            } else {
                xDefsWithoutNs.add(xDefName);
                LOG.info("{}X-definition has no target namespace. xDefName='{}'",
                        logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER), xDefName);
            }
        }
    }

    /**
     * Initializes all XSD documents based on source x-definition from x-definition pool
     */
    private void initXsdSchemas() {
        LOG.info("{}Initialize XSD documents ...", logHeader(INITIALIZATION, XSD_XDPOOL_ADAPTER));

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            schemaFactory.createXsdSchema((XDefinition)xDef, xDefTargetNs.get(xDef.getName()));
        }
    }

    /**
     * Creates schema location context based on x-definition target namespace and x-definition name
     */
    private void initSchemaLocations() {
        LOG.info("{}Initialize schema locations ...", logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER));

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            final String xDefName = entry.getKey();
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
        }

        for (String xDefName : xDefsWithoutNs) {
            final String nsUri = XSD_NAMESPACE_URI_EMPTY;
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
            LOG.debug("{}Creating namespace URI from x-definition name. xDefName='{}', nsUri='{}'",
                    logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER), xDefName, nsUri);
        }
    }

    /**
     * Creates and initialize adapter for transformation of single x-definition
     * @return Transformation adapter
     */
    private XDef2XsdAdapter createXDefAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setAdapterCtx(adapterCtx);
        return adapter;
    }

}
