package kr.pe.kwonnam.research.java.sealedclass;

/**
 * Sealed Class.
 * <br/>
 * 자식 클래스가 inner class 일 경우에는 permit 이 불필요하다.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public sealed class SampleSealedClassInlineApp {
    final class SampleClass extends SampleSealedClassInlineApp {}
    final class SampleSecondClass extends SampleSealedClassInlineApp {}
}