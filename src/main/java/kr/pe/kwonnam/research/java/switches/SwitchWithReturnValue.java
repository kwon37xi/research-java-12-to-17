package kr.pe.kwonnam.research.java.switches;

/**
 * {@switch}에서 {@code ->}로 즉시 값 반환.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class SwitchWithReturnValue {
    public static void main(String[] args) {
        System.out.println("Bhutan : " + onShowWithReturnValue(Country.BHUTAN));
        System.out.println("Poland : " + onShowWithReturnValue(Country.POLAND));
        System.out.println("Korea : " + onShowWithReturnValue(Country.KOREA));
    }

    public static String onShowWithReturnValue(Country country) {
        String output = switch (country) {
            case NETHERLANDS, POLAND, GERMANY -> "European Country";
            case INDIA, BHUTAN, NEPAL -> "Asian Country";
            default -> "It's from Wakanda";
        };
        return output;
    }

}
