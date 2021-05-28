package test.resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Common resource handling
 *
 * @author smid
 * @since 2021-05-20
 */
public class ResourceUtil {

    private Path rootDir;

    public ResourceUtil(Path rootDir) {
        this.rootDir = rootDir;
    }

    public ResourceUtil(String rootDir) {
        this.rootDir = Paths.get(rootDir);
    }

    public Path getRootDir() {
        return rootDir;
    }

    public ResourceUtil setRootDir(Path rootDir) {
        this.rootDir = rootDir;
        return this;
    }

    public Path resolve(String path) {
        return rootDir.resolve(path);
    }

    public Path resolve(Path path) {
        return rootDir.resolve(path);
    }

    /**
     * Vraci absolutni cestu k resource
     *
     * @param resourcePath
     * @return
     */
    public String getResourceAbsolutePath(String resourcePath) {
        return rootDir.resolve(resourcePath).toAbsolutePath().toString();
    }

    /**
     * Vraci absolutni cestu k resource. V pripade, ze resource neexistuje, vraci {@link Optional#empty()}
     *
     * @param resourcePath
     * @return
     */
    public Optional<String> getOptResourceAbsolutePath(String resourcePath) {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        return Optional.ofNullable(path.toAbsolutePath().toString());
    }

    /**
     * Vraci absolutni cestu k resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public String getReqResourceAbsolutePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.exists(path)) {
            throw new ResourceUtilException("Required test resource not found. path='{}'", path.toAbsolutePath());
        }

        return path.toAbsolutePath().toString();
    }

    /**
     * Vraci absolutni cestu k file resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public String getFileResourceAbsolutePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.isRegularFile(path)) {
            throw new ResourceUtilException("Required test file resource not found. path='{}'", path.toAbsolutePath());
        }

        return path.toAbsolutePath().toString();
    }

    /**
     * Vraci absolutni cestu k directory resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public String getDirResourceAbsolutePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.isDirectory(path)) {
            throw new ResourceUtilException("Required test directory resource not found. path='{}'", path.toAbsolutePath());
        }

        return path.toAbsolutePath().toString();
    }

    /**
     * Vraci cestu k resource
     *
     * @param resourcePath
     * @return
     */
    public Path getResourcePath(String resourcePath) {
        return rootDir.resolve(resourcePath);
    }

    /**
     * Vraci cestu k resource. V pripade, ze resource neexistuje, vraci {@link Optional#empty()}
     * @param resourcePath
     * @return
     */
    public Optional<Path> getOptResourcePath(String resourcePath) {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        return Optional.ofNullable(path);
    }

    /**
     * Vraci cestu k resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public Path getReqResourcePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.exists(path)) {
            throw new ResourceUtilException("Required test resource not found. path='{}'", path.toAbsolutePath());
        }

        return path;
    }

    /**
     * Vraci cestu k file resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public Path getFileResourcePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.isRegularFile(path)) {
            throw new ResourceUtilException("Required test file resource not found. path='{}'", path.toAbsolutePath());
        }

        return path;
    }

    /**
     * Vraci cestu k directory resource
     *
     * @param resourcePath
     * @return
     * @throws ResourceUtilException pokud resource neexistuje
     */
    public Path getDirResourcePath(String resourcePath) throws ResourceUtilException {
        final Path path = rootDir.resolve(resourcePath);
        if (!Files.isDirectory(path)) {
            throw new ResourceUtilException("Required test directory resource not found. path='{}'", path.toAbsolutePath());
        }

        return path;
    }

}
