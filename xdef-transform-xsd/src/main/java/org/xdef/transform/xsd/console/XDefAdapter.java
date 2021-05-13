package org.xdef.transform.xsd.console;

import org.xdef.XDPool;
import org.xdef.transform.xsd.console.impl.XDefTransformResult;

/**
 * @author smid
 * @since 2021-05-13
 */
public interface XDefAdapter {

    /**
     * Transforms X-definition(s) to XML schema
     * @return
     */
    XDefTransformResult transform();

    /**
     *
     * @param xdPool
     * @return
     */
    XDefTransformResult transform(XDPool xdPool);

}
