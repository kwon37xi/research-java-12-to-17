package kr.pe.kwonnam.research.java.switches;

/**
 * {@code switch} 에서 instance type 체크하기.
 *
 * 아래는 preview 기능으로 컴파일과 실행시 java 에 {@code --enable-preview} 옵션이 필요하다.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
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
