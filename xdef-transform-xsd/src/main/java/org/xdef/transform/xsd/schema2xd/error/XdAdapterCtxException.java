package org.xdef.transform.xsd.schema2xd.error;

import org.xdef.transform.xsd.error.FormattedRuntimeException;

/**
 * @author smid
 * @since 2021-05-20
 */
public class XdAdapterCtxException extends FormattedRuntimeException {

    public XdAdapterCtxException(String format, Object... args) {
        super(format, args);
    }

}
