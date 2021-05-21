package org.xdef.transform.xsd.xd2schema.model.uc;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaAttribute;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author smid
 * @since 2021-05-21
 */
public class DefaultUniqueConstraintAttributeList extends LinkedList<Pair<String, XmlSchemaAttribute>> implements UniqueConstraintAttributeList {

    @Override
    public List<Pair<String, XmlSchemaAttribute>> values() {
        return this.stream().collect(Collectors.toList());
    }

}
