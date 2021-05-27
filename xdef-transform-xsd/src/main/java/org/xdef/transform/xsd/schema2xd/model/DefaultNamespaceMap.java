package org.xdef.transform.xsd.schema2xd.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.model.DefaultNamespace;
import org.xdef.transform.xsd.model.Namespace;
import org.xdef.transform.xsd.msg.XSD;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdLoggerDefs.XD_ADAPTER_CTX;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;

/**
 * Key:     namespace URI
 * Value:   namespace prefix
 *
 * @author smid
 * @since 2021-05-20
 */
public class DefaultNamespaceMap extends HashMap<String, String> implements NamespaceMap {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNamespaceMap.class);

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;

    public DefaultNamespaceMap(ReportWriter reportWriter) {
        this.reportWriter = reportWriter;
    }

    @Override
    public boolean add(String nsPrefix, String nsUri, final String xDefName) {
        nsPrefix = nsPrefix.trim();
        nsUri = nsUri.trim();

        if (containsKey(nsUri)) {
            reportWriter.warning(XSD.XSD218, xDefName, nsPrefix);
            LOG.warn("{}X-definition namespace already exists. xDefinitionName='{}', nsPrefix='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, nsPrefix);
            return false;
        }

        LOG.info("{}Add x-definition namespace. xDefinitionName='{}', nsPrefix='{}', nsUri='{}'",
                logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, nsPrefix, nsUri);

        put(nsUri, nsPrefix);
        return true;
    }

    @Override
    public Optional<String> findByUri(final String nsUri) {
        LOG.trace("Finding namespace prefix by given URI. nsUri='{}'", nsUri);
        return Optional.ofNullable(get(nsUri));
    }

    @Override
    public List<Namespace> getNamespaces() {
        return entrySet().stream()
                .map(entry -> new DefaultNamespace(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }
}
