package org.xdef.transform.xsd.xd2schema.adapter.impl;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.model.XMDefinition;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.adapter.XdPool2SchemaAdapter;
import org.xdef.transform.xsd.xd2schema.factory.XsdSchemaFactory;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdSchemaImportLocation;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_URI_EMPTY;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdLogGroup.XSD_XDPOOL_ADAPTER;

/**
 * Transformation of given X-Definition pool to collection of XML Schema documents
 */
public class XdPool2XsdAdapter extends AbstractXd2XsdAdapter implements XdPool2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input XDPool used for transformation
     */
    private XDPool xdPool = null;

    /**
     * Target namespace per X-Definition
     * Key:     X-Definition name
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
     * Gathers target namespaces for all x-definitions from source X-Definition pool
     */
    private void initTargetNamespaces() {
        LOG.info("{}Initialize target namespaces ...", logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER));

        xDefsWithoutNs = new HashSet<>();
        xDefTargetNs = new HashMap<>();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final String xDefName = xDef.getName();
            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace((XDefinition)xDef, adapterCtx);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                if (xDefTargetNs.containsKey(xDefName)) {
                    reportWriter.warning(XSD.XSD014, xDefName);
                    LOG.warn("{}Target namespace of X-Definition is already defined. xDefName='{}'",
                            logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER), xDefName);
                } else {
                    xDefTargetNs.put(xDefName, Pair.of(targetNamespace.getKey(), targetNamespace.getValue()));
                    LOG.info("{}Add target namespace to X-Definition. xDefName='{}', nsPrefix='{}', nsUri='{}'",
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
     * Initializes all XML Schema documents based on source X-Definition from X-Definition pool
     */
    private void initXsdSchemas() {
        LOG.info("{}Initialize XML Schema documents ...", logHeader(INITIALIZATION, XSD_XDPOOL_ADAPTER));

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            schemaFactory.createXsdSchema((XDefinition)xDef, xDefTargetNs.get(xDef.getName()));
        }
    }

    /**
     * Creates schema location context based on X-Definition target namespace and X-Definition name
     */
    private void initSchemaLocations() {
        LOG.info("{}Initialize schema locations ...", logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER));

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            final String xDefName = entry.getKey();
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName, xDefName));
        }

        for (String xDefName : xDefsWithoutNs) {
            final String nsUri = NAMESPACE_URI_EMPTY;
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName, xDefName));
            LOG.debug("{}Creating namespace URI from X-Definition name. xDefName='{}', nsUri='{}'",
                    logHeader(PREPROCESSING, XSD_XDPOOL_ADAPTER), xDefName, nsUri);
        }
    }

    /**
     * Creates and initialize adapter for transformation of single X-Definition
     * @return Transformation adapter
     */
    private XDef2XsdAdapter createXDefAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setFeatures(features);
        adapter.setAdapterCtx(adapterCtx);
        return adapter;
    }

}
