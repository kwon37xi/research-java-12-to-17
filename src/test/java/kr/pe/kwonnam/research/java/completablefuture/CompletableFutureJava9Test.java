package kr.pe.kwonnam.research.java.completablefuture;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

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
            .startsWith("MyExecutor-");
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

    @Test
    @DisplayName("copyCompleted : 이미 종료된 CompletableFuture 를 복제하면 종료 상태 그대로의 복제본이 나온다.")
    void copyCompleted() {
        CompletableFuture<String> original = CompletableFuture.completedFuture("Hello World!");

        CompletableFuture<String> copied = original.copy();

        assertThat(copied).isCompletedWithValue("Hello World!");

        CompletableFuture<String> exceptionalOriginal = CompletableFuture.failedFuture(new IllegalStateException("Failed CF!"));

        CompletableFuture<String> exceptionalCopied = exceptionalOriginal.copy();

        assertThat(exceptionalCopied).hasFailedWithThrowableThat().hasMessage("Failed CF!");
    }

    @Test
    @DisplayName("notCopiedIncomplete : 아직 종료전인 CompletableFuture를 cancel하면 원본이 취소돼야 한다.")
    void notCopiedIncomplete() throws ExecutionException, InterruptedException {
        CompletableFuture<String> original = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello CompletableFuture!";
        });

        original.cancel(true);

        assertThat(original).isCancelled();
    }

    @Test
    @DisplayName("copiedIncomplete : 아직 종료전인 CompletableFuture를 copy해서 cancel 하면 복제본만 cancel 되고 원본은 계속 실행된다.")
    void copiedIncomplete() throws ExecutionException, InterruptedException {
        CompletableFuture<String> original = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello CompletableFuture Original!";
        });

        CompletableFuture<String> copied = original.copy();

        copied.cancel(true);

        assertThat(copied)
            .as("복제본에 취소 요청했으므로 복제본은 취소 상태가 된다.")
            .isCancelled();

        assertThat(original)
            .as("원본은 취소되지 않는다.")
            .isNotCancelled();

        assertThat(original.get())
            .as("원본은 정상적으로 응답을 내려준다.")
            .isEqualTo("Hello CompletableFuture Original!");
    }

    @Test
    @DisplayName("minimalCompletionStage : copy()와 동일하게 작동하지만 CompletableFuture 에서 CompletionStage 인터페이스 구현만 리턴하고 나머지는 메소드는 모두 UnsupportedException을 낸다.")
    void minimalCompletionStage() {
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("Hello World!");

        CompletionStage<String> completionStage = completableFuture.minimalCompletionStage();

        UnsupportedOperationException unsupportedOperationException = catchThrowableOfType(() -> {
            ((CompletableFuture<String>) completionStage).isCancelled();
        }, UnsupportedOperationException.class);

        assertThat(unsupportedOperationException).hasMessage(null);

        CompletableFuture<String> completableFutureNew = completableFuture.toCompletableFuture();

        assertThat(completableFutureNew.isCancelled())
            .as("toCompletableFuture()로 다시 원상 복구하면 CompletableFuture의 메소드가 호출 가능해진다.")
            .isFalse();
    }

    @Test
    @DisplayName("completeAsync : supplier 를 비동기로 수행하고 CompletableFuture를 완료한다.")
    void completeAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = new CompletableFuture<>()
            .completeAsync(() -> "Hello CompletableFuture!")
            .thenApply(s -> s + " How are you doing?");

        assertThat(completableFuture.get()).isEqualTo("Hello CompletableFuture! How are you doing?");
    }

    @Test
    @DisplayName("orTimeout : 시간내 수행 완료시 오류 없이 완료된다.")
    void orTimeoutInTime() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello CompletableFuture!")
            .orTimeout(1, TimeUnit.SECONDS);

        assertThat(completableFuture.get())
            .as("시간내 실행 완료시 오류 없이 완료한다.")
            .isEqualTo("Hello CompletableFuture!");
    }


    @Test
    @DisplayName("orTimeout : 시간내 수행 완료 실패시 오류가 발생한다.")
    void orTimeoutNotInTime() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello CompletableFuture!";
        })
            .orTimeout(1, TimeUnit.SECONDS);

        ExecutionException executionException = catchThrowableOfType(() -> {
            completableFuture.get();
        }, ExecutionException.class);

        assertThat(executionException)
            .as("시간내 실행 완료 실패시 오류가 발생한다.")
            .hasCauseInstanceOf(TimeoutException.class)
            .hasMessage(TimeoutException.class.getName());
    }


    @Test
    @DisplayName("completeOnTimeout : 시간내 수행 완료시 성공시 값을 반환한다.")
    void completeOnTimeoutInTime() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello CompletableFuture!";
        }).completeOnTimeout("You failed!", 2, TimeUnit.SECONDS);

        completableFuture.join();

        assertThat(completableFuture)
            .as("시간내 수행 완료를 성공하면 오류 없이 성공한 값을 리턴한다.")
            .isCompleted()
            .isNotCancelled()
            .isCompletedWithValue("Hello CompletableFuture!");
    }

    @Test
    @DisplayName("completeOnTimeout : 시간내 수행 완료 실패시 completeOnTimeout에 지정된 값을 반환한다.")
    void completeOnTimeoutNotInTime() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello CompletableFuture!";
        }).completeOnTimeout("You failed!", 1, TimeUnit.SECONDS);

        completableFuture.join();

        assertThat(completableFuture)
            .as("시간내 수행 완료를 못했을 경우에도 오류 없이 completeOnTimeout 에 지정한 값을 리턴한다.")
            .isCompleted()
            .isNotCancelled()
            .isCompletedWithValue("You failed!");
    }

    @Test
    void delayedExecutor() throws ExecutionException, InterruptedException {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

        List<Long> timestamps = Collections.synchronizedList(new ArrayList<>());
        timestamps.add(System.currentTimeMillis());
        String result = CompletableFuture.supplyAsync(() -> {
            timestamps.add(System.currentTimeMillis());
            return "delayed!";
        }, delayedExecutor).get();

        assertThat(result).isEqualTo("delayed!");
        Duration delayed = Duration.ofMillis(timestamps.get(1) - timestamps.get(0));
        assertThat(delayed).isBetween(Duration.ofMillis(1000), Duration.ofMillis(1100));
    }
}
