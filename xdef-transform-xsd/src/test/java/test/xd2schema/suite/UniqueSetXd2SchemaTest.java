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
        initCaseDirs(SUITE_NAME, "keyAndRef1");
        convertXd2SchemaNoRef("keyAndRef1", Collections.singletonList("keyAndRef1_valid_1"), null);
    }

    @Test
    public void keyAndRef_1B() {
        initCaseDirs(SUITE_NAME, "keyAndRef1B");
        convertXd2SchemaNoRef(
                "keyAndRef1B",
                Collections.singletonList("keyAndRef1B_valid_1"),
                /*Collections.singletonList("keyAndRef1B_invalid_1")*/null);
    }

    @Test
    public void keyAndRef_1C() {
        initCaseDirs(SUITE_NAME, "keyAndRef1C");
        convertXd2SchemaNoRef(
                "keyAndRef1C",
                Collections.singletonList("keyAndRef1C_valid_1"),
                Collections.singletonList("keyAndRef1C_invalid_1"));
    }

    @Test
    public void keyAndRef_1G() {
        initCaseDirs(SUITE_NAME, "keyAndRef1G");
        convertXd2SchemaNoRef(
                "keyAndRef1G",
                Collections.singletonList("keyAndRef1G_valid_1"),
                /*Collections.singletonList("keyAndRef1G_invalid_1")*/null);
    }

    @Test
    public void keyAndRef_1H() {
        initCaseDirs(SUITE_NAME, "keyAndRef1H");
        convertXd2SchemaNoRef(
                "keyAndRef1H",
                Collections.singletonList("keyAndRef1H_valid_1"),
                Collections.singletonList("keyAndRef1H_invalid_1"));
    }

    @Test
    public void keyAndRef_3() {
        initCaseDirs(SUITE_NAME, "keyAndRef3");
        convertXd2SchemaNoRef("keyAndRef3", Collections.singletonList("keyAndRef3_valid_1"), null);
    }

    @Test
    public void keyAndRef_7() {
        initCaseDirs(SUITE_NAME, "keyAndRef7");
        convertXd2SchemaNoRef(
                "keyAndRef7",
                Collections.singletonList("keyAndRef7_valid_1"),
                Collections.singletonList("keyAndRef7_invalid_2"));
    }

    // ==============================
    // ID, IDREF, IDREFS, CHKID, CHKIDS with uniqueSet declaration in root
    // ==============================

    @Test
    public void keyAndRef_4() {
        initCaseDirs(SUITE_NAME, "keyAndRef4");
        convertXd2SchemaNoRef("keyAndRef4", Collections.singletonList("keyAndRef4_valid_1"), null);
    }

    @Test
    public void keyAndRef_4B() {
        initCaseDirs(SUITE_NAME, "keyAndRef4B");
        convertXd2SchemaNoRef("keyAndRef4B", Collections.singletonList("keyAndRef4B_valid_1"), null);
    }

    // ==============================
    // ID, IDREF, IDREFS in different path with uniqueSet declaration in root
    // ==============================

    @Test
    public void keyAndRef_1D() {
        initCaseDirs(SUITE_NAME, "keyAndRef1D");
        convertXd2SchemaNoRef("keyAndRef1D", Collections.singletonList("keyAndRef1D_valid_1"), null);
    }

    @Test
    public void keyAndRef_1e() {
        initCaseDirs(SUITE_NAME, "keyAndRef1E");
        convertXd2SchemaNoRef(
                "keyAndRef1E",
                Arrays.asList("keyAndRef1E_valid_1", "keyAndRef1E_valid_2"),
                Collections.singletonList("keyAndRef1E_invalid_1"));
    }

    @Test
    public void keyAndRef_1F() {
        initCaseDirs(SUITE_NAME, "keyAndRef1F");
        convertXd2SchemaNoRef("keyAndRef1F", Collections.singletonList("keyAndRef1F_valid_1"), null);
    }

    // ==============================
    // ID, IDREF, IDREFS with uniqueSet declaration in element
    // ==============================

    @Test
    public void keyAndRef_2() {
        initCaseDirs(SUITE_NAME, "keyAndRef2");
        convertXd2SchemaNoRef(
                "keyAndRef2",
                Arrays.asList("keyAndRef2_valid_1", "keyAndRef2_valid_2"),
                null);
    }

    @Test
    public void keyAndRef_2B() {
        initCaseDirs(SUITE_NAME, "keyAndRef2B");
        convertXd2SchemaNoRef("keyAndRef2B", Collections.singletonList("keyAndRef2B_valid_1"), null);
    }

    @Test
    public void keyAndRef_2C() {
        initCaseDirs(SUITE_NAME, "keyAndRef2C");
        convertXd2SchemaNoRef(
                "keyAndRef2C",
                Collections.singletonList("keyAndRef2C_valid_1"),
                Collections.singletonList("keyAndRef2C_invalid_1"));
    }

    @Test
    public void keyAndRef_2D() {
        initCaseDirs(SUITE_NAME, "keyAndRef2D");
        convertXd2SchemaNoRef(
                "keyAndRef2D",
                Collections.singletonList("keyAndRef2D_valid_1"),
                /*Collections.singletonList("keyAndRef2D_invalid_1")*/null);
    }

    // Multiple variables inside uniqueSet
    @Test
    public void keyAndRef_5() {
        initCaseDirs(SUITE_NAME, "keyAndRef5");
        convertXd2SchemaNoRef("keyAndRef5", Collections.singletonList("keyAndRef5_valid_1"), null);
    }

    // Multiple variables inside uniqueSet
    @Test
    public void keyAndRef_6() {
        initCaseDirs(SUITE_NAME, "keyAndRef6");
        convertXd2SchemaNoRef("keyAndRef6", Collections.singletonList("keyAndRef6_valid_1"), null);
    }

}
