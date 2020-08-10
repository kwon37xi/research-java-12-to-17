package kr.pe.kwonnam.research.java.completablefuture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 동일한 타입을 리턴하는 CompletableFuture&lt;T&gt; 리스트를 합쳐서 그 결과를 {@link List}로 리턴하는
 * 새로운 {@link CompletableFuture}를 생성하는 Stream collector
 *
 * <pre>
 *     CompletableFuture&lt;String&gt; completableFutures = List.of(
 *          CompletableFuture.completedFuture("string1"),
 *          CompletableFuture.completedFuture("string2"),
 *          CompletableFuture.completedFuture("string3")
 *     );
 *     CompletableFuture&lt;List&lt;String&gt;&gt; resultCompletableFuture =
 *          completableFuturs.stream()
 *          .collect(new CompletableFutureCollector&lt;&gt;());
 *
 *      List&lt;String&gt; result = resultCompletableFuture.get();
 * </pre>
 * @param <T>
 */
public class CompletableFutureCollector<T> implements Collector<CompletableFuture<T>, List<CompletableFuture<T>>, CompletableFuture<List<T>>> {

    @Override
    public Supplier<List<CompletableFuture<T>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<CompletableFuture<T>>, CompletableFuture<T>> accumulator() {
        return Collection::add;
    }

    @Override
    public BinaryOperator<List<CompletableFuture<T>>> combiner() {
        return (completableFutures, completableFutures2) -> {
            List<CompletableFuture<T>> combined = new ArrayList<>();
            combined.addAll(completableFutures);
            combined.addAll(completableFutures2);
            return combined;
        };
    }

    @Override
    public Function<List<CompletableFuture<T>>, CompletableFuture<List<T>>> finisher() {
        return completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
            .thenApply(aVoid -> completableFutures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.singleton(Characteristics.CONCURRENT);
    }
}
