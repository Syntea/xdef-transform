package test.resource;

import org.xdef.transform.xsd.error.FormattedRuntimeException;

/**
 * Resource handling exception
 *
 * @author smid
 * @since 2021-05-20
 */
public class ResourceUtilException extends FormattedRuntimeException {

    public ResourceUtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceUtilException(String format, Object... args) {
        super(format, args);
    }

    public ResourceUtilException(Throwable cause) {
        super(cause);
    }

    public ResourceUtilException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
