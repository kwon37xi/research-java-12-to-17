package kr.pe.kwonnam.research.java.completablefuture;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <a href="https://www.nurkiewicz.com/2015/11/which-thread-executes.html">Which thread executes CompletableFuture's tasks and callbacks?</a>
 */
public class CompletableFutureAsyncThreadPoolTest {
    ExecutorService executor;
    ConcurrentLinkedQueue<String> threadNamesQueue;
    String testThreadName;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(10);
        threadNamesQueue = new ConcurrentLinkedQueue<>();
        testThreadName = Thread.currentThread().getName();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("supplyAsync 작업이 끝난 뒤에 등록된 thenApply 는 호출자 쓰레드에서 실행된다.")
    void thenApplyAfterSupplyAsyncDoneThread() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            logThreadName();
            return "Hello world!";
        }, executor);

        sleepMillis(100); // supplyAsync 가 충분히 끝날 수 있는 시간

        CompletableFuture<Integer> lengthCompletableFuture = completableFuture.thenApply(s -> {
            logThreadName();
            return s.length();
        });

        assertThat(lengthCompletableFuture.get()).isEqualTo(12);
        assertThat(threadNamesQueue.poll()).as("supplyAsync 쓰레드는 쓰레드 풀에서 사용한다.").startsWith("pool");
        assertThat(threadNamesQueue.poll()).as("thenApply 쓰레드는 호출자 쓰레드이다.").doesNotStartWith("pool").isEqualTo(testThreadName);
    }


    @Test
    @DisplayName("supplyAsync 작업이 진행중인 상태에서 등록된 thenApply 는 supplyAsync와 동일한 쓰레드에서 실행된다.")
    void thenApplyAfterSupplyAsyncRunningThread() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            logThreadName();
            // thenApply 등록이 될때까지 supplyAsync 를 지연한다.
            sleepMillis(1000);
            return "Hello world!";
        }, executor).thenApply(s -> {
            logThreadName();
            return s + " 안녕!";
        });

        // supplyAsync 종료할 때 쯤까지 기다림
        sleepMillis(1010);

        CompletableFuture<Integer> lengthCompletableFuture = completableFuture.thenApply(s -> {
            logThreadName();
            return s.length();
        });

        assertThat(lengthCompletableFuture.get()).isEqualTo(16);

        String supplyAsyncThreadName = threadNamesQueue.poll();
        String thenApplyFirstThreadName = threadNamesQueue.poll();
        String thenApplySecondThreadName = threadNamesQueue.poll();

        assertThat(supplyAsyncThreadName).as("supplyAsync 쓰레드는 쓰레드 풀에서 사용한다.")
            .startsWith("pool");
        assertThat(thenApplyFirstThreadName).as("supplyAsync 진행중에 등록된 thenApply의 실행쓰레드는 supplyAsync와 동일한 쓰레드이다.")
            .startsWith("pool").isEqualTo(supplyAsyncThreadName);
        assertThat(thenApplySecondThreadName).as("supplyAsync 진행완료 후에 등록된 thenApply의 실행쓰레드는 호출자 쓰레드이다.")
            .isEqualTo(testThreadName);
    }

    @Test
    @DisplayName("thenApplyAsync 는 명백하게 별도 쓰레드에서 작업을 비동기로 진행한다.")
    void thenApplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            logThreadName();
            return "Hello world!";
        }, executor);

        sleepMillis(100); // supplyAsync 가 충분히 끝날 수 있는 시간

        CompletableFuture<Integer> lengthCompletableFuture = completableFuture.thenApplyAsync(s -> {
            logThreadName();
            return s.length();
        }, executor);

        assertThat(lengthCompletableFuture.get()).isEqualTo(12);
        String supplyAsyncThreadName = threadNamesQueue.poll();
        String thenApplyAsyncThreadName = threadNamesQueue.poll();

        assertThat(supplyAsyncThreadName).as("supplyAsync 쓰레드는 쓰레드 풀에서 사용한다.")
            .startsWith("pool");
        assertThat(thenApplyAsyncThreadName).as("thenApplyAsync 쓰레드는 또 다른 쓰레드 풀에서 사용한다.")
            .startsWith("pool");
    }

    @SneakyThrows
    @Test
    @DisplayName("thenApplyWithoutAsync : *Async가 없는 상황일 경우 또 다른 CompletableFuture.supplyAsync와 thenCompose조합으로 별도 쓰레드 비동기 적용 가능하다.")
    void thenApplyWithoutAsync() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            logThreadName();
            return "Hello my computer!";
        }, executor);

        sleepMillis(100); // supplyAsync 가 충분히 끝날 수 있는 시간

        CompletableFuture<Integer> lengthCompletableFuture = completableFuture
            .thenApply(this::strLen) // thenApplyAsync가 없다는 가정하에, 여기서는 CompletableFuture<CompletableFuter<Integer>) 상태
            .thenCompose(x -> x);  // CompletableFuture<Integer> 로 전환

        assertThat(lengthCompletableFuture.get()).isEqualTo(18);
        assertThat(threadNamesQueue.poll()).as("supplyAsync 쓰레드는 쓰레드 풀에서 사용한다.").startsWith("pool");
        assertThat(threadNamesQueue.poll()).as("thenApply 쓰레드는 호출자 쓰레드이다.").startsWith("pool");
    }

    CompletableFuture<Integer> strLen(String s) {
        return CompletableFuture.supplyAsync(
            () -> {
                logThreadName();
                return s.length();
            },
            executor);
    }

    void sleepMillis(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void logThreadName() {
        threadNamesQueue.add(Thread.currentThread().getName());
    }
}
