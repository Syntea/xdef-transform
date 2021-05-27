package org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;

public abstract class AbstractRegexFactory implements RegexFactory {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * XML Schema adapter context
     */
    protected XsdAdapterCtx adapterCtx;

    @Override
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }
}
