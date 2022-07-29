package kr.pe.kwonnam.research.java.streams;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Stream#mapMulti(BiConsumer)} )}는 {@link Stream#flatMap(Function)}과 거의 유사하지만
 * {@code Stream}을 매번 생성애야하는 부담을 줄여준다.<br/>
 * 개념상, mapMulti 는 하나의 데이터를 여러개로 확장하는 느낌에 가깝고,
 * flatMap 은 내포 스트림을 펼치는 느낌에 가까워 보인다.
 */
class MapMultiTest {

    /**
     * 스트림의 한 항목을 여러 항목으로 확장할 수 있다.
     *
     * @see <a href="https://www.javacodegeeks.com/2021/09/java-16-stream-mapmulti.html">Java 16: Stream.mapMulti</a>
     */
    @Test
    @DisplayName("mapMuilti : String 들의 컬렉션에 적용하여 대문자화/소문자화 함께 수행")
    void mapMultiStrings() {
        List<String> strings = List.of("Twix", "Snickers", "Mars");
        final List<String> results = strings.stream()
            .mapMulti((String s, Consumer<String> consumer) -> {
                consumer.accept(s.toUpperCase());
                consumer.accept(s.toLowerCase());
                consumer.accept(String.valueOf(s.length()));
            })
            .collect(toList());

        assertThat(results).containsExactly("TWIX", "twix", "4", "SNICKERS", "snickers", "8", "MARS", "mars", "4");
    }

    @Test
    @DisplayName("flatmap : flatMap으로 mapMulti와 동일한 효과 내기")
    void flatMapMultiStrings() {
        List<String> strings = List.of("Twix", "Snickers", "Mars");
        final List<String> results = strings.stream()
            .flatMap(s -> Stream.of(s.toUpperCase(), s.toLowerCase(), String.valueOf(s.length())))
            .collect(toList());

        assertThat(results).containsExactly("TWIX", "twix", "4", "SNICKERS", "snickers", "8", "MARS", "mars", "4");
    }

    /**
     * {@link Stream#mapMulti(BiConsumer)}로 컬렉션의 컬렉션을 flat 하게 만들기(즉, {@link Stream#flatMap(Function)} 직접 대체
     * <a href="https://4comprehension.com/java-stream-mapmulti/">Java 16’s Stream#mapMulti() – a Better Stream#flatMap Replacement?</a>
     */
    @Test
    @DisplayName("mapMulti : mapMulti로 내포 컬렉션 펼치기")
    void mapMultiNestedCollection() {
        final List<List<Integer>> listOfIntegerList = List.of(List.of(3), List.of(5, 7), List.of());

        final List<Integer> results = listOfIntegerList.stream()
            .mapMulti((List<Integer> integers, Consumer<Integer> consumer) -> integers.forEach(consumer))
            .collect(toList());

        assertThat(results).containsExactly(3, 5, 7);
    }

    @Test
    @DisplayName("flatMap : flatMap으로 내포 컬렉션 펼치기")
    void flatMapNestedCollection() {
        final List<List<Integer>> listOfIntegerList = List.of(List.of(3), List.of(5, 7), List.of());

        final List<Integer> results = listOfIntegerList.stream()
            .flatMap(integers -> integers.stream())
            .collect(toList());

        assertThat(results).containsExactly(3, 5, 7);
    }
}
