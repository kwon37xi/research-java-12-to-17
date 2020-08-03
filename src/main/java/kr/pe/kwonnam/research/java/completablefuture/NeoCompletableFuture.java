package kr.pe.kwonnam.research.java.completablefuture;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * Java 9 의 {@link CompletableFuture}를 상속하려면 {@link CompletableFuture#newIncompleteFuture()}도 함께 구현해줘야 한다.
 * {@code newIncompleteFuture} 덕분에 상속이 조금은 쉬워졌다.
 * <p>
 * 또한, {@code supplyAsync} 등 시작이 되는 static 메소드들도 모두 새로 만들어줘야 한다.
 * <p>
 * TODO 다른 모든 static 메소드 override
 *
 * @see <a href="https://stackoverflow.com/a/56356109/1051402">Java 9 이후의 CompletableFuture의 상속</a>
 * @see <a href="https://stackoverflow.com/a/26607433/1051402">Java 8 이하의 CompletableFuture의 상속</a>
 */
public class NeoCompletableFuture<T> extends CompletableFuture<T> {

    private final Executor executor;

    NeoCompletableFuture(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Executor defaultExecutor() {
        return executor;
    }

    /**
     * @return 호환성을 위해 CompletableFuture 타입으로 반환할것
     */
    @Override
    public CompletableFuture<T> newIncompleteFuture() {
        return new NeoCompletableFuture<>(executor);
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(executor);

        NeoCompletableFuture<T> neoCompletableFuture = new NeoCompletableFuture<>(executor);
        return neoCompletableFuture.completeAsync(supplier);
    }

    public static CompletableFuture<Void> runAsync​(Runnable runnable, Executor executor) {
        Objects.requireNonNull(runnable);
        return supplyAsync(() -> {
            runnable.run();
            return null;
        }, executor);
    }

    /**
     * 호출 불가하게 막아야 한다.
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> s) {
        throw new UnsupportedOperationException(NeoCompletableFuture.class + " supplyAsync(Supplier) is unsupported.");
    }

    public static CompletableFuture<Void> runAsync​(Runnable runnable) {
        throw new UnsupportedOperationException(NeoCompletableFuture.class + " runAsync(Runnable) is unsupported.");

    }
}
