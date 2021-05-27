package org.xdef.transform.xsd.xd2schema.error;

import org.xdef.transform.xsd.error.FormattedRuntimeException;

/**
 * @author smid
 * @since 2021-05-21
 */
public class XsdNodeFactoryException extends FormattedRuntimeException {

    public XsdNodeFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public XsdNodeFactoryException(String format, Object... args) {
        super(format, args);
    }

    public XsdNodeFactoryException(Throwable cause) {
        super(cause);
    }

    public XsdNodeFactoryException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

}
