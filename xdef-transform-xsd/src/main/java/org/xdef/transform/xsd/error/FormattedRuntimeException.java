package org.xdef.transform.xsd.error;

import org.xdef.transform.xsd.util.StringFormatter;

/**
 * @author smid
 * @since 2021-05-13
 */
public class FormattedRuntimeException extends RuntimeException {

    public FormattedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormattedRuntimeException(String format, Object... args) {
        super(StringFormatter.format(format, args));
    }

    public FormattedRuntimeException(Throwable cause) {
        super(cause);
    }

    public FormattedRuntimeException(Throwable cause, String format, Object... args) {
        super(StringFormatter.format(format, args), cause);
    }

}
