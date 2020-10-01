package org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types;

import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;

public abstract class AbstractRegexFactory implements RegexFactory {

    /**
     * XSD adapter context
     */
    protected XsdAdapterCtx adapterCtx;

    @Override
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }
}
