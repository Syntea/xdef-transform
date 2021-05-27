package org.xdef.transform.xsd.schema2xd.def;

import org.xdef.transform.xsd.util.LoggingGroup;

/**
 * Logging definitions for XML Schema -> X-Definition transformation algorithm
 */
public enum Xsd2XdLoggerDefs implements LoggingGroup {
    XD_ADAPTER("XdAdapter"),
    XD_ADAPTER_CTX("XdAdapterCtx");

    private final String name;

    Xsd2XdLoggerDefs(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
