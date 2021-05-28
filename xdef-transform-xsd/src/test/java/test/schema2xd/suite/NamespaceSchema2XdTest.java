package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class NamespaceSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "namespace";

    @Test
    public void namespace_1() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest");
        convertXsd2XDefNoRef(
                "namespaceTest",
                Collections.singletonList("namespaceTest_valid"),
                null);
    }

}
