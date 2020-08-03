package kr.pe.kwonnam.research.java.completablefuture;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureJava9Test {
    private ExecutorService myExecutor;
    private AtomicInteger atomicInteger;

    @BeforeEach
    void setUp() {
        atomicInteger = new AtomicInteger();
        myExecutor = Executors.newCachedThreadPool(r -> new Thread(r, "MyExecutor-" + atomicInteger.getAndIncrement()));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        myExecutor.shutdown();
        if (!myExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
            myExecutor.shutdownNow();
        }
    }

    @Test
    @DisplayName("override하지 않은 defaultExecutor 는 ForkJoinPool.commonPool() 이다.")
    void defaultExecutor() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        String threadName = completableFuture.supplyAsync(() -> Thread.currentThread().getName()).get();

        assertThat(threadName).startsWith("ForkJoinPool.commonPool");
    }

    @Test
    @DisplayName("NeoCompletableFuture : supplyAsync 를 override 하여, 원하는 executor 로 실행한다.")
    void defaultExecutorOverrideSupplyAsync() throws ExecutionException, InterruptedException {

        String threadName = NeoCompletableFuture.supplyAsync(() -> Thread.currentThread().getName(), myExecutor).get();

        assertThat(threadName).startsWith("MyExecutor-0");
    }

    @Test
    @DisplayName("NeoCompletableFuture : defaultExecutor override 로 다른 *Async 들이 defaultExecutor 에서 실행된다.")
    void defaultExecutorOverrideThenSomeAsync() {

        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<String> completableFuture = NeoCompletableFuture.supplyAsync(() -> {
            threadNames.add(Thread.currentThread().getName());
            return "Hello";
        }, myExecutor);

        CompletableFuture<Void> voidCompletableFuture = completableFuture.thenApplyAsync(s -> {
            threadNames.add(Thread.currentThread().getName());
            return s + " World!";
        }).thenAcceptAsync(s -> {
            threadNames.add(Thread.currentThread().getName());
            assertThat(s).isEqualTo("Hello World!");
        });

        voidCompletableFuture.join();

        assertThat(threadNames.get(0))
            .as("첫번째 실행이므로 0번이다.")
            .isEqualTo("MyExecutor-0");
        assertThat(threadNames.get(1))
            .as("newIncompleteFuture() 메소드도 함께 override 하면 나머지 thenApplyAsync 에 defaultExecutor()가 적용된다.")
            .startsWith("MyExecutor-0");
        assertThat(threadNames.get(2))
            .as("newIncompleteFuture() 메소드도 함께 override 하면 나머지 thenAcceptAsync 에 defaultExecutor()가 적용된다.")
            .startsWith("MyExecutor-");
    }

    @Test
    @DisplayName("NeoCompletableFuture : runAsync 를 override 하여, 원하는 executor 로 실행한다.")
    void defaultExecutorRunAsync() {
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());
        NeoCompletableFuture.runAsync​(() -> threadNames.add(Thread.currentThread().getName()), myExecutor);

        assertThat(threadNames).hasSize(1)
            .hasSameElementsAs(List.of("MyExecutor-0"));
    }
}
