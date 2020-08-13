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
import static org.assertj.core.api.Assertions.in;

@DisplayName("handle과 예외 처리")
public class CompletableFutureHandleTest {
    List<String> messages;

    @BeforeEach
    void setUp() {
        messages = Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    @DisplayName("handle 호출 전에 예외가 발생하지 않을 경우 - handle은 중간에 앞선 결과 데이터를 받아서 변환해서 후속 전달한다.")
    void handleAndNoException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 2)
            .handle((input, throwable) -> {
                if (throwable != null) {
                    messages.add("exception occurs - " + throwable.getMessage());
                } else {
                    messages.add("no exception, got result: " + input);
                }
                // no return value;
                return input + 1;
            }).thenApply(input -> input * 3);

        assertThat(messages).hasSize(1).containsExactly("no exception, got result: 8");
        assertThat(completableFuture).isCompletedWithValue(27);
    }

    @Test
    @DisplayName("handle 호출 전에 예외 발생 - handle은 중간에 예외를 볼수 있고 예외를 보정할수도 있다.")
    void handleAndPreException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .handle((input, throwable) -> {
                if (throwable != null) {
                    messages.add("exception occurs - " + throwable.getMessage());
                    return -1;
                } else {
                    messages.add("no exception, got result: " + input);
                }
                return input + 1;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .as("예외가 발생했으나 값을 보정해서 정상으로 전환하였다.")
            .isCompletedWithValue(-3);

        assertThat(messages).containsExactly("exception occurs - java.lang.ArithmeticException: / by zero");
    }

    @Test
    @DisplayName("handle 호출 전에 예외 발생 - exceptionally에서 처리하면 예외가 사라진 상태로 handle에 전달된다.")
    void handleAndPreExceptionWithExceptionally() {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .exceptionally(throwable -> {
                messages.add("recovering in exceptionally: " + throwable.getMessage());
                return 1;
            })
            .handle((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                    return -1;
                } else {
                    messages.add("no exception, got result: " + input);
                }
                return input + 1;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .isCompletedWithValue(6);

        assertThat(messages)
            .as("예외가 복구되었으므로 no exception 메시지가 들어있다.")
            .hasSize(2)
            .containsExactly("recovering in exceptionally: java.lang.ArithmeticException: / by zero", "no exception, got result: 1");
    }

    @Test
    @DisplayName("handle 호출 전에 예외 발생 - handle에서도 예외 발생하면 handle의 예외가 후속 전달된다.")
    void handleAndPreExceptionAndHandleException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 0)
            .handle((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                    // force exception
                    throw new IllegalStateException("exception in handle.");
                } else {
                    messages.add("no exception, got result: " + input);
                }
                 return input + 1;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .hasFailedWithThrowableThat()
            .as("원본 예외는 무시되고 handle의 예외가 후속 전달된다.")
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("exception in handle.");
    }

    @Test
    @DisplayName("handle 호출 전은 정상 실행 - handle에서 예외 발생하면 handle 예외가 후속 전달된다.")
    void handleAndPreNormalCompleteAndHandleException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 16 / 4)
            .handle((input, throwable1) -> {
                if (throwable1 != null) {
                    messages.add("exception occurs - " + throwable1.getMessage());
                    return -1;
                } else {
                    messages.add("no exception, got result: " + input);
                }
                throw new IllegalStateException("exception in handle.");
//                return input + 1;
            }).thenApply(input -> input * 3);

        assertThat(completableFuture)
            .hasFailedWithThrowableThat()
            .as("handle의 예외가 전달된다.")
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("exception in handle.");
    }
}
