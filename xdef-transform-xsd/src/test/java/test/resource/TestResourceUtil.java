package test.resource;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Resource handling stored in src/test/resources.
 *
 * @author smid
 * @since 2021-05-20
 */
public class TestResourceUtil {

    public static final String TEST_RESOURCE_DIR = "src/test/resources";

    public static ResourceUtil create() {
        return new ResourceUtil(TEST_RESOURCE_DIR);
    }

    public static String getResourceAbsolutePath(String resourcePath) {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getResourceAbsolutePath(resourcePath);
    }

    public static Optional<String> getOptResourceAbsolutePath(String resourcePath) {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getOptResourceAbsolutePath(resourcePath);
    }

    public static String getReqResourceAbsolutePath(String resourcePath) throws ResourceUtilException {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getReqResourceAbsolutePath(resourcePath);
    }

    public static String getFileResourceAbsolutePath(String resourcePath) throws ResourceUtilException {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getFileResourceAbsolutePath(resourcePath);
    }

    public static String getDirResourceAbsolutePath(String resourcePath)  throws ResourceUtilException{
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getDirResourceAbsolutePath(resourcePath);
    }

    public static Path getResourcePath(String resourcePath) {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getResourcePath(resourcePath);
    }

    public static Optional<Path> getOptResourcePath(String resourcePath) {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getOptResourcePath(resourcePath);
    }

    public static Path getReqResourcePath(String resourcePath) throws ResourceUtilException {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getReqResourcePath(resourcePath);
    }

    public static Path getFileResourcePath(String resourcePath) throws ResourceUtilException {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getFileResourcePath(resourcePath);
    }

    public static Path getDirResourcePath(String resourcePath) throws ResourceUtilException {
        final ResourceUtil resourceUtil = create();
        return resourceUtil.getDirResourcePath(resourcePath);
    }

}
