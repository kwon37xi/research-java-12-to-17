package kr.pe.kwonnam.research.java.switches;

public class SwitchWithTypeCoverage {
    public static void main(String[] args) {
        System.out.println("ClassTwo : " + withSealedClass(new ClassTwo()));
    }

    public static String withSealedClass(SampleSealed sampleSealedClassApp) {
        return switch (sampleSealedClassApp) {
            case ClassOne classOne -> "Sample class one";
            case ClassTwo classTwo -> "Sample class two";
            case ClassThree classThree -> "Sample class three";
        };
    }

}
