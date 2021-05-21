package org.xdef.transform.xsd;

import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDConstants;

/**
 * Namespace constants.
 *
 * @author smid
 * @since 2021-05-21
 */
public interface NamespaceConst {

    String NAMESPACE_PREFIX_EMPTY = "";
    String NAMESPACE_URI_EMPTY = "";
    String NAMESPACE_DELIMITER = ":";

    String XML_SCHEMA_DEFAULT_NAMESPACE_PREFIX = "xs";
    String XML_SCHEMA_DEFAULT_NAMESPACE_URI = Constants.URI_2001_SCHEMA_XSD;

    String XDEF_DEFAULT_NAMESPACE_PREFIX = XDConstants.XDEF_NS_PREFIX;
    String XDEF_DEFAULT_NAMESPACE_URI = XDConstants.XDEF40_NS_URI;

}
