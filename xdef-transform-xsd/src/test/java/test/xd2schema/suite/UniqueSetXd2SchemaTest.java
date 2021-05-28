package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class UniqueSetXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "unique_set";

    // ==============================
    // ID, IDREF, IDREFS, CHKID, CHKIDS with uniqueSet declaration in root
    // ==============================

    @Test
    public void keyAndRef_1() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1");
        transformXd2SchemaNoRef("keyAndRef1", Collections.singletonList("keyAndRef1_valid_1"), null);
    }

    @Test
    public void keyAndRef_1B() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1B");
        transformXd2SchemaNoRef(
                "keyAndRef1B",
                Collections.singletonList("keyAndRef1B_valid_1"),
                /*Collections.singletonList("keyAndRef1B_invalid_1")*/null);
    }

    @Test
    public void keyAndRef_1C() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1C");
        transformXd2SchemaNoRef(
                "keyAndRef1C",
                Collections.singletonList("keyAndRef1C_valid_1"),
                Collections.singletonList("keyAndRef1C_invalid_1"));
    }

    @Test
    public void keyAndRef_1G() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1G");
        transformXd2SchemaNoRef(
                "keyAndRef1G",
                Collections.singletonList("keyAndRef1G_valid_1"),
                /*Collections.singletonList("keyAndRef1G_invalid_1")*/null);
    }

    @Test
    public void keyAndRef_1H() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1H");
        transformXd2SchemaNoRef(
                "keyAndRef1H",
                Collections.singletonList("keyAndRef1H_valid_1"),
                Collections.singletonList("keyAndRef1H_invalid_1"));
    }

    @Test
    public void keyAndRef_3() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef3");
        transformXd2SchemaNoRef("keyAndRef3", Collections.singletonList("keyAndRef3_valid_1"), null);
    }

    @Test
    public void keyAndRef_7() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef7");
        transformXd2SchemaNoRef(
                "keyAndRef7",
                Collections.singletonList("keyAndRef7_valid_1"),
                Collections.singletonList("keyAndRef7_invalid_2"));
    }

    // ==============================
    // ID, IDREF, IDREFS, CHKID, CHKIDS with uniqueSet declaration in root
    // ==============================

    @Test
    public void keyAndRef_4() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef4");
        transformXd2SchemaNoRef("keyAndRef4", Collections.singletonList("keyAndRef4_valid_1"), null);
    }

    @Test
    public void keyAndRef_4B() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef4B");
        transformXd2SchemaNoRef("keyAndRef4B", Collections.singletonList("keyAndRef4B_valid_1"), null);
    }

    // ==============================
    // ID, IDREF, IDREFS in different path with uniqueSet declaration in root
    // ==============================

    @Test
    public void keyAndRef_1D() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1D");
        transformXd2SchemaNoRef("keyAndRef1D", Collections.singletonList("keyAndRef1D_valid_1"), null);
    }

    @Test
    public void keyAndRef_1e() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1E");
        transformXd2SchemaNoRef(
                "keyAndRef1E",
                Arrays.asList("keyAndRef1E_valid_1", "keyAndRef1E_valid_2"),
                Collections.singletonList("keyAndRef1E_invalid_1"));
    }

    @Test
    public void keyAndRef_1F() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef1F");
        transformXd2SchemaNoRef("keyAndRef1F", Collections.singletonList("keyAndRef1F_valid_1"), null);
    }

    // ==============================
    // ID, IDREF, IDREFS with uniqueSet declaration in element
    // ==============================

    @Test
    public void keyAndRef_2() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef2");
        transformXd2SchemaNoRef(
                "keyAndRef2",
                Arrays.asList("keyAndRef2_valid_1", "keyAndRef2_valid_2"),
                null);
    }

    @Test
    public void keyAndRef_2B() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef2B");
        transformXd2SchemaNoRef("keyAndRef2B", Collections.singletonList("keyAndRef2B_valid_1"), null);
    }

    @Test
    public void keyAndRef_2C() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef2C");
        transformXd2SchemaNoRef(
                "keyAndRef2C",
                Collections.singletonList("keyAndRef2C_valid_1"),
                Collections.singletonList("keyAndRef2C_invalid_1"));
    }

    @Test
    public void keyAndRef_2D() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef2D");
        transformXd2SchemaNoRef(
                "keyAndRef2D",
                Collections.singletonList("keyAndRef2D_valid_1"),
                /*Collections.singletonList("keyAndRef2D_invalid_1")*/null);
    }

    // Multiple variables inside uniqueSet
    @Test
    public void keyAndRef_5() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef5");
        transformXd2SchemaNoRef("keyAndRef5", Collections.singletonList("keyAndRef5_valid_1"), null);
    }

    // Multiple variables inside uniqueSet
    @Test
    public void keyAndRef_6() {
        initTestCaseDirs(SUITE_NAME, "keyAndRef6");
        transformXd2SchemaNoRef("keyAndRef6", Collections.singletonList("keyAndRef6_valid_1"), null);
    }

}
