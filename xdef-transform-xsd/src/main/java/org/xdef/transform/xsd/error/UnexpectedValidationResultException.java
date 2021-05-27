package org.xdef.transform.xsd.error;

/**
 * @author smid
 * @since 2021-05-20
 */
public class UnexpectedValidationResultException extends FormattedRuntimeException {

    public UnexpectedValidationResultException(String format, Object... args) {
        super(format, args);
    }

    public UnexpectedValidationResultException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
