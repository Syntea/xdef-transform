package org.xdef.transform.xsd.xd2schema.model.uc.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraintAttributeList;

import java.util.LinkedList;
import java.util.List;

/**
 * @author smid
 * @since 2021-05-21
 */
public class DefaultUniqueConstraintAttributeList extends LinkedList<Pair<String, XmlSchemaAttribute>> implements UniqueConstraintAttributeList {

    @Override
    public List<Pair<String, XmlSchemaAttribute>> values() {
        return this;
    }

}
