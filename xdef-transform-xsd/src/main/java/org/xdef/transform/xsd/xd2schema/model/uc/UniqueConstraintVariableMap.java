package org.xdef.transform.xsd.xd2schema.model.uc;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Key:     variable name
 * Value:   variable node path map
 *
 * @author smid
 * @since 2021-05-21
 */
public interface UniqueConstraintVariableMap<T extends UniqueConstraintNodePathMap> {

    /**
     * Finds variable path map based on variable name
     * @param varName       variable name
     * @return  variable path map
     *          otherwise {@link Optional#empty()}
     */
    Optional<UniqueConstraintNodePathMap> findNodePathMap(String varName);

    boolean isEmpty();

    Set<Map.Entry<String, T>> entrySet();

}
