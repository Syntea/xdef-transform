package org.xdef.transform.xsd.xd2schema.model.xsd.impl;

import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

public class XmlSchemaSequenceWrapper extends AbstractXmlSchemaGroupParticleWrapper<XmlSchemaSequence, XmlSchemaSequenceMember> {

    public XmlSchemaSequenceWrapper(XmlSchemaSequence xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public final List<XmlSchemaSequenceMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaSequenceMember) item);
    }

    @Override
    public void addItems(List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase member : items) {
            xsdNode.getItems().add((XmlSchemaSequenceMember) member);
        }
    }
}
