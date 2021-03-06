package org.xdef.transform.xsd.console.impl;

import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class XDefAdapterConfig {
    private String inputDirectory;
    private String outputDirectory;

    private String outputFileExt = ".xsd";
    private String outputFilePrefix = "";

    private Optional<String> inputRoot;
    private List<String> testingDataPos;
    private List<String> testingDataNeg;

    private boolean useDefaultFeatures = true;
    private EnumSet<Xd2XsdFeature> features = EnumSet.noneOf(Xd2XsdFeature.class);

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public Optional<String> getInputRoot() {
        return inputRoot;
    }

    public void setInputRoot(String inputRoot) {
        this.inputRoot = Optional.ofNullable(inputRoot);
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputFileExt() {
        return outputFileExt;
    }

    public void setOutputFileExt(String outputFileExt) {
        this.outputFileExt = outputFileExt;
    }

    public String getOutputFilePrefix() {
        return outputFilePrefix;
    }

    public void setOutputFilePrefix(String outputFilePrefix) {
        this.outputFilePrefix = outputFilePrefix;
    }

    public List<String> getTestingDataPos() {
        return testingDataPos;
    }

    public void setTestingDataPos(List<String> testingDataPos) {
        this.testingDataPos = testingDataPos;
    }

    public List<String> getTestingDataNeg() {
        return testingDataNeg;
    }

    public void setTestingDataNeg(List<String> testingDataNeg) {
        this.testingDataNeg = testingDataNeg;
    }

    public boolean useDefaultFeatures() {
        return useDefaultFeatures;
    }

    public void setUseDefaultFeatures(boolean useDefaultFeatures) {
        this.useDefaultFeatures = useDefaultFeatures;
    }

    public EnumSet<Xd2XsdFeature> getFeatures() {
        return features;
    }

    public void setFeatures(EnumSet<Xd2XsdFeature> features) {
        this.features = features;
    }

    public boolean hasTestingData() {
        return hasPositiveTestingData() || hasNegativeTestingData();
    }

    public boolean hasPositiveTestingData() {
        return getTestingDataPos() != null && !getTestingDataPos().isEmpty();
    }

    public boolean hasNegativeTestingData() {
        return getTestingDataNeg() != null && !getTestingDataNeg().isEmpty();
    }

    @Override
    public String toString() {
        return "XdefAdapterConfig{" +
                "inputFileName='" + inputDirectory + '\'' +
                ", inputRootModel='" + inputRoot + '\'' +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", outputFileExt='" + outputFileExt + '\'' +
                ", outputFilePrefix='" + outputFilePrefix + '\'' +
                ", testingDataPos=" + testingDataPos +
                ", testingDataNeg=" + testingDataNeg +
                ", useDefaultFeatures=" + useDefaultFeatures +
                ", features=" + features +
                '}';
    }
}
