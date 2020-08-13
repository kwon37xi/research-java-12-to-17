package kr.pe.kwonnam.research.java.completablefuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("whenComplete과 예외 처리")
public class CompletableFutureWhenCompleteTest {
    List<String> messages;

    @BeforeEach
    void setUp() {
        messages = Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    @DisplayName("whenComplete 호출 전에 예외가 발생하지 않을 경우 - whenComplete은 중간에 앞선 결과 데이터를 볼수있다. 하지만 그대로 그 다음으로 전달된다.")
    void whenCompleteAndNoException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 2)
            .whenComplete((input, throwable) -> {
                if (throwable != null) {
                    messages.add("exception occurs - " + throwable.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                // no return value;
            }).thenApply(input -> input * 3);

        assertThat(messages).hasSize(1).containsExactly("no exception, got result: 8");
        assertThat(completableFuture.get()).isEqualTo(24);
    }

    @Test
    @DisplayName("whenComplete 호출 전에 예외 발생 - whenComplete은 중간에 예외를 볼수 있고 예외 상태 그대로 후속 전달된다.")
    void whenCompleteAndPreException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .whenComplete((input, throwable) -> {
                if (throwable != null) {
                    messages.add("exception occurs - " + throwable.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                // no return value;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .hasFailedWithThrowableThat()
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("/ by zero");
    }

    @Test
    @DisplayName("whenComplete 호출 전에 예외 발생 - exceptionally에서 처리하면 예외가 사라진 상태로 whenComplete에 전달된다.")
    void whenCompleteAndPreExceptionWithExceptionally() {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .exceptionally(throwable -> {
                messages.add("recovering in exceptionally: " + throwable.getMessage());
                return 1;
            })
            .whenComplete((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                // no return value;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .isCompletedWithValue(3);

        assertThat(messages)
            .as("예외가 복구되었으므로 no exception 메시지가 들어있다.")
            .hasSize(2)
            .containsExactly("recovering in exceptionally: java.lang.ArithmeticException: / by zero", "no exception, got result: 1");
    }

    @Test
    @DisplayName("whenComplete 호출 전에 예외 발생 - whenComplete에서도 예외 발생하면 원본 예외가 후속 전달된다.")
    void whenCompleteAndPreExceptionAndWhenCompleteException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .whenComplete((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                throw new IllegalStateException("exception in whenComplete.");
                // no return value;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .hasFailedWithThrowableThat()
            .as("whenComplete의 예외는 무시되고, 원본 예외가 전달된다.")
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("/ by zero");
    }

    @Test
    @DisplayName("whenComplete 호출 전은 정상 실행 - whenComplete에서 예외 발생하면 whenComplete 예외가 후속 전달된다.")
    void whenCompleteAndPreNormalCompleteAndWhenCompleteException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 4)
            .whenComplete((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                throw new IllegalStateException("exception in whenComplete.");
                // no return value;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .hasFailedWithThrowableThat()
            .as("whenComplete의 예외가 전달된다.")
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("exception in whenComplete.");
    }
}
