package org.xdef.transform.xsd.xd2schema.model.xsd;

import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

/**
 * XML schema group particle wrapper
 *
 * @param <T>
 */
public interface XmlSchemaGroupParticleWrapper<T extends XmlSchemaObjectBase> {

    /**
     * Returns particle XML schema items
     * @return
     */
    List<T> getItems();

    /**
     * Add XML schema item into particle
     * @param item
     */
    void addItem(final XmlSchemaObjectBase item);

    /**
     * Add XML schema items into particle
     * @param items
     */
    void addItems(final List<XmlSchemaObjectBase> items);

}
