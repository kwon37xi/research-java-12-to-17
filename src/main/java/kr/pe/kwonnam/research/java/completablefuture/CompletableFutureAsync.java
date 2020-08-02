package kr.pe.kwonnam.research.java.completablefuture;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureAsync {
    public CompletableFuture<String> supplyAsyncBasic() {
        return CompletableFuture.supplyAsync(() -> "hello! and " + Thread.currentThread().getName());
    }
}
