package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class ListUnionAdvancedXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "list-union";

    @Test
    public void listUnion_1() {
        initCaseDirs(SUITE_NAME, "schemaTypeTest");
        convertXd2SchemaNoRef(
                "schemaTypeTest",
                Collections.singletonList("schemaTypeTest_valid_1"),
                Collections.singletonList("schemaTypeTest_invalid_1"));
    }

    @Test
    public void listUnion_2() {
        initCaseDirs(SUITE_NAME, "schemaTypeTest2");
        convertXd2SchemaNoRef(
                "schemaTypeTest2",
                Collections.singletonList("schemaTypeTest2_valid_1"),
                null);
    }

    @Test
    public void listUnion_3() {
        initCaseDirs(SUITE_NAME, "schemaTypeTest3");
        convertXd2SchemaNoRef(
                "schemaTypeTest3",
                Collections.singletonList("schemaTypeTest3_valid_1"),
                null);
    }

}
