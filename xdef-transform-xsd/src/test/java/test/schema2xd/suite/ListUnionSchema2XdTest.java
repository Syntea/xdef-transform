package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class ListUnionSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "list-union";

    @Test
    public void basic_SchemaType_1() {
        initTestCaseDirs(SUITE_NAME, "schemaTypeTest");
        convertXsd2XDefNoRef(
                "schemaTypeTest",
                Collections.singletonList("schemaTypeTest_valid_1"),
                Collections.singletonList("schemaTypeTest_invalid_1"));
    }

    @Test
    public void basic_SchemaType_2() {
        initTestCaseDirs(SUITE_NAME, "schemaTypeTest2");
        convertXsd2XDefNoRef("schemaTypeTest2", Collections.singletonList("schemaTypeTest2_valid_1"), null);
    }

    @Test
    public void basic_SchemaType_3() {
        initTestCaseDirs(SUITE_NAME, "schemaTypeTest3");
        convertXsd2XDefNoRef("schemaTypeTest3", Collections.singletonList("schemaTypeTest3_valid_1"), null);
    }

}
