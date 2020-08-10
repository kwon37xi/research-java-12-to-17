package kr.pe.kwonnam.research.java.completablefuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * allOf 의 결과로 원하는 Type의 {@link CompletableFuture} 를 반환하게 하는 기법
 *
 * @see <a href="https://www.nurkiewicz.com/2013/05/java-8-completablefuture-in-action.html">Java 8: CompletableFuture in action</a>
 */
public class CompletableFutureTypedAllOfTest {
    @Test
    @DisplayName("typedAllOf : 실행 순서에 상관없이, 여러 CF를 동시에 실행하고 원하는 결과 타입의 CF를 리턴한다.")
    void typedAllOf() throws ExecutionException, InterruptedException {
        long startTimestamp = System.currentTimeMillis();
        List<CompletableFuture<String>> delayedCompletableFutures = List.of(
            delayedString(10, "하나"),
            delayedString(100, "둘"),
            delayedString(1, "셋"),
            delayedString(70, "넷"),
            delayedString(90, "다섯")
        );

        CompletableFuture<Void> allCompletableFuture = CompletableFuture.allOf(delayedCompletableFutures.toArray(new CompletableFuture[delayedCompletableFutures.size()]));
        // allOf 의 thenApply 는 모든 CompletableFuture가 종료되면 그 뒤에 호출된다.

        CompletableFuture<List<String>> joinedCompletableFuture = allCompletableFuture.thenApply(ignoredVoid -> {
            // ignoredVoid 는 사용하지 않는다.
            return delayedCompletableFutures.stream()
                .map(CompletableFuture::join) // 이미 종료된 CompletableFuture 이기 때문에 join 을 해도 blocking 이 발생하지 않는다.
                .collect(toList());
        });

        List<String> strings = joinedCompletableFuture.get();
        long endTimestamp = System.currentTimeMillis();

        assertThat(strings).as("모든 문자열이 빠르게 실행 순서 상관없이 CompletableFuture 생성순서에 따라 반환된다.")
            .isEqualTo(List.of("하나", "둘", "셋", "넷", "다섯"));

        assertThat(Duration.ofMillis(endTimestamp - startTimestamp))
            .as("동시에 실행되므로 가장 오래 걸리는 100ms 보다 약간 오래 걸린 수준에서 끝난다.")
            .isBetween(Duration.ofMillis(100), Duration.ofMillis(110));
    }

    CompletableFuture<String> delayedString(long millis, String result) {
        return new CompletableFuture<String>().completeOnTimeout(result, millis, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("allOfSequentialResults : 처리 결과를 순서대로 정렬해서 노출한다. 외부에 concurrent 처리가 가능한 컬렉션을 사용한다.")
    void allOfSequentialResults() throws ExecutionException, InterruptedException {
        long startTimestamp = System.currentTimeMillis();

        List<CompletableFuture<String>> delayedCompletableFutures = List.of(
            delayedString(10, "이등"),
            delayedString(100, "오등"),
            delayedString(1, "일등"),
            delayedString(70, "삼등"),
            delayedString(90, "사등")
        );

        // 동시성 문제를 피하기 위해 Concurrent* 를 사용함.
        ConcurrentLinkedQueue<String> resultQueue = new ConcurrentLinkedQueue<>();

        // 결과가 나오는대로 바로 외부 큐에 순서대로 저장한다.
        List<CompletableFuture<Boolean>> resultAddCompletableFutures = delayedCompletableFutures.stream()
            .map(stringCompletableFuture -> stringCompletableFuture.thenApply(s -> resultQueue.add(s)))
            .collect(toList());

        CompletableFuture<Void> allCompletableFuture = CompletableFuture
            .allOf(resultAddCompletableFutures.toArray(new CompletableFuture[resultAddCompletableFutures.size()]));
        // allOf 의 thenApply 는 모든 CompletableFuture가 종료되면 그 뒤에 호출된다.

        CompletableFuture<List<String>> joinedCompletableFuture = allCompletableFuture
            .thenApply(ignoredVoid -> new ArrayList<>(resultQueue));

        List<String> result = joinedCompletableFuture.get();
        long endTimestamp = System.currentTimeMillis();

        assertThat(result).as("모든 문자열이 빠르게 실행된 순서대로 조합된 상태로 반환된다.")
            .isEqualTo(List.of("일등", "이등", "삼등", "사등", "오등"));

        assertThat(Duration.ofMillis(endTimestamp - startTimestamp))
            .as("동시에 실행되므로 가장 오래 걸리는 100ms 보다 약간 오래 걸린 수준에서 끝난다.")
            .isBetween(Duration.ofMillis(100), Duration.ofMillis(110));
    }

    @Test
    @DisplayName("thenCombineWithReduceSequentialResults : 처리 결과를 순서대로 정렬해서 노출한다. Stream.reduce에 thenCombine을 사용한다.")
    void thenCombineWithReduceSequentialResults() throws ExecutionException, InterruptedException {
        long startTimestamp = System.currentTimeMillis();

        List<CompletableFuture<String>> delayedCompletableFutures = List.of(
            delayedString(10, "하나"),
            delayedString(100, "둘"),
            delayedString(1, "셋"),
            delayedString(70, "넷"),
            delayedString(90, "다섯")
        );

        CompletableFuture<List<String>> resultCompletableFuture = delayedCompletableFutures.stream()
            .reduce(CompletableFuture.completedFuture(List.of()), (CompletableFuture<List<String>> listCompletableFuture1, CompletableFuture<String> stringCompletableFuture) -> {
                CompletableFuture<List<String>> vCompletableFuture = listCompletableFuture1.thenCombine(stringCompletableFuture, (List<String> strings1, String s) -> {
                    List<String> newList1 = new ArrayList<>(strings1);
                    newList1.add(s);
                    return newList1;
                });
                return vCompletableFuture;
            }, (CompletableFuture<List<String>> listCompletableFuture, CompletableFuture<List<String>> listCompletableFuture2) -> listCompletableFuture.thenCombine(listCompletableFuture2, (strings, strings2) -> {
                List<String> newList = new ArrayList<>();
                newList.addAll(strings);
                newList.addAll(strings2);
                return newList;
            }));


        List<String> result = resultCompletableFuture.get();
        long endTimestamp = System.currentTimeMillis();

        assertThat(result).as("모든 문자열이 빠르게 실행 순서 상관없이 CompletableFuture 생성순서에 따라 반환된다.")
            .isEqualTo(List.of("하나", "둘", "셋", "넷", "다섯"));

        assertThat(Duration.ofMillis(endTimestamp - startTimestamp))
            .as("동시에 실행되므로 가장 오래 걸리는 100ms 보다 약간 오래 걸린 수준에서 끝난다.")
            .isBetween(Duration.ofMillis(100), Duration.ofMillis(110));
    }
}
