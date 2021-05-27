package org.xdef.transform.xsd.xd2schema.factory.facet.pattern.types;

import org.xdef.XDNamedValue;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;

public interface RegexFactory {

    String regex(final XDNamedValue[] params);

    void setAdapterCtx(XsdAdapterCtx adapterCtx);

}
