package org.xdef.transform.xsd.xd2schema.model.uc;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XData;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.model.OptionalExt;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.transform.xsd.XDefConst.XDEF_REF_DELIMITER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_KEY_AND_REF;

/**
 * Model containing information gathered from X-Definition uniqueSet.
 *
 * Stores information about internal variables of uniqueSet.
 * Stores position of ID and REF attributes using uniqueSet.
 */
public class UniqueConstraint {

    private static final Logger LOG = LoggerFactory.getLogger(UniqueConstraint.class);

    public enum Type {
        UNK,
        ID,
        IDREF,
        IDREFS,
        CHKID,
        CHKIDS,
    }

    /**
     * UniqueSet name
     */
    private final String name;

    /**
     * XML Schema document name, where uniqueSet should be placed
     */
    private final String systemId;

    /**
     * Storage of variables inside uniqueSet
     * key:     variable name
     * value:   variable type
     */
    private final Map<String, QName> variables = new HashMap<>();

    /**
     * Storage of attribute's path ID using uniqueSet.
     */
    private final DefaultUniqueConstraintVariableMap variableKeyMap = new DefaultUniqueConstraintVariableMap();

    /**
     * Storage of attribute's path IDREF, CHKID using uniqueSet.
     */
    private final DefaultUniqueConstraintVariableMap variableRefMap = new DefaultUniqueConstraintVariableMap();

    public UniqueConstraint(String name, String systemId) {
        this.name = name;
        this.systemId = systemId;
    }

    /**
     * @return unique set name
     */
    public String getName() {
        return name;
    }

    /**
     * @return XML Schema document name
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return variable key map
     */
    public UniqueConstraintVariableMap getVariableKeyMap() {
        return variableKeyMap;
    }

    /**
     * @return variable reference map
     */
    public UniqueConstraintVariableMap getVariableRefMap() {
        return variableRefMap;
    }

    /**
     * Build unique constraint path
     * @return unique constraint path
     */
    public String getPath() {
        if (systemId != null && !systemId.isEmpty()) {
            return systemId + XDEF_REF_DELIMITER + name;
        }

        return name;
    }

    /**
     * Add variable of unique constraint
     * @param xData                 X-Definition node of unique constraint's variable
     * @param adapterCtx            XML Schema adapter context
     */
    public void addVariable(final XData xData, final XsdAdapterCtx adapterCtx) {
        final String parserName = xData.getParserName();
        final OptionalExt<QName> qNameOpt = OptionalExt.of(Xd2XsdParserMapping.findDefaultParserQName(
                parserName, adapterCtx));
        final String varName = XsdNameUtils.getUniqueSetVarName(xData.getValueTypeName());

        qNameOpt.ifPresent(qName -> addVariable(varName, qName))
                .orElse(() -> LOG.info("{}Unsupported variable of unique set. uniquePath='{}', variableName='{}'",
                        logHeader(TRANSFORMATION, XSD_KEY_AND_REF),
                        getPath(),
                        varName));
    }

    /**
     * Add variable of unique constraint
     * @param name      variable name
     * @param qName     variable type qualified name
     */
    public void addVariable(final String name, final QName qName) {
        variables.computeIfAbsent(name, key -> {
            LOG.info("{}Add variable to unique set. uniquePath='{}', variableName='{}', variableQName='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), getPath(), name, qName);

            variableKeyMap.putIfAbsent(name, new DefaultUniqueConstraintNodePathMap());
            variableRefMap.putIfAbsent(name, new DefaultUniqueConstraintNodePathMap());

            return qName;
        });
    }

    /**
     * Add variable constraint into constraint set.
     * Base type (key, ref) is determines based on {@code type}
     *
     * @param varName   variable name
     * @param xsdAttr   XML Schema attribute node
     * @param varPath   variable path
     * @param type      variable type
     */
    public void addConstraint(final String varName,
                              final XmlSchemaAttribute xsdAttr,
                              final String varPath,
                              final Type type,
                              final ReportWriter reportWriter) {
        final String xPath = Xd2XsdUtils.xPathWithoutAttr(varPath);
        if (!variables.containsKey(varName)) {
            reportWriter.warning(XSD.XSD034, getPath(), varName);
            LOG.warn("{}Unique set does not contain variable with given name. uniquePath='{}', variableName='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), getPath(), varName);
            return;
        }

        if (Type.ID.equals(type)) {
            LOG.info("{}Add key to unique set. uniquePath='{}', keyName='{}', xPath='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), getPath(), xsdAttr.getName(), xPath);

            OptionalExt.ofNullable(variableKeyMap.get(varName)).ifPresent(variableKeys -> {
                final DefaultUniqueConstraintAttributeList keyList = variableKeys.computeIfAbsent(
                        xPath,
                        key -> new DefaultUniqueConstraintAttributeList()
                );

                keyList.add(Pair.of(xsdAttr.getName(), xsdAttr));
            }).orElse(() -> LOG.warn("{}Key map does not contain required variable. variableName='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), varName));
        } else if (Type.IDREF.equals(type) || Type.CHKID.equals(type)) {
            LOG.info("{}Add ref to unique set. uniquePath='{}', keyName='{}', xPath='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), getPath(), xsdAttr.getName(), xPath);

            OptionalExt.ofNullable(variableRefMap.get(varName)).ifPresent(variableRefs -> {
                final DefaultUniqueConstraintAttributeList refList = variableRefs.computeIfAbsent(
                        xPath,
                        key -> new DefaultUniqueConstraintAttributeList()
                );

                refList.add(Pair.of(xsdAttr.getName(), xsdAttr));
            }).orElse(() -> LOG.warn("{}Reference map does not contain required variable. variableName='{}'",
                    logHeader(TRANSFORMATION, XSD_KEY_AND_REF), varName));
        }
    }

    public static boolean isStringConstraint(final Type type) {
        return Type.UNK.equals(type) || Type.IDREFS.equals(type) || Type.CHKIDS.equals(type);
    }

}
