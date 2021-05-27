package org.xdef.transform.xsd.console;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;

/**
 * @author smid
 * @since 2021-05-13
 */
public interface CommandLineProcessor {

    /**
     * Verifies the directory and file paths specified by the command line
     * @param cmd       parsed command line
     * @throws RuntimeException any of input path does not exist
     */
    void validatePaths(final CommandLine cmd) throws RuntimeException;

    /**
     * Parses input command line
     * @param options   Options to be found in command line arguments
     * @param args      Raw command line arguments
     * @throws RuntimeException error occurs while parsing command line arguments
     * @return  parsed command line
     */
    CommandLine parse(final Options options, String... args) throws MissingOptionException, RuntimeException;

}
