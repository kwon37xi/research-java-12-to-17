package kr.pe.kwonnam.research.java.completablefuture;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CompletableFutureCombineCollectorTest")
class CompletableFutureCombineCollectorTest {

    @Test
    @DisplayName("thenCombineWithStreamCollectorSequentialResults : 처리 결과를 순서대로 정렬해서 노출한다. thenCombine 을 Stream.collect에서 사용한다.")
    void thenCombineWithStreamCollectorSequentialResults() throws ExecutionException, InterruptedException {
        long startTimestamp = System.currentTimeMillis();

        List<CompletableFuture<String>> delayedCompletableFutures = List.of(
            delayedString(10, "하나"),
            delayedString(100, "둘"),
            delayedString(1, "셋"),
            delayedString(70, "넷"),
            delayedString(90, "다섯")
        );

        CompletableFuture<List<String>> resultCompletableFuture = delayedCompletableFutures.stream()
            .collect(new CompletableFutureCombineCollector<>());

        long intermediateTimestamp = System.currentTimeMillis();

        List<String> result = resultCompletableFuture.get();
        long endTimestamp = System.currentTimeMillis();

        assertThat(result).as("모든 문자열이 빠르게 실행 순서 상관없이 CompletableFuture 생성순서에 따라 반환된다.")
            .isEqualTo(List.of("하나", "둘", "셋", "넷", "다섯"));

        assertThat(Duration.ofMillis(intermediateTimestamp - startTimestamp))
            .as("합쳐주는 CompletableFuture 생성까지는 non blocking 이므로 시간차가 거의 안난다.")
            .isBetween(Duration.ofMillis(0), Duration.ofMillis(10)); // 10 ms 이내.

        assertThat(Duration.ofMillis(endTimestamp - startTimestamp))
            .as("동시에 실행되므로 가장 오래 걸리는 100ms 보다 약간 오래 걸린 수준에서 끝난다.")
            .isBetween(Duration.ofMillis(100), Duration.ofMillis(110));
    }

    CompletableFuture<String> delayedString(long millis, String result) {
        return new CompletableFuture<String>().completeOnTimeout(result, millis, TimeUnit.MILLISECONDS);
    }


}