package kr.pe.kwonnam.research.java.records;

/**
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class SampleRecordClass {
    public static void main(String[] args) {
        Country firstCountry = new Country("Netherlands", "Amsterdam");
        Country secondCountry = new Country("Germany", "Berlin");

        System.out.println("First country object is " + firstCountry);
        System.out.println("Second country object is " + secondCountry);
        System.out.println("Check if both objects equal" + firstCountry.equals(secondCountry));

        Country countryClone = new Country(firstCountry.name(), firstCountry.capital());
        System.out.println("country clone object : " + countryClone);
    }

}
