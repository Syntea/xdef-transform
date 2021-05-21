package org.xdef.transform.xsd.xd2schema.model.xsd;

import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

public abstract class AbstractXmlSchemaGroupParticleWrapper<T extends org.apache.ws.commons.schema.XmlSchemaGroupParticle, M extends XmlSchemaObjectBase> extends org.apache.ws.commons.schema.XmlSchemaGroupParticle implements XmlSchemaGroupParticleWrapper<M> {

    protected final T xsdNode;

    public AbstractXmlSchemaGroupParticleWrapper(T xsdNode) {
        this.xsdNode = xsdNode;
    }

    public final T xsd() {
        return xsdNode;
    }

}
