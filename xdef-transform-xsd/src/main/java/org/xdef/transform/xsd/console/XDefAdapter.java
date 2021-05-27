package org.xdef.transform.xsd.console;

import org.xdef.XDPool;
import org.xdef.transform.xsd.console.impl.XDefTransformResult;

/**
 * @author smid
 * @since 2021-05-13
 */
public interface XDefAdapter {

    /**
     * Transforms X-definition(s) to XML Schema
     * @return result of transformation
     */
    XDefTransformResult transform();

    /**
     * Transforms X-definition pool to XML Schema
     * @param xdPool    input X-Definition pool
     * @return result of transformation
     */
    XDefTransformResult transform(XDPool xdPool);

}
