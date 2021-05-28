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
        initCaseDirs(SUITE_NAME, "t011");
        convertXd2Schema("t011", Collections.singletonList("t011"), null);
    }

    @Test
    public void xdPool_t012() {
        initCaseDirs(SUITE_NAME, "t012");
        convertXd2Schema("t012", Arrays.asList("t012", "t012_1", "t012_2"), null);
    }

    @Test
    public void xdPool_t013() {
        initCaseDirs(SUITE_NAME, "t013");
        convertXd2Schema("t013", Collections.singletonList("t013"), null);
    }

    @Test
    public void xdPool_t014() {
        initCaseDirs(SUITE_NAME, "t014");
        convertXd2Schema("t014", Collections.singletonList("t014"), null);
    }

    @Test
    public void xdPool_t015() {
        initCaseDirs(SUITE_NAME, "t015");
        convertXd2Schema("t015", Arrays.asList("t015", "t015_1"), null);
    }

    @Test
    public void xdPool_t018() {
        initCaseDirs(SUITE_NAME, "t018");
        convertXd2Schema("t018", Collections.singletonList("t018"), null);
    }

    @Test
    public void xdPool_GlobalAndLocal() {
        initCaseDirs(SUITE_NAME, "globalAndLocalTest");
        convertXd2SchemaNoRef(
                "globalAndLocalTest",
                Arrays.asList("globalAndLocalTest_X", "globalAndLocalTest_Y", "globalAndLocalTest_Z"),
                Arrays.asList("globalAndLocalTest_X_invalid", "globalAndLocalTest_Y_invalid", "globalAndLocalTest_Z_invalid"));
    }

    @Test
    public void xdPool_MultiXdef1() {
        initCaseDirs(SUITE_NAME, "multiXdefTest");
        convertXd2SchemaNoRef("multiXdefTest", Collections.singletonList("multiXdefTest_valid_1"), null);
    }

    @Test
    public void xdPool_MultiXdef2() {
        initCaseDirs(SUITE_NAME, "multiXdefTest2");
        convertXd2SchemaNoRef("multiXdefTest2", Collections.singletonList("multiXdefTest2_valid_1"), null);
    }

    @Test
    public void xdPool_MultiXdef3() {
        initCaseDirs(SUITE_NAME, "multiXdefTest3");
        convertXd2SchemaNoRef("multiXdefTest3", Collections.singletonList("multiXdefTest3_valid_1"), null);
    }

    @Test
    public void xdPool_SimpleRef() {
        initCaseDirs(SUITE_NAME, "simpleRefTest");
        convertXd2SchemaNoRef("simpleRefTest", Collections.singletonList("simpleRefTest_valid_1"), null);
    }

    @Test
    public void xdPool_Ref1() {
        initCaseDirs(SUITE_NAME, "refTest1");
        convertXd2SchemaNoRef("refTest1", Collections.singletonList("refTest1_valid_1"), null);
    }

    @Test
    public void xdPool_Ref2() {
        initCaseDirs(SUITE_NAME, "refTest2");
        convertXd2SchemaNoRef("refTest2", Collections.singletonList("refTest2_valid_1"), null);
    }

    @Test
    public void xdPool_Ref3() {
        initCaseDirs(SUITE_NAME, "refTest3");
        convertXd2SchemaNoRef("refTest3", Collections.singletonList("refTest3_valid_1"), null);
    }

    @Test
    public void xdPool_Sisma() {
        initCaseDirs(SUITE_NAME, "sisma");
        convertXd2SchemaNoRef("sisma", Collections.singletonList("sisma"), null);
    }

    @Test
    public void xdPool_XDefType1() {
        initCaseDirs(SUITE_NAME, "typeTest");
        convertXd2SchemaNoRef("typeTest", Collections.singletonList("typeTest_valid_1"), null);
    }

    @Test
    public void xdPool_XDefType2() {
        initCaseDirs(SUITE_NAME, "typeTest2");
        convertXd2SchemaWithFeatures("typeTest2", Collections.singletonList("typeTest2_valid_1"), null, EnumSet.of(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR));
    }

    @Test
    public void xdPool_Test000_05() {
        initCaseDirs(SUITE_NAME, "Test000_05");
        convertXd2SchemaNoRef("Test000_05", Collections.singletonList("Test000_05"), null);
    }

}
