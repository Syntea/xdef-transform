package org.xdef.transform.xsd.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.factory.declaration.impl.DefaultTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.transform.xsd.schema2xd.model.impl.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;


/**
 * Creates X-Definition declarations
 */
public class XdDeclarationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XdDeclarationFactory.class);

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML node factory
     */
    final private XdNodeFactory xdFactory;

    final private XdAdapterCtx adapterCtx;

    /**
     * Set of names of already processed top level declarations
     */
    final Set<String> processedTopDeclarations = new HashSet<>();

    public XdDeclarationFactory(XmlSchema schema, XdNodeFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates X-Definition declaration based on given builder.
     * Append created declaration to builder X-Definition parent node
     * @param builder   X-Definition declaration builder
     */
    public void createDeclaration(final XdDeclarationBuilder builder) {
        LOG.info("{}Creating declaration ...", logHeader(TRANSFORMATION, builder.simpleType));
        if (builder.parentNode == null) {
            adapterCtx.getReportWriter().warning(XSD.XSD214);
            LOG.warn("{}Parent node is not set. Created declaration is going to be lost!",
                    logHeader(TRANSFORMATION, builder.simpleType));
            return;
        }

        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        xdDeclaration.setTextContent(builder.build());
        builder.parentNode.appendChild(xdDeclaration);
    }

    /**
     * Creates X-Definition declaration content based on given builder.
     * @param builder   X-Definition declaration builder
     * @return X-Definition declaration content
     */
    public String createDeclarationContent(final XdDeclarationBuilder builder) {
        LOG.info("{}Creating declaration content ...", logHeader(TRANSFORMATION, builder.simpleType));
        return builder.build();
    }

    /**
     * Creates X-Definition declaration content without any restrictions
     * @param baseType      declaration qualified name
     * @return X-Definition declaration content
     */
    public String createSimpleTextDeclaration(final QName baseType) {
        final DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory(baseType.getLocalPart());
        defaultTypeFactory.setType(IDeclarationTypeFactory.Type.TEXT_DECL);
        return defaultTypeFactory.build("", adapterCtx.getReportWriter());
    }

    /**
     * Creates default initialized X-Definition declaration builder
     * @return
     */
    public XdDeclarationBuilder createBuilder() {
        return new XdDeclarationBuilder().init(schema, this, adapterCtx.getReportWriter());
    }

    /**
     * Check if top level X-Definition declaration can be created
     * @param name  declaration name
     * @return true, if X-Definition declaration with given name has not been created yet
     */
    boolean canBeProcessed(final String name) {
        return processedTopDeclarations.add(name);
    }
}
