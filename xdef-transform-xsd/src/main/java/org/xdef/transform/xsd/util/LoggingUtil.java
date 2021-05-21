package org.xdef.transform.xsd.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Node;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.xd2schema.definition.AlgPhase;

import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMTEXT;
import static org.xdef.transform.xsd.XDefConst.XDEF_REF_DELIMITER;

/**
 * @author smid
 * @since 2021-05-20
 */
public class LoggingUtil {

    public static final String HEADER_LINE = StringUtils.repeat('=', 80);

    public static String logHeader(final AlgPhase phase, final String nodeName) {
        return logHeader(phase, nodeName, "");
    }

    public static String logHeader(final AlgPhase phase, final LoggingGroup group) {
        return logHeader(phase, group, "");
    }

    public static String logHeader(final AlgPhase phase, final XmlSchemaObjectBase node) {
        return logHeader(phase, "", nodeInfo(node));
    }

    public static String logHeader(final AlgPhase phase, final XMNode node) {
        return logHeader(phase, "", nodeInfo(node));
    }

    public static String logHeader(final AlgPhase phase, final Node node) {
        return logHeader(phase, "", nodeInfo(node));
    }

    public static String logHeader(final LoggingGroup group) {
        return logHeader(null, group, "");
    }

    public static String logHeader(final LoggingGroup group, final XMNode node) {
        return logHeader(null, group, nodeInfo(node));
    }

    public static String logHeader(final AlgPhase phase) {
        return logHeader(phase, "", "");
    }

    public static String logHeader(final AlgPhase phase, final LoggingGroup group, final XmlSchemaObjectBase node) {
        return logHeader(phase, group, nodeInfo(node));
    }

    public static String logHeader(final AlgPhase phase, final LoggingGroup group, final Node node) {
        return logHeader(phase, group, nodeInfo(node));
    }

    private static String logHeader(final AlgPhase phase, LoggingGroup group, final String nodeInfo) {
        return logHeader(phase, group.getName(), nodeInfo);
    }

    private static String logHeader(final AlgPhase phase, String groupName, final String nodeInfo) {
        final StringBuilder sb = new StringBuilder();

        if (groupName != null && !StringUtils.isBlank(groupName)) {
            sb.append("[" + groupName + "]");
        }

        if (nodeInfo != null && !StringUtils.isBlank(nodeInfo)) {
            sb.append("[" + nodeInfo + "]");
        }

        if (sb.length() > 0) {
            sb.append(" ");
        }

        if (phase != null) {
            sb.append(phase.getVal() + ": ");
        }

        return sb.toString();
    }

    private static String nodeInfo(final XMNode node) {
        String nodeInfo = null;
        if (node != null) {
            if (node.getXMDefinition() == null) {
                nodeInfo = getXNodeName(node);
            } else if (XMDEFINITION == node.getKind()) {
                nodeInfo = node.getXMDefinition().getName();
            } else {
                nodeInfo = node.getXMDefinition().getName() + " - " + getXNodeName(node);
            }
        }

        return nodeInfo;
    }

    private static String nodeInfo(final Node node) {
        String nodeInfo = null;
        if (node != null) {
            if (node.getNodeName() != null) {
                nodeInfo = node.getNodeName();
            }
        }

        return nodeInfo;
    }

    private static String nodeInfo(final XmlSchemaObjectBase node) {
        String nodeInfo = null;
        if (node != null) {
            if (node instanceof XmlSchemaNamed) {
                nodeInfo = ((XmlSchemaNamed) node).getName();
            }
        }

        return nodeInfo;
    }

    private static String getXNodeName(final XMNode node) {
        String nodeName = node.getName();
        if (node.getKind() == XMTEXT) {
            int pos = -1;
            try {
                pos = node.getXDPosition().substring(0, node.getXDPosition().length() - nodeName.length() - 1).lastIndexOf('/');
            } catch (Exception ex) {

            } finally {
                if (pos != -1) {
                    nodeName = node.getXDPosition().substring(pos + 1, node.getXDPosition().lastIndexOf('/'));
                } else {
                    pos = node.getXDPosition().lastIndexOf('/');
                    if (pos != -1) {
                        nodeName = node.getXDPosition().substring(0, pos);
                    }
                }

                pos = nodeName.indexOf(XDEF_REF_DELIMITER);
                if (pos != -1) {
                    nodeName = nodeName.substring(pos + 1);
                }
            }
        }

        return nodeName;
    }

}
