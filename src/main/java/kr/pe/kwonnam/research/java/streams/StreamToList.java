package kr.pe.kwonnam.research.java.streams;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link Stream}을 {@code toList()}로 즉시 list 로 변환할 수 있다.
 *
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class StreamToList {
    public static void main(String[] args) {
        Stream<String> countryStream = Stream.of("Germany", "Georgia", "Bulgaria");

        
        List<String> countries = countryStream.toList();

        for (String country : countries) {
            System.out.println(country);
        }
    }

}
