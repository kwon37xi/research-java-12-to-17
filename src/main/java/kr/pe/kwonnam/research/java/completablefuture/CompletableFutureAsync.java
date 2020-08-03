package kr.pe.kwonnam.research.java.completablefuture;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureAsync {
    public CompletableFuture<String> supplyAsyncBasic() {
        return CompletableFuture.supplyAsync(() -> "hello! and " + Thread.currentThread().getName());
    }

    public CompletableFuture<String> supplyAsyncWithException(String name) {
        return CompletableFuture.supplyAsync(() -> {
            if (name == null) {
                throw new IllegalArgumentException("Computation Error!");
            }
            return "Hello " + name;
        }).handle((String s, Throwable throwable) -> {
            if (s != null) {
                return s;
            }

            return String.format("Hello Stranger! - throwable name : %s, %s", throwable.getClass().getName(), throwable.getMessage());
        });
    }
}
