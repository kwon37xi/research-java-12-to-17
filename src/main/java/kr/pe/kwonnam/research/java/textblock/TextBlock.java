package kr.pe.kwonnam.research.java.textblock;

/**
 * 텍스트 블럭.
 *
 * 들여쓰기를 변수와 맞추는 텍스트 블럭 앞의 공백은 없는 것으로 친다.
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class TextBlock {
    public static void main(String[] args) {
        String multiLineText = """
        There are good ships and wood ships,
        ships that sail the sea,
        but the best ships are friendships,
        may they always be!
        """;

        System.out.println("The multi line text example " + multiLineText);
    }

}
