package org.xdef.transform.xsd.xd2schema.model.uc;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author smid
 * @since 2021-05-21
 */
public class DefaultUniqueConstraintVariableMap extends HashMap<String, DefaultUniqueConstraintNodePathMap> implements UniqueConstraintVariableMap<DefaultUniqueConstraintNodePathMap> {

    @Override
    public Optional<UniqueConstraintNodePathMap> findNodePathMap(String varName) {
        return Optional.ofNullable(get(varName));
    }

}
