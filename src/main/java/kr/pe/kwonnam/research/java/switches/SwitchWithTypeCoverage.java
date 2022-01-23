package kr.pe.kwonnam.research.java.switches;

/**
 * {@code switch} 에서 instance type 체크하기.<br/>
 * 이 경우, sealed 클래스의 자식 클래스에 대해 모든 case 를 작성하지 않거나 default 를 작성하지 않으면 컴파일
 * 오류가 발생한다.
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
