package org.xdef.transform.xsd.xd2schema.model.uc;

import java.util.Map;
import java.util.Set;

/**
 * Key:     variable node path
 * Value:   list of variable attributes
 *
 * @author smid
 * @since 2021-05-21
 */
public interface UniqueConstraintNodePathMap<T extends UniqueConstraintAttributeList> {

    // ====================
    // Basic Map interface
    // ====================

    boolean isEmpty();

    int size();

    Set<Map.Entry<String, T>> entrySet();

}
