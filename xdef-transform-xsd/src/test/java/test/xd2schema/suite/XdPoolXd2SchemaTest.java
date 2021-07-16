package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XdPoolXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "xd-pool";

    @Test
    public void xdPool_t011() {
        initTestCaseDirs(SUITE_NAME, "t011");
        transformXd2Schema("t011", Collections.singletonList("t011"), null);
    }

    @Test
    public void xdPool_t012() {
        initTestCaseDirs(SUITE_NAME, "t012");
        transformXd2Schema("t012", Arrays.asList("t012", "t012_1", "t012_2"), null);
    }

    @Test
    public void xdPool_t013() {
        initTestCaseDirs(SUITE_NAME, "t013");
        transformXd2Schema("t013", Collections.singletonList("t013"), null);
    }

    @Test
    public void xdPool_t014() {
        initTestCaseDirs(SUITE_NAME, "t014");
        transformXd2Schema("t014", Collections.singletonList("t014"), null);
    }

    @Test
    public void xdPool_t015() {
        initTestCaseDirs(SUITE_NAME, "t015");
        transformXd2Schema("t015", Arrays.asList("t015", "t015_1"), null);
    }

    @Test
    public void xdPool_t018() {
        initTestCaseDirs(SUITE_NAME, "t018");
        transformXd2Schema("t018", Collections.singletonList("t018"), null);
    }

    @Test
    public void xdPool_GlobalAndLocal() {
        initTestCaseDirs(SUITE_NAME, "globalAndLocalTest");
        transformXd2SchemaNoRef(
                "globalAndLocalTest",
                Arrays.asList("globalAndLocalTest_X", "globalAndLocalTest_Y", "globalAndLocalTest_Z"),
                Arrays.asList("globalAndLocalTest_X_invalid", "globalAndLocalTest_Y_invalid", "globalAndLocalTest_Z_invalid"));
    }

    @Test
    public void xdPool_MultiXDef1() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest");
        transformXd2SchemaNoRef("multiXdefTest", Collections.singletonList("multiXdefTest_valid_1"), null);
    }

    @Test
    public void xdPool_MultiXDef2() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest2");
        transformXd2SchemaNoRef("multiXdefTest2", Collections.singletonList("multiXdefTest2_valid_1"), null);
    }

    @Test
    public void xdPool_MultiXDef3() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest3");
        transformXd2SchemaNoRef("multiXdefTest3", Collections.singletonList("multiXdefTest3_valid_1"), null);
    }

    @Test
    public void xdPool_SimpleRef() {
        initTestCaseDirs(SUITE_NAME, "simpleRefTest");
        transformXd2SchemaNoRef("simpleRefTest", Collections.singletonList("simpleRefTest_valid_1"), null);
    }

    @Test
    public void xdPool_SimpleRefb() {
        initTestCaseDirs(SUITE_NAME, "simpleRefTestb");
        transformXd2Schema(
                "simpleRefTest",
                Collections.singletonList("simpleRefTest_valid_1"),
                null,
                false,
                null,
                false,
                EnumSet.of(Xd2XsdFeature.XSD_ELEMENT_NO_SIMPLE_TYPE));
    }

    @Test
    public void xdPool_Ref1() {
        initTestCaseDirs(SUITE_NAME, "refTest1");
        transformXd2SchemaNoRef("refTest1", Collections.singletonList("refTest1_valid_1"), null);
    }

    @Test
    public void xdPool_Ref2() {
        initTestCaseDirs(SUITE_NAME, "refTest2");
        transformXd2SchemaNoRef("refTest2", Collections.singletonList("refTest2_valid_1"), null);
    }

    @Test
    public void xdPool_Ref3() {
        initTestCaseDirs(SUITE_NAME, "refTest3");
        transformXd2SchemaNoRef("refTest3", Collections.singletonList("refTest3_valid_1"), null);
    }

    @Test
    public void xdPool_Sisma() {
        initTestCaseDirs(SUITE_NAME, "sisma");
        transformXd2SchemaNoRef("sisma", Collections.singletonList("sisma"), null);
    }

    @Test
    public void xdPool_XDefType1() {
        initTestCaseDirs(SUITE_NAME, "typeTest");
        transformXd2SchemaNoRef("typeTest", Collections.singletonList("typeTest_valid_1"), null);
    }

    @Test
    public void xdPool_XDefType2() {
        initTestCaseDirs(SUITE_NAME, "typeTest2");
        transformXd2SchemaWithFeatures("typeTest2", Collections.singletonList("typeTest2_valid_1"), null, EnumSet.of(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR));
    }

    @Test
    public void xdPool_Test000_05() {
        initTestCaseDirs(SUITE_NAME, "Test000_05");
        transformXd2SchemaNoRef("Test000_05", Collections.singletonList("Test000_05"), null);
    }

}
