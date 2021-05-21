package org.xdef.transform.xsd.xd2schema.model.uc;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaAttribute;

import java.util.List;

/**
 * List of pairs [variable name / xml schema attribute name, xml schema attribute]
 *
 * @author smid
 * @since 2021-05-21
 */
public interface UniqueConstraintAttributeList {

    /**
     * Returns all values stored in list
     * @return
     */
    List<Pair<String, XmlSchemaAttribute>> values();

    // ====================
    // Basic List interface
    // ====================

    int size();
}
