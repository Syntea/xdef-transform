package org.xdef.transform.xsd.xd2schema.model;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.xdef.impl.XData;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.util.SchemaLogger;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_INFO;
import static org.xdef.transform.xsd.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.util.Xd2XsdLoggerDefs.XSD_KEY_AND_REF;

/**
 * Model containing information gathered from x-definition uniqueSet.
 *
 * Stores information about internal variables of uniqueSet.
 * Stores position of ID and REF attributes using uniqueSet.
 */
public class UniqueConstraint {

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
     * XSD document name, where uniqueSet should be placed
     */
    private final String systemId;

    /**
     * Storage of variables inside uniqueSet
     * key:     variable name
     * value:   variable type
     */
    private Map<String, QName> variables = new HashMap<String, QName>();

    /**
     * Storage of attribute's path IDREF, CHKID using uniqueSet
     */
    private Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> refs = new HashMap<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>>();

    /**
     * Storage of attribute's path ID using uniqueSet
     */
    private Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> keys = new HashMap<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>>();

    public UniqueConstraint(String name, String systemId) {
        this.name = name;
        this.systemId = systemId;
    }

    public String getName() {
        return name;
    }

    public String getSystemId() {
        return systemId;
    }

    /**
     * Variable name
     * Node path
     * List pair variable schema name, org.xdef.transform.xsd attr
     * @return
     */
    public Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> getRefs() {
        return refs;
    }

    public Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> getKeys() {
        return keys;
    }

    /**
     * Build unique constraint path
     * @return path
     */
    public String getPath() {
        if (systemId != null && !"".equals(systemId)) {
            return systemId + '#' + name;
        }

        return name;
    }

    /**
     * Add variable of unique constraint
     * @param xData                 x-definition node of unique constraint's variable
     * @param adapterCtx            XSD adapter context
     */
    public void addVar(final XData xData, final XsdAdapterCtx adapterCtx) {
        final String parserName = xData.getParserName();
        final QName qName = Xd2XsdParserMapping.findDefaultParserQName(parserName, adapterCtx);
        final String varName = XsdNameUtils.getUniqueSetVarName(xData.getValueTypeName());

        if (qName != null) {
            addVar(varName, qName);
        } else {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, XSD_KEY_AND_REF, "Unsupported variable of unique set. Unique=" + getPath() + ", VarName=" + varName);
        }
    }

    /**
     * Add variable of unique constraint
     * @param name      variable name
     * @param qName     variable type qualified name
     */
    public void addVar(final String name, final QName qName) {
        if (variables.containsKey(name)) {
            return;
        }

        SchemaLogger.print(LOG_INFO, TRANSFORMATION, XSD_KEY_AND_REF, "Add variable to unique set. Unique=" + getPath() + ", VarName=" + name + ", QName=" + qName);
        variables.put(name, qName);
        if (!keys.containsKey(name)) {
            keys.put(name, new HashMap<String, List<Pair<String, XmlSchemaAttribute>>>());
        }

        if (!refs.containsKey(name)) {
            refs.put(name, new HashMap<String, List<Pair<String, XmlSchemaAttribute>>>());
        }
    }

    /**
     * Add variable constraint into constraint set.
     * Base type (key, ref) is determines based on {@paramref type}
     *
     * @param varName   variable name
     * @param xsdAttr   XSD attribute node
     * @param varPath   variable path
     * @param type      variable type
     */
    public void addConstraint(final String varName, final XmlSchemaAttribute xsdAttr, final String varPath, final Type type, final ReportWriter reportWriter) {
        final String xPath = Xd2XsdUtils.xPathWithoutAttr(varPath);
        if (!variables.containsKey(varName)) {
            reportWriter.warning(XSD.XSD034, getPath(), varName);
            SchemaLogger.print(LOG_WARN, TRANSFORMATION, XSD_KEY_AND_REF, "Unique set does not contain variable with given name. Unique=" + getPath() + ", VarName=" + varName);
            return;
        }

        if (Type.ID.equals(type)) {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, XSD_KEY_AND_REF, "Add key to unique set. Unique=" + getPath() + ", Name=" + xsdAttr.getName() + ", Path=" + xPath);
            final Map<String, List<Pair<String, XmlSchemaAttribute>>> variableKeys = keys.get(varName);
            List<Pair<String, XmlSchemaAttribute>> keyList = variableKeys.get(xPath);
            if (keyList == null) {
                keyList = new LinkedList<Pair<String, XmlSchemaAttribute>>();
                variableKeys.put(xPath, keyList);
            }

            keyList.add(Pair.of(xsdAttr.getName(), xsdAttr));
        } else if (Type.IDREF.equals(type) || Type.CHKID.equals(type)) {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, XSD_KEY_AND_REF, "Add ref to unique set. Unique=" + getPath() + ", Name=" + xsdAttr.getName() + ", Path=" + xPath);
            final Map<String, List<Pair<String, XmlSchemaAttribute>>> variableRefs = refs.get(varName);
            List<Pair<String, XmlSchemaAttribute>> refList = variableRefs.get(xPath);
            if (refList == null) {
                refList = new LinkedList<Pair<String, XmlSchemaAttribute>>();
                variableRefs.put(xPath, refList);
            }

            refList.add(Pair.of(xsdAttr.getName(), xsdAttr));
        }
    }

    public static boolean isStringConstraint(final Type type) {
        return Type.UNK.equals(type) || Type.IDREFS.equals(type) || Type.CHKIDS.equals(type);
    }
}
