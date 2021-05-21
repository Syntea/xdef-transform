package org.xdef.transform.xsd.xd2schema.error;

import org.xdef.transform.xsd.error.FormattedRuntimeException;

/**
 * @author smid
 * @since 2021-05-21
 */
public class XsdNamespaceException extends FormattedRuntimeException {

    public XsdNamespaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public XsdNamespaceException(String format, Object... args) {
        super(format, args);
    }

    public XsdNamespaceException(Throwable cause) {
        super(cause);
    }

    public XsdNamespaceException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
