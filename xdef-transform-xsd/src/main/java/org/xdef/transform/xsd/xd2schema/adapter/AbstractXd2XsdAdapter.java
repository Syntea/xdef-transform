package org.xdef.transform.xsd.xd2schema.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all adapters transforming x-definition to XSD document
 */
public abstract class AbstractXd2XsdAdapter {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * Adapter context
     */
    protected XsdAdapterCtx adapterCtx = null;

    /**
     * Output report writer
     */
    protected ReportWriter reportWriter = null;

    /**
     * Enabled algorithm features
     */
    protected Set<Xd2XsdFeature> features = new HashSet<>();

    /**
     * External setting of adapter context
     * @param adapterCtx    adapter context
     */
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Get names of all created schemas
     * @return return set of schemas names
     */
    public final Set<String> getSchemaNames() {
        return adapterCtx.getSchemaNames();
    }

    /**
     * Set output writer
     * @param reportWriter      output report writer
     */
    public void setReportWriter(ReportWriter reportWriter) {
        this.reportWriter = reportWriter;
    }

    /**
     * Set features which should be enabled by transformation algorithm
     * @param features          features to be enabled
     */
    public void setFeatures(Set<Xd2XsdFeature> features) {
        this.features = features;
    }

    /**
     * Add feature which should be enabled by transformation algorithm
     * @param feature           feature to be enabled
     */
    public void addFeature(Xd2XsdFeature feature) {
        features.add(feature);
    }

}
