package org.xdef.transform.xsd.xd2schema.model.impl;

import org.xdef.sys.SRuntimeException;
import org.xdef.transform.xsd.msg.XSD;

import java.util.Objects;

/**
 * Definition of XML Schema document import.
 *
 * Source data model for creating XML Schema xs:import node.
 */
public class XsdSchemaImportLocation {

    /**
     * XML Schema document namespace URI
     */
    private final String namespaceUri;

    /**
     * XML Schema document file name
     */
    private final String fileName;

    /**
     * XML Schema document file extension
     */
    private final String fileExt = ".xsd";

    /**
     * XML Schema document path
     */
    private String path;

    public XsdSchemaImportLocation(String namespaceUri, String fileName) {
        this.namespaceUri = namespaceUri;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Creates XML Schema import path based on internal variable state
     * @param schemaName    XML Schema document name which will be used if fileName is not set
     * @return XML Schema document import path
     */
    public String buildLocation(final String schemaName) {
        final StringBuilder sb = new StringBuilder();
        if (path != null && !path.trim().isEmpty()) {
            sb.append(path).append('/');
        }

        if (fileName != null) {
            sb.append(fileName);
        } else if (schemaName != null) {
            sb.append(schemaName);
        } else {
            throw new SRuntimeException(XSD.XSD008, schemaName, namespaceUri);
        }

        sb.append(fileExt);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XsdSchemaImportLocation that = (XsdSchemaImportLocation) o;
        return Objects.equals(namespaceUri, that.namespaceUri)
                && Objects.equals(path, that.path)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(fileExt, that.fileExt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespaceUri, path, fileName, fileExt);
    }

    @Override
    public String toString() {
        return "XsdSchemaImportLocation{" +
                "namespaceUri='" + namespaceUri + '\'' +
                ", path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileExt='" + fileExt + '\'' +
                '}';
    }

}
