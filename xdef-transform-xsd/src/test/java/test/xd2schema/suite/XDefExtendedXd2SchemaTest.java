package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XDefExtendedXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "xdef-extended";

    @Test
    public void extended_SoapRequestD6WS() {
        initTestCaseDirs(SUITE_NAME, "SoapRequestD6WS");
        transformXd2SchemaNoRef("SoapRequestD6WS", null, null);
    }

    @Test
    public void extended_ChildAttr2Child() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_CHLD");
        transformXd2SchemaNoRef("ATTR_CHLD_to_CHLD", Collections.singletonList("ATTR_CHLD_to_CHLD_valid_1"), null);
    }

    @Test
    public void extended_ChildAttr2Attr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_ATTR");
        transformXd2SchemaNoRef("ATTR_CHLD_to_ATTR", Collections.singletonList("ATTR_CHLD_to_ATTR_valid_1"), null);
    }

    @Test
    public void extended_ChildAttr2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_ATTR_CHLD");
        transformXd2SchemaNoRef("ATTR_CHLD_to_ATTR_CHLD", Collections.singletonList("ATTR_CHLD_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void extended_Attr2Attr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_ATTR");
        transformXd2SchemaNoRef(
                "ATTR_to_ATTR",
                Arrays.asList("ATTR_to_ATTR_valid_1", "ATTR_to_ATTR_valid_2"),
                Arrays.asList("ATTR_to_ATTR_invalid_1", "ATTR_to_ATTR_invalid_2"));
    }

    @Test
    public void extended_Attr2Child() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_CHLD");
        transformXd2SchemaNoRef(
                "ATTR_to_CHLD", Collections.singletonList("ATTR_to_CHLD_valid_1"), null);
    }

    @Test
    public void extended_Attr2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_ATTR_CHLD");
        transformXd2SchemaNoRef(
                "ATTR_to_ATTR_CHLD", Collections.singletonList("ATTR_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void extended_Child2Attr() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_ATTR");
        transformXd2SchemaNoRef(
                "CHLD_to_ATTR", Collections.singletonList("CHLD_to_ATTR_valid_1"), null);
    }

    @Test
    public void extended_Child2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_ATTR_CHLD");
        transformXd2SchemaNoRef(
                "CHLD_to_ATTR_CHLD", Collections.singletonList("CHLD_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void extended_Child2Child() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_CHLD");
        transformXd2SchemaNoRef(
                "CHLD_to_CHLD", Collections.singletonList("CHLD_to_CHLD_valid_1"), null);
    }

    @Test
    public void extended_BasicTest() {
        initTestCaseDirs(SUITE_NAME, "basicTest");
        transformXd2SchemaNoRef(
                "basicTest",
                Arrays.asList("basicTest_valid_1", "basicTest_valid_2", "basicTest_valid_3"),
                Arrays.asList("basicTest_invalid_1", "basicTest_invalid_2", "basicTest_invalid_3", "basicTest_invalid_4"));
    }

    @Test
    public void extended_B1_common() {
        initTestCaseDirs(SUITE_NAME, "B1_common");
        transformXd2SchemaNoRef(
                "B1_common",
                Arrays.asList("B1_Common_valid_1", "B1_Common_valid_2"),
                null);
    }

    @Test
    public void extended_DateTime() {
        initTestCaseDirs(SUITE_NAME, "dateTimeTest");
        transformXd2SchemaNoRef(
                "dateTimeTest", Collections.singletonList("dateTimeTest_valid_1"), null);
    }

    @Test
    public void extended_Declaration() {
        initTestCaseDirs(SUITE_NAME, "declarationTest");
        transformXd2SchemaNoRef(
                "declarationTest",
                Arrays.asList("declarationTest_valid_1", "declarationTest_valid_2", "declarationTest_valid_3"),
                Arrays.asList("declarationTest_invalid_1", "declarationTest_invalid_2", "declarationTest_invalid_3", "declarationTest_invalid_4"));
    }

    @Test
    public void extended_Namespace1() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest");
        transformXd2SchemaNoRef(
                "namespaceTest", Collections.singletonList("namespaceTest_valid"), null);
    }

    @Test
    public void extended_Namespace2() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest2");
        transformXd2SchemaNoRef(
                "namespaceTest2", Collections.singletonList("namespaceTest2_valid_1"), null);
    }

    @Test
    public void extended_Namespace3() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest3");
        transformXd2SchemaNoRef(
                "namespaceTest3", Collections.singletonList("namespaceTest3_valid_1"), null);
    }

    @Test
    public void extended_Namespace4() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest4");
        transformXd2SchemaNoRef(
                "namespaceTest4", Collections.singletonList("namespaceTest4_valid_1"), null);
    }

    @Test
    public void extended_SimpleModelTest() {
        initTestCaseDirs(SUITE_NAME, "simpleModelTest");
        transformXd2SchemaNoRef(
                "simpleModelTest",
                Arrays.asList("simpleModelTest_valid_1", "simpleModelTest_valid_2"/*, "simpleModelTest_valid_3"*/, "simpleModelTest_valid_5", "simpleModelTest_valid_5"),
                null);
    }

    @Test
    public void extended_t990() {
        initTestCaseDirs(SUITE_NAME, "t990");
        transformXd2SchemaNoRef(
                "t990",
                Collections.singletonList("t990_1"),
                Arrays.asList("t990_1e", "t990_2e", "t990_3e", "t990_4e", "t990_5e"));
    }

    @Test
    public void extended_D1A() {
        initTestCaseDirs(SUITE_NAME, "D1A");
        transformXd2SchemaNoRef(
                "D1A", Collections.singletonList("D1A"), null);
    }

    @Test
    public void extended_D2A() {
        initTestCaseDirs(SUITE_NAME, "D2A");
        transformXd2SchemaNoRef(
                "D2A", Collections.singletonList("D2A"), null);
    }

    @Test
    public void extended_D3A() {
        initTestCaseDirs(SUITE_NAME, "D3A");
        transformXd2SchemaNoRef(
                "D3A", Collections.singletonList("D3A"), null);
    }

}
