package kr.pe.kwonnam.research.java.completablefuture;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@DisplayName("CompletableFutureAsyncTest")
class CompletableFutureAsyncTest {

    private CompletableFutureAsync completableFutureAsync;

    @BeforeEach
    void setUp() {
        completableFutureAsync = new CompletableFutureAsync();
    }

    @Test
    @DisplayName("supplyAsync 기본 : supplyAsync 는 ForkJoinPool ThreadPool을 사용한다.")
    void supplyAsyncBasic() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = completableFutureAsync.supplyAsyncBasic();

        assertThat(Thread.currentThread().getName()).isEqualTo("main");
        assertThat(cf.get()).startsWith("hello!")
            .contains("and ForkJoinPool.commonPool");
    }

    @Test
    @DisplayName("thenApply : supplyAsync 이후 연달아서 그 결과를 받아서 처리하려면 thenApply를 사용한다.")
    void supplyAsyncThenApply() throws ExecutionException, InterruptedException {
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            threadNames.add(Thread.currentThread().getName());
            return "hello";
        });

        CompletableFuture<String> finalFuture = cf.thenApply(s -> {
            threadNames.add(Thread.currentThread().getName());
            return s + " world!";
        });

        assertThat(finalFuture.get()).isEqualTo("hello world!");

        log.info("Threads : {}", threadNames);

        assertThat(threadNames.get(0))
            .as("supplyAsync는 별도 쓰레드 풀에서 실행된다.")
            .contains("ForkJoinPool.");

        // thenAccept 의 쓰레드는 상황에 따라 달라진다.

        assertThat(Thread.currentThread().getName()).isEqualTo("main");
    }


    @Test
    @DisplayName("thenAccept : supplyAsync 이후 연달아서 그 결과를 받아서 실행하되 값 리턴이 불필요하면 thenAccept를 사용한다.")
    void supplyAsyncThenAccept() throws ExecutionException, InterruptedException {
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            threadNames.add(Thread.currentThread().getName());
            return "Hello";
        });

        CompletableFuture<Void> finalFuture = cf.thenAccept(s -> {
            threadNames.add(Thread.currentThread().getName());
            System.out.println("Computation returned " + s);
            // no return
        });

        finalFuture.get();

        log.info("Threads : {}", threadNames);

        assertThat(threadNames.get(0))
            .as("supplyAsync는 별도 쓰레드 풀에서 실행된다.")
            .contains("ForkJoinPool.");

        // thenAccept 의 쓰레드는 상황에 따라 달라진다.

        assertThat(Thread.currentThread().getName()).isEqualTo("main");
    }

    @Test
    @DisplayName("thenRun : supplyAsync 이후 연달아서 실행하되 앞선 결과값도, 새로운 값 리턴도 불필요하면 thenRun을 사용한다.")
    void supplyAsyncThenRun() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computation started!");
            return "hello";
        }).thenRun(() -> System.out.println("Computation finished!"));// 인자도 없고 리턴값도 없다.

        future.get();
    }

    @Test
    @DisplayName("thenCompose : supplyAsync 이후 그 결과 값을 받아 또 다른 CompletableFuture를 수행하려면 thenCompose 를 사용한다.")
    void thenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "Hello")
            .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World!"));

        assertThat(cf.get()).isEqualTo("Hello World!");
    }

    @Test
    @DisplayName("thenCombine : 두 개의 CompletableFuture를 독립 실행하고 실행이 둘 다 끝나면 그 결과를 합쳐서 리턴한다.")
    void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "Hello")
            .thenCombine(CompletableFuture.supplyAsync(() -> "World!"),
                (s, s2) -> s + " " + s2);

        assertThat(cf.get()).isEqualTo("Hello World!");
    }

    @Test
    @DisplayName("thenAcceptBoth : thenCombine과 같지만 리턴값이 없다.")
    void thenAcceptBoth() {
        CompletableFuture.supplyAsync(() -> "Hello")
            .thenAcceptBoth(CompletableFuture.supplyAsync(() -> "World!"),
                (s, s2) -> System.out.printf("thenAcceptBoth : %s %s%n", s, s2)); // 리턴값이 없다.
    }

    @Test
    @DisplayName("allOf : 모든 CompletableFuture 를 동시 실행하고 종료될 때 까지 기다린다.")
    void allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Beautiful");
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "World");

        CompletableFuture<Void> all = CompletableFuture.allOf(future1, future2, future3);

        all.get(); // no return value

        assertThat(future1).isDone();
        assertThat(future2).isDone();
        assertThat(future3).isDone();
    }

    @Test
    @DisplayName("join : 여러 CompletableFuture를 Stream으로 Join하면 결과값을 합칠 수 있다.")
    void joinCompletableFutures() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Beautiful");
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "World");

        String combined = Stream.of(future1, future2, future3)
            .map(CompletableFuture::join)
            .collect(joining(" "));

        // 비동기로 실행되더라도 join 하는 순서 때문에 future1, future2, future3 순서가 그대로 유지됨.
        assertThat(combined).isEqualTo("Hello Beautiful World");
    }

    @Test
    @DisplayName("supplyAsyncWithException : handle을 통해 예외를 처리한다. handle이 받는 예외는 CompletionException 으로 감싸져 있다. handle() 자체도 CompletableFuture 를 리턴한다.")
    void supplyAsyncWithException_no_name() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = completableFutureAsync.supplyAsyncWithException(null);
        assertThat(completableFuture.get())
            .as("예외가 발생했으므로 예외 관련 내용을 반환한다. throwable은 CompletionException 으로 감싸져 있다.")
            .isEqualTo("Hello Stranger! - throwable name : java.util.concurrent.CompletionException, java.lang.IllegalArgumentException: Computation Error!");
    }

    @Test
    @DisplayName("supplyAsyncWithException : handle을 통해 정상응답을 처리한다. handle() 자체도 CompletableFuture 를 리턴한다.")
    void supplyAsyncWithException_with_name() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = completableFutureAsync.supplyAsyncWithException("Seoul");
        assertThat(completableFuture.get())
            .as("정상 응답을 리턴한다.")
            .isEqualTo("Hello Seoul");
    }

    @Test
    @DisplayName("completeExceptionally : 예외를 던지고 끝내버린다.")
    void completeExceptionally() {
        IllegalArgumentException causeException = new IllegalArgumentException("Calculation failed!");

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(causeException);

        ExecutionException executionException = catchThrowableOfType(() -> {
            completableFuture.get();
        }, ExecutionException.class);

        assertThat(executionException)
            .as("ExecutionException 이 실제 예외를 감싸고 있다.")
            .hasCause(causeException)
            .hasMessage("java.lang.IllegalArgumentException: Calculation failed!");
    }
}