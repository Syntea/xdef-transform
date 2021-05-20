package org.xdef.transform.xsd.xd2schema.model.xsd;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdPostProcessor;

import java.util.List;

public class CXmlSchemaChoice extends CXmlSchemaGroupParticle<XmlSchemaChoice, XmlSchemaChoiceMember> {

    public enum TransformDirection {
        NONE,
        TOP_DOWN,
        BOTTOM_UP
    }

    /**
     * Used to be true if the instance has x-definition source mixed element
     */
    private TransformDirection transformDirection = TransformDirection.NONE;

    public CXmlSchemaChoice(XmlSchemaChoice xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public final List<XmlSchemaChoiceMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaChoiceMember) item);
    }

    @Override
    public void addItems(List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase member : items) {
            xsdNode.getItems().add((XmlSchemaChoiceMember) member);
        }
    }

    public boolean hasTransformDirection() {
        return !TransformDirection.NONE.equals(transformDirection);
    }

    public void setTransformDirection(final TransformDirection direction) {
        transformDirection = direction;
    }

    public void updateOccurence(final XsdAdapterCtx adapterCtx) {
        final Pair<Long, Long> memberOccurence = Xd2XsdUtils.calculateGroupChoiceMembersOccurrence(xsdNode, adapterCtx);
        long elementMinOccursSum = memberOccurence.getKey();
        long elementMaxOccursSum = memberOccurence.getValue();

        xsdNode.setMaxOccurs(elementMaxOccursSum);
        if (xsdNode.getMinOccurs() > 0) {
            xsdNode.setMinOccurs(elementMinOccursSum);
        }

        final XsdPostProcessor postProcessor = new XsdPostProcessor(adapterCtx);
        for (XmlSchemaObjectBase member : xsdNode.getItems()) {
            postProcessor.allMemberToChoiceMember(member);
        }
    }
}
