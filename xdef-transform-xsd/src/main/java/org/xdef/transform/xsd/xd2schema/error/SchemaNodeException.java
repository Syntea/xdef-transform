package org.xdef.transform.xsd.xd2schema.error;

import org.xdef.transform.xsd.error.FormattedRuntimeException;

/**
 * @author smid
 * @since 2021-05-21
 */
public class SchemaNodeException extends FormattedRuntimeException {

    public SchemaNodeException(String format, Object... args) {
        super(format, args);
    }

}
