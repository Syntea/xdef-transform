package org.xdef.transform.xsd.xd2schema;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.adapter.AbstractXd2XsdAdapter;
import org.xdef.transform.xsd.xd2schema.adapter.Xd2XsdPostProcessingAdapter;
import org.xdef.transform.xsd.xd2schema.adapter.Xd2XsdReferenceAdapter;
import org.xdef.transform.xsd.xd2schema.adapter.Xd2XsdTreeAdapter;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdSchemaFactory;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_XDEF_ADAPTER;

/**
 * Transformation of given x-definition or x-definition pool to collection of XSD documents
 */
public class XDef2XsdAdapter extends AbstractXd2XsdAdapter implements XDef2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input x-definition used for transformation
     */
    private XDefinition xDefinition = null;

    /**
     * Output XSD document
     */
    private XmlSchema schema = null;

    @Override
    public XmlSchemaCollection createSchema(final XDPool xdPool) {
        if (xdPool == null) {
            throw new SRuntimeException(XSD.XSD047);
        }
        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchemaCollection createSchema(final XMDefinition xDef) {
        if (xDef == null) {
            throw new SRuntimeException(XDEF.XDEF705);
        }

        LOG.info(HEADER_LINE);
        LOG.info("{}Transforming x-definition. xdefName='{}'", logHeader(XSD_XDEF_ADAPTER), xDef.getName());
        LOG.info(HEADER_LINE);

        boolean poolPostProcessing = true;

        this.xDefinition = (XDefinition)xDef;
        if (adapterCtx == null) {
            adapterCtx = new XsdAdapterCtx(features, reportWriter);
            adapterCtx.init();
            schema = createXsdSchema();
            poolPostProcessing = false;
        } else {
            schema = adapterCtx.findSchemaReq(XsdNameUtils.getSchemaName(xDef), INITIALIZATION);
        }

        final XsdNodeFactory xsdFactory = new XsdNodeFactory(schema, adapterCtx);
        final Xd2XsdTreeAdapter treeAdapter = new Xd2XsdTreeAdapter(schema, xDef.getName(), xsdFactory, adapterCtx);
        final Xd2XsdReferenceAdapter referenceAdapter = new Xd2XsdReferenceAdapter(schema, xDef.getName(), xsdFactory, treeAdapter, adapterCtx);

        treeAdapter.loadXdefRootNames(xDefinition);
        treeAdapter.loadXdefRootUniqueSets(xDefinition);
        referenceAdapter.createRefsAndImports(xDefinition);
        transformXdef(treeAdapter);

        if (!poolPostProcessing) {
            final Xd2XsdPostProcessingAdapter postProcessingAdapter = new Xd2XsdPostProcessingAdapter();
            postProcessingAdapter.setAdapterCtx(adapterCtx);
            postProcessingAdapter.process(xDefinition);
            postProcessingAdapter.setReportWriter(reportWriter);
        }

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Transform x-definition tree to XSD document via treeAdapter
     * @param treeAdapter   transformation algorithm
     */
    private void transformXdef(final Xd2XsdTreeAdapter treeAdapter) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Transformation of x-definition tree", logHeader(TRANSFORMATION, xDefinition));
        LOG.info(HEADER_LINE);

        final Set<String> rootNodeNames = adapterCtx.findSchemaRootNodeNames(xDefinition.getName());

        for (XMElement elem : xDefinition.getModels()) {
            if (rootNodeNames.contains(elem.getName())) {
                treeAdapter.convertTree(elem);
                LOG.info("{}Adding root element to schema. elementName='{}'",
                        logHeader(TRANSFORMATION, elem), elem.getName());
            }
        }
    }

    /**
     * Creates and initialize XSD document
     */
    private XmlSchema createXsdSchema() {
        Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace(xDefinition, adapterCtx);

        LOG.info("{}Creating XSD document. systemName='{}', targetNsPrefix='{}', targetNsUri='{}'",
                logHeader(INITIALIZATION, xDefinition),
                xDefinition.getName(),
                targetNamespace.getKey(),
                targetNamespace.getValue());

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        return schemaFactory.createXsdSchema(xDefinition, targetNamespace);
    }

}
