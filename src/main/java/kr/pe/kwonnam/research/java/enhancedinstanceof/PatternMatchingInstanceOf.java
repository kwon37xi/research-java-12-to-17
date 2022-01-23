package kr.pe.kwonnam.research.java.enhancedinstanceof;


/**
 * {@code instanceOf}에 합치할 경우, 자동으로 해당 타입으로 변환된 객체를 사용할 수 있게 됨.<br/>
 *
 * 이 경우 {@code countryData} 변수는 블럭 안에서만 사용가능하다.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class PatternMatchingInstanceOf {
    public static void main(String[] args) {
        Object o = new CountryData("Netherlands", "Amsterdam", "Europe");

        if (o instanceof CountryData countryData) {
            System.out.println("This capital of Netherlands is " + countryData.capital());
        }

        if (o instanceof CountryData countryData && countryData.continent().equals("Europe")) {
            System.out.println("This continent of Netherlands is " + countryData.continent());
        }
    }

}
