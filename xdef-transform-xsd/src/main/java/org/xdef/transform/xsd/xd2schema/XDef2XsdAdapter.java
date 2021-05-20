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
import org.xdef.transform.xsd.util.SchemaLogger;
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

import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_INFO;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.INITIALIZATION;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.util.Xd2XsdLoggerDefs.XSD_XDEF_ADAPTER;

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
        
        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "====================");
        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "Transforming x-definition. Name=" + xDef.getName());
        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "====================");

        boolean poolPostProcessing = true;

        this.xDefinition = (XDefinition)xDef;
        if (adapterCtx == null) {
            adapterCtx = new XsdAdapterCtx(features, reportWriter);
            adapterCtx.init();
            schema = createXsdSchema();
            poolPostProcessing = false;
        } else {
            schema = adapterCtx.findSchema(XsdNameUtils.getSchemaName(xDef), false, INITIALIZATION);
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
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xDefinition, "*** Transformation of x-definition tree ***");

        final Set<String> rootNodeNames = adapterCtx.findSchemaRootNodeNames(xDefinition.getName());

        if (rootNodeNames != null) {
            for (XMElement elem : xDefinition.getModels()) {
                if (rootNodeNames.contains(elem.getName())) {
                    treeAdapter.convertTree(elem);
                    SchemaLogger.printP(LOG_INFO, TRANSFORMATION, elem, "Adding root element to schema. Element=" + elem.getName());
                }
            }
        }
    }

    /**
     * Creates and initialize XSD document
     */
    private XmlSchema createXsdSchema() {
        Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace(xDefinition, adapterCtx);

        SchemaLogger.printP(LOG_INFO, INITIALIZATION, xDefinition, "Creating XSD document. " +
                "systemName=" + xDefinition.getName() + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri=" + targetNamespace.getValue());

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        return schemaFactory.createXsdSchema(xDefinition, targetNamespace);
    }

}
