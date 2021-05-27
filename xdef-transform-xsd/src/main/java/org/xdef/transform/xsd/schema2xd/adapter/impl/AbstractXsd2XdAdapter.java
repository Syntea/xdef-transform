package org.xdef.transform.xsd.schema2xd.adapter.impl;

import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.schema2xd.def.Xsd2XdFeature;
import org.xdef.transform.xsd.schema2xd.model.impl.XdAdapterCtx;

import java.util.HashSet;
import java.util.Set;


/**
 * Base class for all adapters transforming XML Schema document to X-Definition
 */
public class AbstractXsd2XdAdapter {

    /**
     * Adapter context
     */
    protected XdAdapterCtx adapterCtx = null;

    /**
     * Output report writer
     */
    protected ReportWriter reportWriter = null;

    /**
     * Enabled algorithm features
     */
    protected Set<Xsd2XdFeature> features = new HashSet<>();

    /**
     * Set output writer
     * @param reportWriter  output report writer
     */
    public void setReportWriter(ReportWriter reportWriter) {
        this.reportWriter = reportWriter;
    }

    /**
     * Set features which should be enabled by transformation algorithm
     * @param features      features to be enabled
     */
    public void setFeatures(Set<Xsd2XdFeature> features) {
        this.features = features;
    }

    /**
     * Add feature which should be enabled by transformation algorithm
     * @param feature       feature to be enabled
     */
    public void addFeature(Xsd2XdFeature feature) {
        features.add(feature);
    }

}
