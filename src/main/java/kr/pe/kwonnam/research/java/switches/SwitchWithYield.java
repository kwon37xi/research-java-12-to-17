package kr.pe.kwonnam.research.java.switches;

/**
 * {@code switch}와 {@code yield}.
 *
 * {@yield} 는 case block 에서 리턴할때 사용한다.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class SwitchWithYield {
    public static void main(String[] args) {
        onShowWithYield(Country.BHUTAN);
        onShowWithYield(Country.GERMANY);
    }

    public static void onShowWithYield(Country country) {
        String output = switch (country) {
            case NETHERLANDS, POLAND, GERMANY -> {
                System.out.println("European Country : " + country);
                yield "Country belongs to Europe Continent";
            }
            case INDIA, BHUTAN, NEPAL -> {
                System.out.println("Asian Country : " + country);
                yield "Country belongs to Asian Continent";
            }
            default -> "It's from Wakanda";
        };

        System.out.println("결과 : " + output);
    }
}
