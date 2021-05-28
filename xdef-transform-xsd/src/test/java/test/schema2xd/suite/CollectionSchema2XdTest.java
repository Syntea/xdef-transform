package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class CollectionSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "collection";

    @Test
    public void xdPool_t011() {
        initTestCaseDirs(SUITE_NAME, "t011");
        convertXsd2XdPoolNoRef(
                "t011",
                Collections.singletonList("t011"),
                null);
    }

    @Test
    public void xdPool_t012() {
        initTestCaseDirs(SUITE_NAME, "t012");
        convertXsd2XdPoolNoRef(
                "t012",
                Collections.singletonList("t012"),
                null);
    }

    @Test
    public void xdPool_t013() {
        initTestCaseDirs(SUITE_NAME, "t013");
        convertXsd2XdPoolNoRef(
                "t013",
                Collections.singletonList("t013"),
                null);
    }

    @Test
    public void xdPool_t014() {
        initTestCaseDirs(SUITE_NAME, "t014");
        convertXsd2XdPoolNoRef(
                "t014",
                Collections.singletonList("t014"),
                null);
    }

    @Test
    public void xdPool_t015() {
        initTestCaseDirs(SUITE_NAME, "t015");
        convertXsd2XdPoolNoRef(
                "t015",
                Collections.singletonList("t015"),
                null);
    }

    @Test
    public void xdPool_t018() {
        initTestCaseDirs(SUITE_NAME, "t018");
        convertXsd2XdPoolNoRef(
                "t018",
                Collections.singletonList("t018"),
                null);
    }

    @Test
    public void xdPool_Namespace_1() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest2");
        convertXsd2XdPoolNoRef(
                "namespaceTest2",
                Collections.singletonList("namespaceTest2_valid_1"),
                null);
    }

    @Test
    public void xdPool_Namespace_2() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest3");
        convertXsd2XdPoolNoRef(
                "namespaceTest3",
                Collections.singletonList("namespaceTest3_valid_1"),
                null);
    }

    @Test
    public void xdPool_Namespace_3() {
        initTestCaseDirs(SUITE_NAME, "namespaceTest4");
        convertXsd2XdPoolNoRef(
                "namespaceTest4",
                Collections.singletonList("namespaceTest4_valid_1"),
                null);
    }

    @Test
    public void xdPool_MultipleXDef_1() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest");
        convertXsd2XdPoolNoRef(
                "multiXdefTest",
                Collections.singletonList("multiXdefTest_valid_1"),
                null);
    }

    @Test
    public void xdPool_MultipleXDef_2() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest2");
        convertXsd2XdPoolNoRef(
                "multiXdefTest2",
                Collections.singletonList("multiXdefTest2_valid_1"),
                null);
    }

    @Test
    public void xdPool_MultipleXDef_3() {
        initTestCaseDirs(SUITE_NAME, "multiXdefTest3");
        convertXsd2XdPoolNoRef(
                "multiXdefTest3",
                Collections.singletonList("multiXdefTest3_valid_1"),
                null);
    }

    @Test
    public void xdPool_Reference_1() {
        initTestCaseDirs(SUITE_NAME, "refTest1");
        convertXsd2XdPoolNoRef(
                "refTest1",
                Collections.singletonList("refTest1_valid_1"),
                null);
    }

    @Test
    public void xdPool_Reference_2() {
        initTestCaseDirs(SUITE_NAME, "refTest2");
        convertXsd2XdPoolNoRef(
                "refTest2",
                Collections.singletonList("refTest2_valid_1"),
                null);
    }

    @Test
    public void xdPool_Reference_3() {
        initTestCaseDirs(SUITE_NAME, "refTest3");
        convertXsd2XdPoolNoRef(
                "refTest3",
                Collections.singletonList("refTest3_valid_1"),
                null);
    }

    @Test
    public void xdPool_Sisma() {
        initTestCaseDirs(SUITE_NAME, "sisma");
        convertXsd2XdPoolNoRef(
                "sisma",
                Collections.singletonList("sisma"),
                null);
    }

    @Test
    public void xdPool_Sisma_RegistraceSU() {
        initTestCaseDirs(SUITE_NAME, "Sisma_RegistraceSU");
        convertXsd2XdPoolNoRef(
                "Sisma_RegistraceSU",
                Collections.singletonList("Sisma_RegistraceSU"),
                null);
    }

}
